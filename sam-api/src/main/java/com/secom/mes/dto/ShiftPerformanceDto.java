package com.secom.mes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftPerformanceDto {
    private Integer shiftId;
    private String shiftCode;
    private String shiftName;
    private Long totalLots;
    private Long passCount;
    private Long failCount;
    private BigDecimal failRatePct;
    private BigDecimal avgQualityScore;
    private Long operatorCount;
    private Long equipmentUsed;
}
