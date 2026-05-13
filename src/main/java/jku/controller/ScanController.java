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
                "nfcTagId", m.getNfcTagId(),
                "bezeichnung", m.getBezeichnung(),
                "typ", m.getTyp(),
                "zustand", m.getZustand().name(),
                "standortName", m.getStandortName() != null ? m.getStandortName() : "",
                "standortLat", m.getStandortLat() != null ? m.getStandortLat() : 0,
                "standortLng", m.getStandortLng() != null ? m.getStandortLng() : 0
        );
    }
}
