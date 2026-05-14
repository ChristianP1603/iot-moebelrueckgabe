package jku.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "prozess_instanz")
public class ProzessInstanz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long moebelstuckId;

    @Column(nullable = false)
    private Long camundaInstanceKey;

    @Column(nullable = false)
    private String status = "GESTARTET";

    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }

    public Long getMoebelstuckId() { return moebelstuckId; }
    public void setMoebelstuckId(Long moebelstuckId) { this.moebelstuckId = moebelstuckId; }

    public Long getCamundaInstanceKey() { return camundaInstanceKey; }
    public void setCamundaInstanceKey(Long camundaInstanceKey) { this.camundaInstanceKey = camundaInstanceKey; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
