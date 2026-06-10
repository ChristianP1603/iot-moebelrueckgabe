
package jku.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jku.entity.EventTyp;
import jku.entity.Pruefergebnis;

/**
 * Request-Datensatz für einen eingehenden Möbel-Scan aus dem Frontend.
 *
 * Enthält:
 * - Identifikation des Möbelstücks
 * - Standortdaten
 * - Art des Ereignisses
 * - optionale Zusatzinformationen für den Prozess
 */

public record ScanRequest(
        @NotBlank String nfcTagId,
        Double standortLat,
        Double standortLng,
        @NotBlank String standortName,
        @NotNull EventTyp eventTyp,
        String gescanntVon,
        Pruefergebnis pruefergebnis,
        Boolean ersatzteileVorhanden
) {}
