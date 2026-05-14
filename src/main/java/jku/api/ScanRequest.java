package jku.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jku.entity.EventTyp;

public record ScanRequest(
        @NotBlank String nfcTagId,
        Double standortLat,
        Double standortLng,
        @NotBlank String standortName,
        @NotNull EventTyp eventTyp,
        String gescanntVon
) {}
