package jku.service;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import jku.api.ScanRequest;
import jku.entity.EventTyp;
import jku.entity.Moebelstuck;
import jku.entity.Pruefergebnis;
import jku.entity.Zustand;
import jku.entity.ScanHistory;
import jku.entity.ProzessInstanz;
import jku.repository.MoebelstuckRepository;
import jku.repository.ProzessInstanzRepository;
import jku.repository.ScanHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class ScanProcessService {

    private static final Logger log = LoggerFactory.getLogger(ScanProcessService.class);

    private static final String BPMN_PROCESS_ID = "moebelrueckgabe-standard";

    private static final Map<EventTyp, Zustand> ZUSTAND_NACH_EVENT = Map.of(
            EventTyp.REPARATUR, Zustand.IN_REPARATUR,
            EventTyp.ENTSORGUNG, Zustand.ENTSORGT,
            EventTyp.TEILDEMONTAGE, Zustand.TEILWEISE_BESCHAEDIGT,
            EventTyp.EINLAGERUNG, Zustand.GUT
    );

    private static final Map<Pruefergebnis, Zustand> ZUSTAND_NACH_PRUEFUNG = Map.of(
            Pruefergebnis.GUT, Zustand.GUT,
            Pruefergebnis.REPARATUR, Zustand.IN_REPARATUR,
            Pruefergebnis.TEILWEISE_BESCHAEDIGT, Zustand.TEILWEISE_BESCHAEDIGT,
            Pruefergebnis.SCHLECHT, Zustand.DEFEKT
    );

    private static final Map<EventTyp, String> EVENT_TO_MESSAGE = Map.of(
            EventTyp.RUECKGABE, "moebel-rueckgabe-start",
            EventTyp.PRUEFUNG, "moebel-pruefung-done",
            EventTyp.EINLAGERUNG, "moebel-eingelagert",
            EventTyp.REPARATUR, "moebel-reparatur-start",
            EventTyp.ENTSORGUNG, "moebel-entsorgt",
            EventTyp.TEILDEMONTAGE, "moebel-teildemontage"
    );

    @Autowired(required = false)
    private CamundaClient camundaClient;

    private final MoebelstuckRepository moebelRepo;
    private final ScanHistoryRepository scanRepo;
    private final ProzessInstanzRepository prozessRepo;

    public ScanProcessService(MoebelstuckRepository moebelRepo,
                              ScanHistoryRepository scanRepo,
                              ProzessInstanzRepository prozessRepo) {
        this.moebelRepo = moebelRepo;
        this.scanRepo = scanRepo;
        this.prozessRepo = prozessRepo;
    }

    public Moebelstuck handleScan(ScanRequest request) {
        Moebelstuck moebel = moebelRepo.findByNfcTagId(request.nfcTagId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag nicht angelegt"));

        ScanHistory scan = new ScanHistory();
        scan.setMoebelstuckId(moebel.getId());
        scan.setStandortLat(request.standortLat());
        scan.setStandortLng(request.standortLng());
        scan.setStandortName(request.standortName());
        scan.setEventTyp(request.eventTyp());
        scan.setGescanntVon(request.gescanntVon());
        scan.setPruefergebnis(request.pruefergebnis());
        scan.setErsatzteileVorhanden(request.ersatzteileVorhanden());
        scanRepo.save(scan);

        moebel.setStandortName(request.standortName());
        if (request.standortLat() != null && request.standortLng() != null) {
            moebel.setStandortLat(request.standortLat());
            moebel.setStandortLng(request.standortLng());
        }
        if (request.eventTyp() == EventTyp.PRUEFUNG && request.pruefergebnis() != null) {
            moebel.setZustand(ZUSTAND_NACH_PRUEFUNG.get(request.pruefergebnis()));
        } else {
            Zustand neuerZustand = ZUSTAND_NACH_EVENT.get(request.eventTyp());
            if (neuerZustand != null) {
                moebel.setZustand(neuerZustand);
            }
        }
        moebelRepo.save(moebel);

        // Camunda-Integration (optional)
        if (camundaClient == null) {
            log.info("Camunda nicht verbunden, Scan ohne Prozess-Integration gespeichert.");
            return moebel;
        }

        try {
            if (request.eventTyp() == EventTyp.RUECKGABE) {
                ProcessInstanceEvent event = camundaClient
                        .newCreateInstanceCommand()
                        .bpmnProcessId(BPMN_PROCESS_ID)
                        .latestVersion()
                        .variables(Map.of(
                                "moebelId", moebel.getId().toString(),
                                "nfcTagId", moebel.getNfcTagId(),
                                "standort", request.standortName()
                        ))
                        .execute();

                ProzessInstanz pi = new ProzessInstanz();
                pi.setMoebelstuckId(moebel.getId());
                pi.setCamundaInstanceKey(event.getProcessInstanceKey());
                pi.setStatus("GESTARTET");
                prozessRepo.save(pi);

                log.info("Camunda Prozess gestartet: {}", event.getProcessInstanceKey());
            } else {
                String message = EVENT_TO_MESSAGE.get(request.eventTyp());
                if (message != null) {
                    Map<String, Object> vars = new java.util.HashMap<>(Map.of(
                            "standort", request.standortName(),
                            "eventTyp", request.eventTyp().name()
                    ));
                    if (request.pruefergebnis() != null) {
                        vars.put("pruefergebnis", request.pruefergebnis().name());
                    }
                    if (request.ersatzteileVorhanden() != null) {
                        vars.put("ersatzteileVorhanden", request.ersatzteileVorhanden());
                    }
                    camundaClient.newPublishMessageCommand()
                            .messageName(message)
                            .correlationKey(moebel.getId().toString())
                            .variables(vars)
                            .send()
                            .join();
                    log.info("Camunda Message '{}' korreliert fuer {}", message, moebel.getId());
                }
            }
        } catch (Exception e) {
            log.warn("Camunda-Anbindung fehlgeschlagen (wird ignoriert): {}", e.getMessage());
        }

        return moebel;
    }
}
