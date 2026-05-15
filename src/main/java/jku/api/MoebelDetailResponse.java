package jku.api;

import jku.entity.Moebelstuck;
import jku.entity.ProzessInstanz;
import jku.entity.ScanHistory;

import java.util.List;

public record MoebelDetailResponse(
        Long id,
        String nfcTagId,
        String bezeichnung,
        String typ,
        String zustand,
        Double standortLat,
        Double standortLng,
        String standortName,
        Double preis,
        String kaufdatum,
        boolean hatFoto,
        List<ScanHistory> scanHistory,
        List<ProzessInstanz> prozessInstanzen
) {
    public static MoebelDetailResponse from(Moebelstuck m, List<ScanHistory> scans, List<ProzessInstanz> prozesse) {
        return new MoebelDetailResponse(
                m.getId(),
                m.getNfcTagId(),
                m.getBezeichnung(),
                m.getTyp(),
                m.getZustand().name(),
                m.getStandortLat(),
                m.getStandortLng(),
                m.getStandortName(),
                m.getPreis(),
                m.getKaufdatum() != null ? m.getKaufdatum().toString() : null,
                m.isHatFoto(),
                scans,
                prozesse
        );
    }
}
