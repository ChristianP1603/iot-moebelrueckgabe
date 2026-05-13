package jku.api;

import jku.entity.Moebelstuck;
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
        List<ScanHistory> scanHistory
) {
    public static MoebelDetailResponse from(Moebelstuck m, List<ScanHistory> scans) {
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
                scans
        );
    }
}
