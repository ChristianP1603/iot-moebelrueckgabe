package jku;

import jku.entity.Moebelstuck;
import jku.entity.Zustand;
import jku.repository.MoebelstuckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Order(2)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MoebelstuckRepository repo;

    public DataSeeder(MoebelstuckRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repo.count() > 0) {
            log.info("Datenbank enthaelt bereits {} Moebelstuecke, kein Seeding noetig.", repo.count());
            return;
        }

        log.info("Seed-Daten werden eingefuegt...");

        seed("MOEBL-0001", "Schreibtisch Modell A", "Schreibtisch", Zustand.GUT,
                48.3363, 14.3194, "Buero 301", 249.99, LocalDate.of(2023, 3, 15));

        seed("MOEBL-0002", "Drehstuhl Standard", "Stuhl", Zustand.DEFEKT,
                48.3358, 14.3188, "Buero 214", 189.50, LocalDate.of(2022, 11, 20));

        seed("MOEBL-0003", "Rollcontainer 3-Laden", "Rollcontainer", Zustand.GUT,
                48.3371, 14.3201, "Lager A", 129.00, LocalDate.of(2024, 1, 10));

        seed("MOEBL-0004", "Besprechungstisch gross", "Tisch", Zustand.IN_REPARATUR,
                48.3355, 14.3210, "Konferenzraum 1", 449.00, LocalDate.of(2021, 6, 5));

        seed("MOEBL-0005", "Aktenschrank 2-turig", "Schrank", Zustand.GUT,
                48.3368, 14.3179, "Lager B", 319.00, LocalDate.of(2023, 9, 1));

        log.info("5 Moebelstuecke eingefuegt.");
    }

    private void seed(String nfcTagId, String bezeichnung, String typ, Zustand zustand,
                      double lat, double lng, String standort, double preis, LocalDate kaufdatum) {
        Moebelstuck m = new Moebelstuck();
        m.setNfcTagId(nfcTagId);
        m.setBezeichnung(bezeichnung);
        m.setTyp(typ);
        m.setZustand(zustand);
        m.setStandortLat(lat);
        m.setStandortLng(lng);
        m.setStandortName(standort);
        m.setPreis(preis);
        m.setKaufdatum(kaufdatum);
        repo.save(m);
    }
}
