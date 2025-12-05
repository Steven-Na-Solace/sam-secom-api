package com.secom.mes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentHealthDto {
    private Integer equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private String equipmentType;
    private String equipmentStatus;
    private Long totalLotsProcessed;
    private Long failedLots;
    private BigDecimal equipmentFailRatePct;
    private BigDecimal avgQualityScore;
    private Long daysOperated;
    private BigDecimal lotsBetweenFailures;
    private BigDecimal healthScore;
}
