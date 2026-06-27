package jku.service;

import jku.api.ScanRequest;
import jku.entity.*;
import jku.repository.MoebelstuckRepository;
import jku.repository.ProzessInstanzRepository;
import jku.repository.ScanHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ScanProcessServiceTest {

    @Autowired
    private ScanProcessService service;

    @Autowired
    private MoebelstuckRepository moebelRepo;

    @Autowired
    private ScanHistoryRepository scanRepo;

    @Autowired
    private ProzessInstanzRepository prozessRepo;

    private Moebelstuck testMoebel;

    @BeforeEach
    void setUp() {
        scanRepo.deleteAll();
        prozessRepo.deleteAll();
        moebelRepo.deleteAll();

        testMoebel = new Moebelstuck();
        testMoebel.setNfcTagId("TEST-0001");
        testMoebel.setBezeichnung("Testmoebel");
        testMoebel.setTyp(MoebelTyp.SCHREIBTISCH);
        testMoebel.setZustand(Zustand.GUT);
        testMoebel = moebelRepo.save(testMoebel);
    }

    @Test
    void rueckgabeAendertZustandNicht() {
        ScanRequest req = new ScanRequest("TEST-0001", 48.0, 14.0, "Lager", EventTyp.RUECKGABE, null, null, null);
        Moebelstuck result = service.handleScan(req);
        assertEquals(Zustand.GUT, result.getZustand());
    }

    @Test
    void reparaturSetztZustandInReparatur() {
        ScanRequest req = new ScanRequest("TEST-0001", 48.0, 14.0, "Werkstatt", EventTyp.REPARATUR, null, null, null);
        Moebelstuck result = service.handleScan(req);
        assertEquals(Zustand.IN_REPARATUR, result.getZustand());
    }

    @Test
    void entsorgungSetztZustandEntsorgt() {
        ScanRequest req = new ScanRequest("TEST-0001", 48.0, 14.0, "Container", EventTyp.ENTSORGUNG, null, null, null);
        Moebelstuck result = service.handleScan(req);
        assertEquals(Zustand.ENTSORGT, result.getZustand());
    }

    @Test
    void teildemontageSetztZustandTeilweiseBeschaedigt() {
        ScanRequest req = new ScanRequest("TEST-0001", 48.0, 14.0, "Werkstatt", EventTyp.TEILDEMONTAGE, null, null, null);
        Moebelstuck result = service.handleScan(req);
        assertEquals(Zustand.TEILWEISE_BESCHAEDIGT, result.getZustand());
    }

    @Test
    void einlagerungSetztZustandGut() {
        testMoebel.setZustand(Zustand.DEFEKT);
        moebelRepo.save(testMoebel);

        ScanRequest req = new ScanRequest("TEST-0001", 48.0, 14.0, "Lager", EventTyp.EINLAGERUNG, null, null, null);
        Moebelstuck result = service.handleScan(req);
        assertEquals(Zustand.GUT, result.getZustand());
    }

    @ParameterizedTest
    @CsvSource({
            "GUT, GUT",
            "REPARATUR, IN_REPARATUR",
            "TEILWEISE_BESCHAEDIGT, TEILWEISE_BESCHAEDIGT",
            "SCHLECHT, DEFEKT"
    })
    void pruefungSetztZustandNachPruefergebnis(String pruefergebnis, String erwarteterZustand) {
        ScanRequest req = new ScanRequest("TEST-0001", 48.0, 14.0, "Pruefraum",
                EventTyp.PRUEFUNG, null, Pruefergebnis.valueOf(pruefergebnis), null);
        Moebelstuck result = service.handleScan(req);
        assertEquals(Zustand.valueOf(erwarteterZustand), result.getZustand());
    }

    @Test
    void scanAktualisiertStandort() {
        ScanRequest req = new ScanRequest("TEST-0001", 48.123, 14.456, "Neuer Standort", EventTyp.RUECKGABE, null, null, null);
        Moebelstuck result = service.handleScan(req);
        assertEquals("Neuer Standort", result.getStandortName());
        assertEquals(48.123, result.getStandortLat());
        assertEquals(14.456, result.getStandortLng());
    }

    @Test
    void scanErstelltHistoryEintrag() {
        ScanRequest req = new ScanRequest("TEST-0001", 48.0, 14.0, "Lager", EventTyp.PRUEFUNG, "Max", Pruefergebnis.GUT, null);
        service.handleScan(req);

        var history = scanRepo.findByMoebelstuckIdOrderByZeitstempelAsc(testMoebel.getId());
        assertEquals(1, history.size());
        assertEquals(EventTyp.PRUEFUNG, history.get(0).getEventTyp());
        assertEquals(Pruefergebnis.GUT, history.get(0).getPruefergebnis());
        assertEquals("Max", history.get(0).getGescanntVon());
    }

    @Test
    void unbekannterTagWirftNotFound() {
        ScanRequest req = new ScanRequest("GIBTS-NICHT", 48.0, 14.0, "Lager", EventTyp.RUECKGABE, null, null, null);
        assertThrows(ResponseStatusException.class, () -> service.handleScan(req));
    }

    @Test
    void standardmoebelKompletterprozess_GUT() {
        scan(EventTyp.RUECKGABE, null);
        assertEquals(Zustand.GUT, aktuellerZustand());

        scan(EventTyp.PRUEFUNG, Pruefergebnis.GUT);
        assertEquals(Zustand.GUT, aktuellerZustand());

        scan(EventTyp.EINLAGERUNG, null);
        assertEquals(Zustand.GUT, aktuellerZustand());
    }

    @Test
    void standardmoebelKompletterprozess_REPARATUR() {
        scan(EventTyp.RUECKGABE, null);
        scan(EventTyp.PRUEFUNG, Pruefergebnis.REPARATUR);
        assertEquals(Zustand.IN_REPARATUR, aktuellerZustand());

        scan(EventTyp.REPARATUR, null);
        assertEquals(Zustand.IN_REPARATUR, aktuellerZustand());

        scan(EventTyp.EINLAGERUNG, null);
        assertEquals(Zustand.GUT, aktuellerZustand());
    }

    @Test
    void standardmoebelKompletterprozess_TEILWEISE_BESCHAEDIGT() {
        scan(EventTyp.RUECKGABE, null);
        scan(EventTyp.PRUEFUNG, Pruefergebnis.TEILWEISE_BESCHAEDIGT);
        assertEquals(Zustand.TEILWEISE_BESCHAEDIGT, aktuellerZustand());

        scan(EventTyp.TEILDEMONTAGE, null);
        assertEquals(Zustand.TEILWEISE_BESCHAEDIGT, aktuellerZustand());
    }

    @Test
    void standardmoebelKompletterprozess_SCHLECHT() {
        scan(EventTyp.RUECKGABE, null);
        scan(EventTyp.PRUEFUNG, Pruefergebnis.SCHLECHT);
        assertEquals(Zustand.DEFEKT, aktuellerZustand());

        scan(EventTyp.ENTSORGUNG, null);
        assertEquals(Zustand.ENTSORGT, aktuellerZustand());
    }

    @Test
    void sondermoebelKompletterprozess_GUT() {
        testMoebel.setKategorie(MoebelKategorie.SONDERMOEBEL);
        moebelRepo.save(testMoebel);

        scan(EventTyp.RUECKGABE, null);
        assertEquals(Zustand.GUT, aktuellerZustand());
        assertFalse(testMoebel.isStandardmoebel());

        scan(EventTyp.PRUEFUNG, Pruefergebnis.GUT);
        assertEquals(Zustand.GUT, aktuellerZustand());

        scan(EventTyp.EINLAGERUNG, null);
        assertEquals(Zustand.GUT, aktuellerZustand());
    }

    @Test
    void sondermoebelKompletterprozess_SCHLECHT() {
        testMoebel.setKategorie(MoebelKategorie.SONDERMOEBEL);
        moebelRepo.save(testMoebel);

        scan(EventTyp.RUECKGABE, null);
        scan(EventTyp.PRUEFUNG, Pruefergebnis.SCHLECHT);
        assertEquals(Zustand.DEFEKT, aktuellerZustand());

        scan(EventTyp.ENTSORGUNG, null);
        assertEquals(Zustand.ENTSORGT, aktuellerZustand());
    }

    private void scan(EventTyp eventTyp, Pruefergebnis pruefergebnis) {
        ScanRequest req = new ScanRequest("TEST-0001", 48.0, 14.0, "Standort", eventTyp, null, pruefergebnis, null);
        service.handleScan(req);
    }

    private Zustand aktuellerZustand() {
        return moebelRepo.findByNfcTagId("TEST-0001").orElseThrow().getZustand();
    }
}
