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

/**
 * Verarbeitet eingehende Scan-Anfragen aus dem Frontend.
 *
 * Aufgaben - App/DB->Camunda-Prozess:
 * - Möbelstück anhand der NFC-Tag-ID aus der Datenbank laden
 * - Prüfergebnis aus Request oder aus dem aktuellen Möbel-Zustand ableiten
 * - Scan-History speichern
 * - Standort und Zustand des Möbelstücks aktualisieren
 * - bei Rückgabe eine neue Camunda-Prozessinstanz starten
 * - bei Folgeereignissen eine Camunda-Message an eine bestehende Instanz senden
 *
 * Verwendete Prozessvariablen:
 * - moebelId
 * - nfcTagId
 * - standort
 * - pruefergebnis
 * - ersatzteileVorhanden
 *
 *  Referenz
 *  https://docs.camunda.io/docs/reference/glossary/#process-instance
 * https://docs.camunda.io/docs/components/modeler/bpmn/message-events
 * https://docs.camunda.io/docs/apis-tools/orchestration-cluster-api-rest/specifications/publish-message
 *
 */

@Service
public class ScanProcessService {

    // Logger für Konsolen-/Serverausgaben
    private static final Logger log = LoggerFactory.getLogger(ScanProcessService.class);

    // BPMN Process ID des Hauptprozesses in Camunda
    private static final String BPMN_PROCESS_ID = "moebelrueckgabe-standard";

    // Zuordnung: Event -> neuer Möbel-Zustand
    private static final Map<EventTyp, Zustand> ZUSTAND_NACH_EVENT = Map.of(
            EventTyp.REPARATUR, Zustand.IN_REPARATUR,
            EventTyp.ENTSORGUNG, Zustand.ENTSORGT,
            EventTyp.TEILDEMONTAGE, Zustand.TEILWEISE_BESCHAEDIGT,
            EventTyp.EINLAGERUNG, Zustand.GUT
    );

    // Zuordnung: Prüfergebnis -> neuer Möbel-Zustand
    private static final Map<Pruefergebnis, Zustand> ZUSTAND_NACH_PRUEFUNG = Map.of(
            Pruefergebnis.GUT, Zustand.GUT,
            Pruefergebnis.REPARATUR, Zustand.IN_REPARATUR,
            Pruefergebnis.TEILWEISE_BESCHAEDIGT, Zustand.TEILWEISE_BESCHAEDIGT,
            Pruefergebnis.SCHLECHT, Zustand.DEFEKT
    );

    // Zuordnung: aktueller Möbel-Zustand -> passendes Prüfergebnis
    private static final Map<Zustand, Pruefergebnis> PRUEFERGEBNIS_NACH_ZUSTAND = Map.of(
        Zustand.GUT, Pruefergebnis.GUT,
        Zustand.IN_REPARATUR, Pruefergebnis.REPARATUR,
        Zustand.TEILWEISE_BESCHAEDIGT, Pruefergebnis.TEILWEISE_BESCHAEDIGT,
        Zustand.DEFEKT, Pruefergebnis.SCHLECHT
    );

    // Zuordnung: Event -> Camunda Message Name
    private static final Map<EventTyp, String> EVENT_TO_MESSAGE = Map.of(
            EventTyp.RUECKGABE, "moebel-rueckgabe-start",
            EventTyp.PRUEFUNG, "moebel-pruefung-done",
            EventTyp.EINLAGERUNG, "moebel-eingelagert",
            EventTyp.REPARATUR, "moebel-reparatur-start",
            EventTyp.ENTSORGUNG, "moebel-entsorgt",
            EventTyp.TEILDEMONTAGE, "moebel-teildemontage"
    );

    // Camunda-Client für Prozessstart / Message-Versand
    @Autowired(required = false)
    private CamundaClient camundaClient;

    // Repositories für DB-Zugriffe
    private final MoebelstuckRepository moebelRepo;
    private final ScanHistoryRepository scanRepo;
    private final ProzessInstanzRepository prozessRepo;

    // Konstruktor: Repositories werden übergeben
    public ScanProcessService(MoebelstuckRepository moebelRepo,
                              ScanHistoryRepository scanRepo,
                              ProzessInstanzRepository prozessRepo) {
        this.moebelRepo = moebelRepo;
        this.scanRepo = scanRepo;
        this.prozessRepo = prozessRepo;
    }

    // Hauptmethode: verarbeitet einen Scan aus dem Frontend
    public Moebelstuck handleScan(ScanRequest request) {

        // Möbelstück anhand der NFC-Tag-ID suchen
        Moebelstuck moebel = moebelRepo.findByNfcTagId(request.nfcTagId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag nicht angelegt"));

        // Prüfergebnis aus Request holen
        Pruefergebnis pruefergebnis = request.pruefergebnis();

        // Falls Rückgabe und kein Prüfergebnis mitgeschickt:
        // Prüfergebnis aus aktuellem Möbel-Zustand ableiten
        if (request.eventTyp() == EventTyp.RUECKGABE && pruefergebnis == null) {
            pruefergebnis = PRUEFERGEBNIS_NACH_ZUSTAND.get(moebel.getZustand());
            log.info("Pruefergebnis fuer Rueckgabe aus Zustand abgeleitet: Zustand={}, pruefergebnis={}",
                    moebel.getZustand(), pruefergebnis);
        }

        // Neuer Scan-History-Eintrag wird erzeugt
        ScanHistory scan = new ScanHistory();
        scan.setMoebelstuckId(moebel.getId());
        scan.setStandortLat(request.standortLat());
        scan.setStandortLng(request.standortLng());
        scan.setStandortName(request.standortName());
        scan.setEventTyp(request.eventTyp());
        scan.setGescanntVon(request.gescanntVon());
        scan.setPruefergebnis(pruefergebnis);
        scan.setErsatzteileVorhanden(request.ersatzteileVorhanden());

        // Scan-History in DB speichern
        scanRepo.save(scan);

        // Standort des Möbelstücks aktualisieren
        moebel.setStandortName(request.standortName());

        // GPS-Koordinaten nur setzen, wenn vorhanden
        if (request.standortLat() != null && request.standortLng() != null) {
            moebel.setStandortLat(request.standortLat());
            moebel.setStandortLng(request.standortLng());
        }

        // Wenn Prüfergebnis vorhanden:
        // Möbel-Zustand aus Prüfergebnis ableiten
        if (pruefergebnis != null) {
            moebel.setZustand(ZUSTAND_NACH_PRUEFUNG.get(pruefergebnis));
        } else {
            // Sonst Möbel-Zustand aus Event ableiten
            Zustand neuerZustand = ZUSTAND_NACH_EVENT.get(request.eventTyp());
            if (neuerZustand != null) {
                moebel.setZustand(neuerZustand);
            }
        }

        // Möbelstück in DB speichern
        moebelRepo.save(moebel);

        // Wenn kein Camunda-Client vorhanden:
        // nur DB speichern, kein Prozessstart
        if (camundaClient == null) {
            log.info("Camunda nicht verbunden, Scan ohne Prozess-Integration gespeichert.");
            return moebel;
        }

        try {
            // Fall 1: Rückgabe -> neue Prozessinstanz wird erzeugt
            if (request.eventTyp() == EventTyp.RUECKGABE) {

                // Variablen für Camunda-Prozess zusammenbauen
                Map<String, Object> vars = new java.util.HashMap<>();
                vars.put("moebelId", moebel.getId().toString());
                vars.put("nfcTagId", moebel.getNfcTagId());
                vars.put("standort", request.standortName());

                // Prüfergebnis nur mitgeben, wenn vorhanden
                if (pruefergebnis != null) {
                    vars.put("pruefergebnis", pruefergebnis.name());
                }

                // Ersatzteile nur mitgeben, wenn vorhanden
                if (request.ersatzteileVorhanden() != null) {
                    vars.put("ersatzteileVorhanden", request.ersatzteileVorhanden());
                }

                vars.put("standardmoebel", moebel.isStandardmoebel());

                // Neue Camunda-Prozessinstanz starten
                ProcessInstanceEvent event = camundaClient
                        .newCreateInstanceCommand()
                        .bpmnProcessId(BPMN_PROCESS_ID)
                        .latestVersion()
                        .variables(vars)
                        .execute();                

                // Prozessinstanz auch lokal in DB protokollieren
                ProzessInstanz pi = new ProzessInstanz();
                pi.setMoebelstuckId(moebel.getId());
                pi.setCamundaInstanceKey(event.getProcessInstanceKey());
                pi.setStatus("GESTARTET");
                prozessRepo.save(pi);

                // Log-Ausgabe: Prozess gestartet
                log.info("Camunda Prozess gestartet: {}", event.getProcessInstanceKey());
            } else {
                // Fall 2: anderes Event -> Message an laufenden Prozess senden
                String message = EVENT_TO_MESSAGE.get(request.eventTyp());

                // Nur senden, wenn Message für Event definiert ist
                if (message != null) {

                    // Variablen für Message zusammenbauen
                    Map<String, Object> vars = new java.util.HashMap<>(Map.of(
                            "standort", request.standortName(),
                            "eventTyp", request.eventTyp().name()
                    ));

                    // Prüfergebnis optional mitgeben
                    if (pruefergebnis != null) {
                        vars.put("pruefergebnis", pruefergebnis.name());
                    }
                    if (request.ersatzteileVorhanden() != null) {
                        vars.put("ersatzteileVorhanden", request.ersatzteileVorhanden());
                    }

                    // Camunda Message veröffentlichen
                    // correlationKey = Möbel-ID zur Zuordnung zur richtigen Prozessinstanz
                    camundaClient.newPublishMessageCommand()
                            .messageName(message)
                            .correlationKey(moebel.getId().toString())
                            .variables(vars)
                            .send()
                            .join();

                    // Log-Ausgabe: Message wurde korreliert
                    log.info("Camunda Message '{}' korreliert fuer {}", message, moebel.getId());
                }
            }
        } catch (Exception e) {
            // Fehler bei Camunda nur loggen, Scan bleibt trotzdem gespeichert
            log.warn("Camunda-Anbindung fehlgeschlagen (wird ignoriert): {}", e.getMessage());
        }

        // Aktualisiertes Möbelstück zurückgeben
        return moebel;
    }
}
