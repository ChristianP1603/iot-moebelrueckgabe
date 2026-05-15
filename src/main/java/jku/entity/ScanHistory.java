package jku.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "scan_history")
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moebelstuck_id", nullable = false)
    private Long moebelstuckId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moebelstuck_id", insertable = false, updatable = false)
    @JsonIgnore
    private Moebelstuck moebelstuck;

    private Double standortLat;
    private Double standortLng;
    private String standortName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventTyp eventTyp;

    private String gescanntVon;

    @Enumerated(EnumType.STRING)
    private Pruefergebnis pruefergebnis;

    private Boolean ersatzteileVorhanden;

    @Column(nullable = false)
    private OffsetDateTime zeitstempel;

    @PrePersist
    protected void onCreate() {
        if (zeitstempel == null) {
            zeitstempel = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }

    public Long getMoebelstuckId() { return moebelstuckId; }
    public void setMoebelstuckId(Long moebelstuckId) { this.moebelstuckId = moebelstuckId; }

    public Double getStandortLat() { return standortLat; }
    public void setStandortLat(Double standortLat) { this.standortLat = standortLat; }

    public Double getStandortLng() { return standortLng; }
    public void setStandortLng(Double standortLng) { this.standortLng = standortLng; }

    public String getStandortName() { return standortName; }
    public void setStandortName(String standortName) { this.standortName = standortName; }

    public EventTyp getEventTyp() { return eventTyp; }
    public void setEventTyp(EventTyp eventTyp) { this.eventTyp = eventTyp; }

    public String getGescanntVon() { return gescanntVon; }
    public void setGescanntVon(String gescanntVon) { this.gescanntVon = gescanntVon; }

    public Pruefergebnis getPruefergebnis() { return pruefergebnis; }
    public void setPruefergebnis(Pruefergebnis pruefergebnis) { this.pruefergebnis = pruefergebnis; }

    public Boolean getErsatzteileVorhanden() { return ersatzteileVorhanden; }
    public void setErsatzteileVorhanden(Boolean ersatzteileVorhanden) { this.ersatzteileVorhanden = ersatzteileVorhanden; }

    public OffsetDateTime getZeitstempel() { return zeitstempel; }
    public void setZeitstempel(OffsetDateTime zeitstempel) { this.zeitstempel = zeitstempel; }
}
