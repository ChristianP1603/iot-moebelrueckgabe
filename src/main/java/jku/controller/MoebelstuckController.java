package jku.controller;

import jakarta.validation.Valid;
import jku.api.MoebelCreateRequest;
import jku.api.MoebelDetailResponse;
import jku.entity.Moebelstuck;
import jku.entity.ProzessInstanz;
import jku.entity.ScanHistory;
import jku.entity.Zustand;
import jku.repository.MoebelstuckRepository;
import jku.repository.ProzessInstanzRepository;
import jku.repository.ScanHistoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moebelstuck")
public class MoebelstuckController {

    private final MoebelstuckRepository moebelRepo;
    private final ScanHistoryRepository scanRepo;
    private final ProzessInstanzRepository prozessRepo;

    private static final Map<String, MoebelDefaults> STANDARD_DATEN = Map.of(
            "MOEBL-0001", new MoebelDefaults(Zustand.GUT, 48.3363, 14.3194, "Buero 301"),
            "MOEBL-0002", new MoebelDefaults(Zustand.DEFEKT, 48.3358, 14.3188, "Buero 214"),
            "MOEBL-0003", new MoebelDefaults(Zustand.GUT, 48.3371, 14.3201, "Lager A"),
            "MOEBL-0004", new MoebelDefaults(Zustand.IN_REPARATUR, 48.3355, 14.3210, "Konferenzraum 1"),
            "MOEBL-0005", new MoebelDefaults(Zustand.GUT, 48.3368, 14.3179, "Lager B")
    );

    public MoebelstuckController(MoebelstuckRepository moebelRepo, ScanHistoryRepository scanRepo,
                                 ProzessInstanzRepository prozessRepo) {
        this.moebelRepo = moebelRepo;
        this.scanRepo = scanRepo;
        this.prozessRepo = prozessRepo;
    }

    @GetMapping
    public List<Moebelstuck> listAll() {
        return moebelRepo.findAll();
    }

    @GetMapping("/{id}")
    public MoebelDetailResponse getById(@PathVariable Long id) {
        Moebelstuck m = moebelRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moebelstueck nicht gefunden"));
        List<ScanHistory> scans = scanRepo.findByMoebelstuckIdOrderByZeitstempelAsc(id);
        List<ProzessInstanz> prozesse = prozessRepo.findByMoebelstuckId(id);
        return MoebelDetailResponse.from(m, scans, prozesse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Moebelstuck create(@Valid @RequestBody MoebelCreateRequest request) {
        if (moebelRepo.findByNfcTagId(request.nfcTagId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "NFC-Tag-ID bereits vergeben");
        }
        Moebelstuck m = new Moebelstuck();
        m.setNfcTagId(request.nfcTagId());
        m.setBezeichnung(request.bezeichnung());
        m.setTyp(request.typ());
        m.setZustand(request.zustand() != null ? Zustand.valueOf(request.zustand()) : Zustand.GUT);
        m.setPreis(request.preis());
        m.setKaufdatum(request.kaufdatum());
        return moebelRepo.save(m);
    }

    @PostMapping("/reset")
    @Transactional
    public Map<String, Object> resetAll() {
        List<Moebelstuck> alle = moebelRepo.findAll();
        int count = 0;
        for (Moebelstuck m : alle) {
            MoebelDefaults defaults = STANDARD_DATEN.get(m.getNfcTagId());
            if (defaults != null) {
                defaults.applyTo(m);
                moebelRepo.save(m);
                scanRepo.deleteByMoebelstuckId(m.getId());
                count++;
            }
        }
        return Map.of("count", count, "message", count + " Moebelstuecke zurueckgesetzt");
    }

    @PostMapping("/{id}/reset")
    @Transactional
    public Moebelstuck resetSingle(@PathVariable Long id) {
        Moebelstuck m = moebelRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moebelstueck nicht gefunden"));
        MoebelDefaults defaults = STANDARD_DATEN.get(m.getNfcTagId());
        if (defaults == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keine Standarddaten fuer dieses Moebelstueck");
        }
        defaults.applyTo(m);
        moebelRepo.save(m);
        scanRepo.deleteByMoebelstuckId(id);
        return m;
    }

    private record MoebelDefaults(Zustand zustand, double lat, double lng, String standortName) {
        void applyTo(Moebelstuck m) {
            m.setZustand(zustand);
            m.setStandortLat(lat);
            m.setStandortLng(lng);
            m.setStandortName(standortName);
        }
    }
}
