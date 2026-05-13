package jku.api;

import jakarta.validation.constraints.NotBlank;

public record ScanRequest(
        @NotBlank String nfcTagId,
        Double standortLat,
        Double standortLng,
        @NotBlank String standortName,
        @NotBlank String eventTyp,
        String gescanntVon
) {}
