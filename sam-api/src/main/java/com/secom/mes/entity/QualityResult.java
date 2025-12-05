package com.secom.mes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quality_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Integer resultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false, unique = true)
    private Lot lot;

    @Column(name = "classification", nullable = false)
    private Integer classification; // -1 = pass, 1 = fail

    @Column(name = "test_timestamp_raw", nullable = false, length = 50)
    private String testTimestampRaw;

    @Column(name = "test_datetime")
    private LocalDateTime testDatetime;

    // AI/ML Enhancement Fields
    @Column(name = "predicted_risk", precision = 5, scale = 4)
    private BigDecimal predictedRisk;

    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "risk_factors", columnDefinition = "JSON")
    private String riskFactors;

    @Column(name = "model_version", length = 20)
    private String modelVersion;

    // Quality Details
    @Column(name = "quality_score", precision = 5, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "defect_type", length = 100)
    private String defectType;

    @Column(name = "defect_code", length = 50)
    private String defectCode;

    @Column(name = "defect_location", length = 100)
    private String defectLocation;

    // Inspection Tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id")
    private Operator inspector;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Operator reviewer;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "disposition", length = 20)
    private String disposition; // 'released', 'rework', 'scrap', 'pending'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
