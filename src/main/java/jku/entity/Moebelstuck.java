package jku.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "moebelstuck")
public class Moebelstuck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nfcTagId;

    @Column(nullable = false)
    private String bezeichnung;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoebelTyp typ;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Zustand zustand = Zustand.GUT;

    private Double standortLat;
    private Double standortLng;
    private String standortName;
    private Double preis;
    private LocalDate kaufdatum;

    @Column(nullable = false)
    private boolean standardmoebel = true;

    @Column(columnDefinition = "bytea")
    @JsonIgnore
    private byte[] foto;

    private boolean hatFoto;

    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }

    public String getNfcTagId() { return nfcTagId; }
    public void setNfcTagId(String nfcTagId) { this.nfcTagId = nfcTagId; }

    public String getBezeichnung() { return bezeichnung; }
    public void setBezeichnung(String bezeichnung) { this.bezeichnung = bezeichnung; }

    public MoebelTyp getTyp() { return typ; }
    public void setTyp(MoebelTyp typ) { this.typ = typ; }

    public Zustand getZustand() { return zustand; }
    public void setZustand(Zustand zustand) { this.zustand = zustand; }

    public Double getStandortLat() { return standortLat; }
    public void setStandortLat(Double standortLat) { this.standortLat = standortLat; }

    public Double getStandortLng() { return standortLng; }
    public void setStandortLng(Double standortLng) { this.standortLng = standortLng; }

    public String getStandortName() { return standortName; }
    public void setStandortName(String standortName) { this.standortName = standortName; }

    public Double getPreis() { return preis; }
    public void setPreis(Double preis) { this.preis = preis; }

    public LocalDate getKaufdatum() { return kaufdatum; }
    public void setKaufdatum(LocalDate kaufdatum) { this.kaufdatum = kaufdatum; }

    public boolean isStandardmoebel() { return standardmoebel; }
    public void setStandardmoebel(boolean standardmoebel) { this.standardmoebel = standardmoebel; }

    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) {
        this.foto = foto;
        this.hatFoto = foto != null && foto.length > 0;
    }

    public boolean isHatFoto() { return hatFoto; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
