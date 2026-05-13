package jku.service;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import jku.api.ScanRequest;
import jku.entity.*;
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
import java.util.Set;

@Service
public class ScanProcessService {

    private static final Logger log = LoggerFactory.getLogger(ScanProcessService.class);

    private static final String BPMN_PROCESS_ID = "moebelrueckgabe-standard";

    private static final Set<String> VALID_EVENTS = Set.of(
            "RUECKGABE", "EINLAGERUNG", "PRUEFUNG", "TRANSPORT",
            "REPARATUR", "ENTSORGUNG", "TEILDEMONTAGE"
    );

    private static final Map<String, Zustand> ZUSTAND_NACH_EVENT = Map.of(
            "REPARATUR", Zustand.IN_REPARATUR,
            "ENTSORGUNG", Zustand.ENTSORGT,
            "TEILDEMONTAGE", Zustand.TEILWEISE_BESCHAEDIGT
    );

    private static final Map<String, String> EVENT_TO_MESSAGE = Map.of(
            "RUECKGABE", "moebel-rueckgabe-start",
            "PRUEFUNG", "moebel-pruefung-done",
            "EINLAGERUNG", "moebel-eingelagert",
            "TRANSPORT", "moebel-transport",
            "REPARATUR", "moebel-reparatur-start",
            "ENTSORGUNG", "moebel-entsorgt",
            "TEILDEMONTAGE", "moebel-teildemontage"
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
        if (!VALID_EVENTS.contains(request.eventTyp())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Ungueltiger event_typ '" + request.eventTyp() + "'");
        }

        Moebelstuck moebel = moebelRepo.findByNfcTagId(request.nfcTagId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag nicht angelegt"));

        // Scan-History speichern
        ScanHistory scan = new ScanHistory();
        scan.setMoebelstuckId(moebel.getId());
        scan.setStandortLat(request.standortLat());
        scan.setStandortLng(request.standortLng());
        scan.setStandortName(request.standortName());
        scan.setEventTyp(EventTyp.valueOf(request.eventTyp()));
        scan.setGescanntVon(request.gescanntVon());
        scanRepo.save(scan);

        // Möbelstück aktualisieren
        moebel.setStandortName(request.standortName());
        if (request.standortLat() != null && request.standortLng() != null) {
            moebel.setStandortLat(request.standortLat());
            moebel.setStandortLng(request.standortLng());
        }
        Zustand neuerZustand = ZUSTAND_NACH_EVENT.get(request.eventTyp());
        if (neuerZustand != null) {
            moebel.setZustand(neuerZustand);
        }
        moebelRepo.save(moebel);

        // Camunda-Integration (optional)
        if (camundaClient == null) {
            log.info("Camunda nicht verbunden, Scan ohne Prozess-Integration gespeichert.");
            return moebel;
        }

        try {
            if ("RUECKGABE".equals(request.eventTyp())) {
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
                    camundaClient.newPublishMessageCommand()
                            .messageName(message)
                            .correlationKey(moebel.getId().toString())
                            .variables(Map.of(
                                    "standort", request.standortName(),
                                    "eventTyp", request.eventTyp()
                            ))
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
