package com.secom.mes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionSummaryDto {
    private Long totalLots;
    private Long activeEquipmentCount;
    private Long activeOperatorCount;
    private Long passCount;
    private Long failCount;
    private BigDecimal failRatePct;
    private BigDecimal avgQualityScore;
    private LocalDateTime firstProductionDate;
    private LocalDateTime lastProductionDate;
}
