package com.secom.mes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighRiskLotDto {
    private String lotNumber;
    private String productName;
    private String equipmentCode;
    private BigDecimal predictedRisk;
    private BigDecimal riskScore;
    private LocalDateTime testDatetime;
    private Integer actualClassification;
}
