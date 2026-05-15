package jku.api;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record MoebelCreateRequest(
        @NotBlank String nfcTagId,
        @NotBlank String bezeichnung,
        @NotBlank String typ,
        Double preis,
        LocalDate kaufdatum,
        String zustand,
        Boolean standardmoebel
) {}
