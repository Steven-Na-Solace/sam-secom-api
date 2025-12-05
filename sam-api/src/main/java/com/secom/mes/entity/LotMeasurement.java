package com.secom.mes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lot_measurement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LotMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_id")
    private Long measurementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private FeatureMeta feature;

    @Column(name = "measurement_value")
    private Double measurementValue;

    @Column(name = "is_out_of_spec")
    private Boolean isOutOfSpec;

    @Column(name = "measured_at")
    private LocalDateTime measuredAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isOutOfSpec == null) {
            isOutOfSpec = false;
        }
    }
}
