package jku.controller;

import jakarta.validation.Valid;
import jku.api.ScanRequest;
import jku.entity.Moebelstuck;
import jku.repository.MoebelstuckRepository;
import jku.service.ScanProcessService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
        return Map.of(
                "status", "gefunden",
                "id", m.getId(),
                "nfc_tag_id", m.getNfcTagId(),
                "bezeichnung", m.getBezeichnung(),
                "typ", m.getTyp(),
                "zustand", m.getZustand(),
                "standort_name", m.getStandortName() != null ? m.getStandortName() : "",
                "standort_lat", m.getStandortLat() != null ? m.getStandortLat() : 0,
                "standort_lng", m.getStandortLng() != null ? m.getStandortLng() : 0
        );
    }
}
