package jku.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "scan_history")
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long moebelstuckId;

    private Double standortLat;
    private Double standortLng;
    private String standortName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventTyp eventTyp;

    private String gescanntVon;

    @Column(nullable = false)
    private OffsetDateTime zeitstempel;

    @PrePersist
    protected void onCreate() {
        if (zeitstempel == null) {
            zeitstempel = OffsetDateTime.now();
        }
    }

    // Getter & Setter

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

    public OffsetDateTime getZeitstempel() { return zeitstempel; }
    public void setZeitstempel(OffsetDateTime zeitstempel) { this.zeitstempel = zeitstempel; }
}
