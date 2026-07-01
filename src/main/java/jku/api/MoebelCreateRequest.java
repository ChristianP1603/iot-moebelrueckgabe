package jku.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jku.entity.MoebelKategorie;
import jku.entity.MoebelTyp;
import jku.entity.Zustand;
import java.time.LocalDate;

public record MoebelCreateRequest(
        @NotBlank String nfcTagId,
        @NotBlank String bezeichnung,
        @NotNull MoebelTyp typ,
        Double preis,
        LocalDate kaufdatum,
        Zustand zustand,
        MoebelKategorie kategorie,
        String kommentar,
        String nutzer,
        String eigentuemer,
        LocalDate reserviertVon,
        LocalDate reserviertBis,
        String reserviertFuer
) {}
