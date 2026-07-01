package jku.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoebelstuckTest {

    @Test
    void defaultKategorieIstStandard() {
        Moebelstuck m = new Moebelstuck();
        assertEquals(MoebelKategorie.STANDARD, m.getKategorie());
        assertTrue(m.isStandardmoebel());
    }

    @Test
    void sondermoebelKategorie() {
        Moebelstuck m = new Moebelstuck();
        m.setKategorie(MoebelKategorie.SONDERMOEBEL);
        assertEquals(MoebelKategorie.SONDERMOEBEL, m.getKategorie());
        assertFalse(m.isStandardmoebel());
    }

    @Test
    void defaultZustandIstGut() {
        Moebelstuck m = new Moebelstuck();
        assertEquals(Zustand.GUT, m.getZustand());
    }

    @Test
    void fotoSetztHatFoto() {
        Moebelstuck m = new Moebelstuck();
        assertFalse(m.isHatFoto());

        m.setFoto(new byte[]{1, 2, 3});
        assertTrue(m.isHatFoto());

        m.setFoto(null);
        assertFalse(m.isHatFoto());

        m.setFoto(new byte[0]);
        assertFalse(m.isHatFoto());
    }

    @Test
    void defaultVerfuegbarIstNull() {
        Moebelstuck m = new Moebelstuck();
        assertNull(m.getVerfuegbar());
    }
}
