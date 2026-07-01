package jku.api;

import jku.entity.MoebelKategorie;
import jku.entity.MoebelTyp;
import jku.entity.Moebelstuck;
import jku.entity.ProzessInstanz;
import jku.entity.ScanHistory;
import jku.entity.Zustand;

import java.util.List;

public record MoebelDetailResponse(
        Long id,
        String nfcTagId,
        String bezeichnung,
        MoebelTyp typ,
        Zustand zustand,
        MoebelKategorie kategorie,
        Double standortLat,
        Double standortLng,
        String standortName,
        Double preis,
        String kaufdatum,
        boolean hatFoto,
        String kommentar,
        String nutzer,
        String eigentuemer,
        String reserviertVon,
        String reserviertBis,
        String reserviertFuer,
        Boolean verfuegbar,
        boolean entfernt,
        List<ScanHistory> scanHistory,
        List<ProzessInstanz> prozessInstanzen
) {
    public static MoebelDetailResponse from(Moebelstuck m, List<ScanHistory> scans, List<ProzessInstanz> prozesse) {
        return new MoebelDetailResponse(
                m.getId(),
                m.getNfcTagId(),
                m.getBezeichnung(),
                m.getTyp(),
                m.getZustand(),
                m.getKategorie(),
                m.getStandortLat(),
                m.getStandortLng(),
                m.getStandortName(),
                m.getPreis(),
                m.getKaufdatum() != null ? m.getKaufdatum().toString() : null,
                m.isHatFoto(),
                m.getKommentar(),
                m.getNutzer(),
                m.getEigentuemer(),
                m.getReserviertVon() != null ? m.getReserviertVon().toString() : null,
                m.getReserviertBis() != null ? m.getReserviertBis().toString() : null,
                m.getReserviertFuer(),
                m.getVerfuegbar(),
                m.isEntfernt(),
                scans,
                prozesse
        );
    }
}
