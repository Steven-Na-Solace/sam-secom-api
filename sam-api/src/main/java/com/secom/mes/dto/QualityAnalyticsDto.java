package com.secom.mes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityAnalyticsDto {
    private String defectType;
    private Long occurrenceCount;
    private BigDecimal pctOfFailures;
    private BigDecimal avgQualityScore;
    private String affectedProductFamilies;
    private String affectedEquipmentTypes;
}
