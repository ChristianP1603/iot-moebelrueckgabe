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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moebelstuck")
public class MoebelstuckController {

    private final MoebelstuckRepository moebelRepo;
    private final ScanHistoryRepository scanRepo;
    private final ProzessInstanzRepository prozessRepo;

    private static final Map<String, MoebelDefaults> STANDARD_DATEN = Map.ofEntries(
            Map.entry("MOEBL-0001", new MoebelDefaults(Zustand.GUT, 48.3363, 14.3194, "Keplergebaeude Buero 301")),
            Map.entry("MOEBL-0002", new MoebelDefaults(Zustand.DEFEKT, 48.3358, 14.3188, "Keplergebaeude Buero 214")),
            Map.entry("MOEBL-0003", new MoebelDefaults(Zustand.GUT, 48.3361, 14.3190, "Keplergebaeude Lager A")),
            Map.entry("MOEBL-0004", new MoebelDefaults(Zustand.IN_REPARATUR, 48.3355, 14.3195, "Keplergebaeude Konferenzraum 1")),
            Map.entry("MOEBL-0005", new MoebelDefaults(Zustand.GUT, 48.3360, 14.3186, "Keplergebaeude Lager B")),
            Map.entry("MOEBL-0006", new MoebelDefaults(Zustand.GUT, 48.3382, 14.3228, "Science Park 1 Buero 105")),
            Map.entry("MOEBL-0007", new MoebelDefaults(Zustand.GUT, 48.3384, 14.3231, "Science Park 1 Buero 108")),
            Map.entry("MOEBL-0008", new MoebelDefaults(Zustand.GUT, 48.3379, 14.3235, "Science Park 2 Labor 201")),
            Map.entry("MOEBL-0009", new MoebelDefaults(Zustand.DEFEKT, 48.3381, 14.3225, "Science Park 2 Lager")),
            Map.entry("MOEBL-0010", new MoebelDefaults(Zustand.GUT, 48.3385, 14.3233, "Science Park 3 Buero 302")),
            Map.entry("MOEBL-0011", new MoebelDefaults(Zustand.GUT, 48.3378, 14.3229, "Science Park 1 Seminarraum 3")),
            Map.entry("MOEBL-0012", new MoebelDefaults(Zustand.GUT, 48.3383, 14.3227, "Science Park 3 Hoersaal")),
            Map.entry("MOEBL-0013", new MoebelDefaults(Zustand.GUT, 48.3372, 14.3218, "TNF-Tower Buero 401")),
            Map.entry("MOEBL-0014", new MoebelDefaults(Zustand.TEILWEISE_BESCHAEDIGT, 48.3374, 14.3220, "TNF-Tower Buero 403")),
            Map.entry("MOEBL-0015", new MoebelDefaults(Zustand.GUT, 48.3370, 14.3216, "TNF-Tower Sekretariat")),
            Map.entry("MOEBL-0016", new MoebelDefaults(Zustand.ENTSORGT, 48.3371, 14.3222, "TNF-Tower Lounge")),
            Map.entry("MOEBL-0017", new MoebelDefaults(Zustand.GUT, 48.3375, 14.3219, "TNF-Tower Buero 405")),
            Map.entry("MOEBL-0018", new MoebelDefaults(Zustand.GUT, 48.3350, 14.3180, "Managementzentrum Sitzungssaal")),
            Map.entry("MOEBL-0019", new MoebelDefaults(Zustand.GUT, 48.3352, 14.3182, "Managementzentrum Buero 102")),
            Map.entry("MOEBL-0020", new MoebelDefaults(Zustand.GUT, 48.3348, 14.3178, "Managementzentrum Bibliothek")),
            Map.entry("MOEBL-0021", new MoebelDefaults(Zustand.DEFEKT, 48.3351, 14.3184, "Managementzentrum Eingang")),
            Map.entry("MOEBL-0022", new MoebelDefaults(Zustand.GUT, 48.3365, 14.3205, "Bibliothek Lesesaal 1")),
            Map.entry("MOEBL-0023", new MoebelDefaults(Zustand.GUT, 48.3367, 14.3207, "Bibliothek Lesesaal 2")),
            Map.entry("MOEBL-0024", new MoebelDefaults(Zustand.GUT, 48.3364, 14.3203, "Bibliothek Magazin")),
            Map.entry("MOEBL-0025", new MoebelDefaults(Zustand.GUT, 48.3370, 14.3205, "Mensa Erdgeschoss")),
            Map.entry("MOEBL-0026", new MoebelDefaults(Zustand.IN_REPARATUR, 48.3369, 14.3208, "Mensa Erdgeschoss")),
            Map.entry("MOEBL-0027", new MoebelDefaults(Zustand.GUT, 48.3366, 14.3210, "UniCenter Lounge")),
            Map.entry("MOEBL-0028", new MoebelDefaults(Zustand.GUT, 48.3368, 14.3212, "UniCenter Lernzone"))
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
        m.setZustand(request.zustand() != null ? request.zustand() : Zustand.GUT);
        m.setPreis(request.preis());
        m.setKaufdatum(request.kaufdatum());
        if (request.kategorie() != null) {
            m.setKategorie(request.kategorie());
        }
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

    @PostMapping("/{id}/foto")
    public Moebelstuck uploadFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Moebelstuck m = moebelRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moebelstueck nicht gefunden"));
        try {
            m.setFoto(file.getBytes());
            return moebelRepo.save(m);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Foto konnte nicht gespeichert werden");
        }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> getFoto(@PathVariable Long id) {
        Moebelstuck m = moebelRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moebelstueck nicht gefunden"));
        if (m.getFoto() == null || m.getFoto().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(m.getFoto());
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
