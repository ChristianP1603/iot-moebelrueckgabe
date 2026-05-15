package jku;

import jku.entity.Moebelstuck;
import jku.entity.MoebelTyp;
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

        // Keplergebaeude
        seed("MOEBL-0001", "Schreibtisch Modell A", MoebelTyp.SCHREIBTISCH, Zustand.GUT,
                48.33630, 14.31940, "Keplergebaeude Buero 301", 249.99, LocalDate.of(2023, 3, 15));
        seed("MOEBL-0002", "Drehstuhl Standard", MoebelTyp.STUHL, Zustand.DEFEKT,
                48.33580, 14.31880, "Keplergebaeude Buero 214", 189.50, LocalDate.of(2022, 11, 20));
        seed("MOEBL-0003", "Rollcontainer 3-Laden", MoebelTyp.ROLLCONTAINER, Zustand.GUT,
                48.33610, 14.31900, "Keplergebaeude Lager A", 129.00, LocalDate.of(2024, 1, 10));
        seed("MOEBL-0004", "Besprechungstisch gross", MoebelTyp.TISCH, Zustand.IN_REPARATUR,
                48.33550, 14.31950, "Keplergebaeude Konferenzraum 1", 449.00, LocalDate.of(2021, 6, 5));
        seed("MOEBL-0005", "Aktenschrank 2-tuerig", MoebelTyp.AKTENSCHRANK, Zustand.GUT,
                48.33600, 14.31860, "Keplergebaeude Lager B", 319.00, LocalDate.of(2023, 9, 1));

        // Science Park
        seed("MOEBL-0006", "Stehschreibtisch elektrisch", MoebelTyp.STEHPULT, Zustand.GUT,
                48.33820, 14.32280, "Science Park 1 Buero 105", 599.00, LocalDate.of(2024, 2, 12));
        seed("MOEBL-0007", "Ergonomiestuhl Premium", MoebelTyp.STUHL, Zustand.GUT,
                48.33840, 14.32310, "Science Park 1 Buero 108", 429.00, LocalDate.of(2023, 7, 22));
        seed("MOEBL-0008", "Labortisch breit", MoebelTyp.TISCH, Zustand.GUT,
                48.33790, 14.32350, "Science Park 2 Labor 201", 680.00, LocalDate.of(2022, 4, 18));
        seed("MOEBL-0009", "Regal 5-Boeden Metall", MoebelTyp.REGAL, Zustand.DEFEKT,
                48.33810, 14.32250, "Science Park 2 Lager", 159.00, LocalDate.of(2021, 10, 3));
        seed("MOEBL-0010", "Schreibtischlampe LED", MoebelTyp.LAMPE, Zustand.GUT,
                48.33850, 14.32330, "Science Park 3 Buero 302", 89.00, LocalDate.of(2024, 5, 8));
        seed("MOEBL-0011", "Whiteboard magnetisch 200x120", MoebelTyp.WHITEBOARD, Zustand.GUT,
                48.33780, 14.32290, "Science Park 1 Seminarraum 3", 345.00, LocalDate.of(2023, 1, 14));
        seed("MOEBL-0012", "Konferenzstuhl stapelbar", MoebelTyp.STUHL, Zustand.GUT,
                48.33830, 14.32270, "Science Park 3 Hoersaal", 125.00, LocalDate.of(2022, 8, 30));

        // TNF-Tower
        seed("MOEBL-0013", "Schreibtisch Modell C", MoebelTyp.SCHREIBTISCH, Zustand.GUT,
                48.33720, 14.32180, "TNF-Tower Buero 401", 279.00, LocalDate.of(2023, 11, 5));
        seed("MOEBL-0014", "Buerostuhl Standard", MoebelTyp.STUHL, Zustand.TEILWEISE_BESCHAEDIGT,
                48.33740, 14.32200, "TNF-Tower Buero 403", 195.00, LocalDate.of(2021, 3, 20));
        seed("MOEBL-0015", "Sideboard Eiche", MoebelTyp.SIDEBOARD, Zustand.GUT,
                48.33700, 14.32160, "TNF-Tower Sekretariat", 410.00, LocalDate.of(2022, 6, 11));
        seed("MOEBL-0016", "Stehlampe Bogen", MoebelTyp.LAMPE, Zustand.ENTSORGT,
                48.33710, 14.32220, "TNF-Tower Lounge", 179.00, LocalDate.of(2020, 9, 15));
        seed("MOEBL-0017", "Rollcontainer 2-Laden", MoebelTyp.ROLLCONTAINER, Zustand.GUT,
                48.33750, 14.32190, "TNF-Tower Buero 405", 99.00, LocalDate.of(2024, 3, 1));

        // Managementzentrum
        seed("MOEBL-0018", "Konferenztisch oval", MoebelTyp.KONFERENZTISCH, Zustand.GUT,
                48.33500, 14.31800, "Managementzentrum Sitzungssaal", 890.00, LocalDate.of(2023, 5, 20));
        seed("MOEBL-0019", "Chefsessel Leder", MoebelTyp.SESSEL, Zustand.GUT,
                48.33520, 14.31820, "Managementzentrum Buero 102", 650.00, LocalDate.of(2022, 12, 8));
        seed("MOEBL-0020", "Regal Buche 4-Boeden", MoebelTyp.REGAL, Zustand.GUT,
                48.33480, 14.31780, "Managementzentrum Bibliothek", 215.00, LocalDate.of(2021, 7, 14));
        seed("MOEBL-0021", "Garderobe Metall 6-Haken", MoebelTyp.GARDEROBE, Zustand.DEFEKT,
                48.33510, 14.31840, "Managementzentrum Eingang", 135.00, LocalDate.of(2023, 2, 28));

        // Bibliothek
        seed("MOEBL-0022", "Lesetisch gross", MoebelTyp.TISCH, Zustand.GUT,
                48.33650, 14.32050, "Bibliothek Lesesaal 1", 520.00, LocalDate.of(2022, 1, 19));
        seed("MOEBL-0023", "Stuhl Bibliothek gepolstert", MoebelTyp.STUHL, Zustand.GUT,
                48.33670, 14.32070, "Bibliothek Lesesaal 2", 165.00, LocalDate.of(2023, 4, 10));
        seed("MOEBL-0024", "Buecherregal Stahl", MoebelTyp.REGAL, Zustand.GUT,
                48.33640, 14.32030, "Bibliothek Magazin", 280.00, LocalDate.of(2021, 11, 25));

        // Mensa / UniCenter
        seed("MOEBL-0025", "Kantinentisch klappbar", MoebelTyp.TISCH, Zustand.GUT,
                48.33700, 14.32050, "Mensa Erdgeschoss", 310.00, LocalDate.of(2023, 8, 5));
        seed("MOEBL-0026", "Kantinenstuhl Kunststoff", MoebelTyp.STUHL, Zustand.IN_REPARATUR,
                48.33690, 14.32080, "Mensa Erdgeschoss", 75.00, LocalDate.of(2022, 5, 17));
        seed("MOEBL-0027", "Sofa 3-Sitzer grau", MoebelTyp.SOFA, Zustand.GUT,
                48.33660, 14.32100, "UniCenter Lounge", 780.00, LocalDate.of(2024, 1, 22));
        seed("MOEBL-0028", "Schreibtisch kompakt", MoebelTyp.SCHREIBTISCH, Zustand.GUT,
                48.33680, 14.32120, "UniCenter Lernzone", 199.00, LocalDate.of(2023, 10, 30));

        log.info("{} Moebelstuecke eingefuegt.", repo.count());
    }

    private void seed(String nfcTagId, String bezeichnung, MoebelTyp typ, Zustand zustand,
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
