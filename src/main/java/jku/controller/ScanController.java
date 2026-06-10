package jku.controller;

import jakarta.validation.Valid;
import jku.api.ScanRequest;
import jku.entity.Moebelstuck;
import jku.repository.MoebelstuckRepository;
import jku.service.ScanProcessService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final ScanProcessService scanProcessService;
    private final MoebelstuckRepository moebelRepo;

    public ScanController(ScanProcessService scanProcessService, MoebelstuckRepository moebelRepo) {
        this.scanProcessService = scanProcessService;
        this.moebelRepo = moebelRepo;
    }

    @PostMapping("/scan")
    @ResponseStatus(HttpStatus.CREATED)
    public Moebelstuck createScan(@Valid @RequestBody ScanRequest request) {
        return scanProcessService.handleScan(request);
    }

    @GetMapping("/scan/{nfcTagId}")
    public Map<String, Object> getByTag(@PathVariable String nfcTagId) {
        Optional<Moebelstuck> opt = moebelRepo.findByNfcTagId(nfcTagId);
        if (opt.isEmpty()) {
            return Map.of("status", "unbekannt");
        }
        Moebelstuck m = opt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("status", "gefunden");
        result.put("id", m.getId());
        result.put("nfc_tag_id", m.getNfcTagId());
        result.put("bezeichnung", m.getBezeichnung());
        result.put("typ", m.getTyp());
        result.put("zustand", m.getZustand());
        result.put("kategorie", m.getKategorie());
        result.put("standort_name", m.getStandortName() != null ? m.getStandortName() : "");
        result.put("standort_lat", m.getStandortLat() != null ? m.getStandortLat() : 0);
        result.put("standort_lng", m.getStandortLng() != null ? m.getStandortLng() : 0);
        return result;
    }
}
