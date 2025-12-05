package com.secom.mes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "feature_meta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feature_id")
    private Integer featureId;

    @Column(name = "feature_code", nullable = false, unique = true, length = 20)
    private String featureCode;

    @Column(name = "feature_name", nullable = false, length = 100)
    private String featureName;

    @Column(name = "feature_category", nullable = false, length = 50)
    private String featureCategory;

    @Column(name = "process_stage", length = 50)
    private String processStage;

    @Column(name = "measurement_type", length = 50)
    private String measurementType;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "normal_range_min")
    private Double normalRangeMin;

    @Column(name = "normal_range_max")
    private Double normalRangeMax;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_critical")
    private Boolean isCritical;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "feature")
    @JsonIgnore
    private List<LotMeasurement> measurements;

    @OneToMany(mappedBy = "feature")
    @JsonIgnore
    private List<FeatureImportance> importanceRecords;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isCritical == null) {
            isCritical = false;
        }
    }
}
