package com.secom.mes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "feature_importance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureImportance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "importance_id")
    private Integer importanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private FeatureMeta feature;

    @Column(name = "defect_type", length = 50)
    private String defectType; // 'overall', 'electrical_fail', 'dimensional_oor', etc.

    @Column(name = "importance_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal importanceScore;

    @Column(name = "correlation_coefficient", precision = 6, scale = 4)
    private BigDecimal correlationCoefficient;

    @Column(name = "sample_count", nullable = false)
    private Integer sampleCount;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "calculation_method", length = 50)
    private String calculationMethod;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @PrePersist
    protected void onCreate() {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
        if (calculationMethod == null) {
            calculationMethod = "correlation";
        }
        if (defectType == null) {
            defectType = "overall";
        }
    }
}
