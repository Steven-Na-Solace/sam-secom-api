package com.secom.mes.controller;

import com.secom.mes.dto.*;
import com.secom.mes.repository.FeatureImportanceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Analytics and reporting endpoints using database views")
public class AnalyticsController {

    @PersistenceContext
    private EntityManager entityManager;

    private final FeatureImportanceRepository featureImportanceRepository;

    public AnalyticsController(FeatureImportanceRepository featureImportanceRepository) {
        this.featureImportanceRepository = featureImportanceRepository;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get production summary",
               description = "Returns overall production KPIs from production_summary view")
    public ProductionSummaryDto getProductionSummary() {
        String sql = "SELECT * FROM production_summary";
        Query query = entityManager.createNativeQuery(sql);
        Object[] result = (Object[]) query.getSingleResult();

        return new ProductionSummaryDto(
                ((Number) result[0]).longValue(),  // total_lots
                ((Number) result[1]).longValue(),  // active_equipment_count
                ((Number) result[2]).longValue(),  // active_operator_count
                ((Number) result[3]).longValue(),  // pass_count
                ((Number) result[4]).longValue(),  // fail_count
                result[5] != null ? new BigDecimal(result[5].toString()) : BigDecimal.ZERO,  // fail_rate_pct
                result[6] != null ? new BigDecimal(result[6].toString()) : BigDecimal.ZERO,  // avg_quality_score
                result[7] != null ? ((java.sql.Timestamp) result[7]).toLocalDateTime() : null,  // first_production_date
                result[8] != null ? ((java.sql.Timestamp) result[8]).toLocalDateTime() : null   // last_production_date
        );
    }

    @GetMapping("/equipment-health")
    @Operation(summary = "Get equipment health statistics",
               description = "Returns health metrics for all equipment from equipment_health_stats view")
    public List<EquipmentHealthDto> getEquipmentHealth() {
        String sql = "SELECT * FROM equipment_health_stats";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        List<EquipmentHealthDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            dtos.add(new EquipmentHealthDto(
                    ((Number) row[0]).intValue(),   // equipment_id
                    (String) row[1],                 // equipment_code
                    (String) row[2],                 // equipment_name
                    (String) row[3],                 // equipment_type
                    (String) row[4],                 // equipment_status
                    ((Number) row[5]).longValue(),   // total_lots_processed
                    ((Number) row[6]).longValue(),   // failed_lots
                    row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO,  // equipment_fail_rate_pct
                    row[8] != null ? new BigDecimal(row[8].toString()) : BigDecimal.ZERO,  // avg_quality_score
                    ((Number) row[9]).longValue(),   // days_operated
                    row[10] != null ? new BigDecimal(row[10].toString()) : BigDecimal.ZERO, // lots_between_failures
                    row[11] != null ? new BigDecimal(row[11].toString()) : BigDecimal.ZERO  // health_score
            ));
        }
        return dtos;
    }

    @GetMapping("/shift-performance")
    @Operation(summary = "Get shift performance comparison",
               description = "Returns performance metrics by shift from shift_performance_comparison view")
    public List<ShiftPerformanceDto> getShiftPerformance() {
        String sql = "SELECT * FROM shift_performance_comparison";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        List<ShiftPerformanceDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            dtos.add(new ShiftPerformanceDto(
                    ((Number) row[0]).intValue(),   // shift_id
                    (String) row[1],                 // shift_code
                    (String) row[2],                 // shift_name
                    ((Number) row[3]).longValue(),   // total_lots
                    ((Number) row[4]).longValue(),   // pass_count
                    ((Number) row[5]).longValue(),   // fail_count
                    row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO,  // fail_rate_pct
                    row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO,  // avg_quality_score
                    ((Number) row[8]).longValue(),   // operator_count
                    ((Number) row[9]).longValue()    // equipment_used
            ));
        }
        return dtos;
    }

    @GetMapping("/quality-summary")
    @Operation(summary = "Get quality analytics summary",
               description = "Returns defect type breakdown from quality_analytics_summary view")
    public List<QualityAnalyticsDto> getQualitySummary() {
        String sql = "SELECT * FROM quality_analytics_summary";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        List<QualityAnalyticsDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            dtos.add(new QualityAnalyticsDto(
                    (String) row[0],                 // defect_type
                    ((Number) row[1]).longValue(),   // occurrence_count
                    row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO,  // pct_of_failures
                    row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO,  // avg_quality_score
                    (String) row[4],                 // affected_product_families
                    (String) row[5]                  // affected_equipment_types
            ));
        }
        return dtos;
    }

    @GetMapping("/feature-importance")
    @Operation(summary = "Get feature importance rankings",
               description = "Returns top features by importance score for a specific defect type or overall")
    public List<Object> getFeatureImportance(
            @Parameter(description = "Defect type (overall, electrical_fail, dimensional_oor, etc.)")
            @RequestParam(defaultValue = "overall") String defectType,
            @Parameter(description = "Limit results (default 10)")
            @RequestParam(defaultValue = "10") Integer limit) {

        String sql = """
            SELECT
                fm.feature_code,
                fm.feature_name,
                fm.feature_category,
                fi.importance_score,
                fi.correlation_coefficient,
                fi.calculation_method
            FROM feature_importance fi
            JOIN feature_meta fm ON fi.feature_id = fm.feature_id
            WHERE fi.defect_type = :defectType
            ORDER BY fi.importance_score DESC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("defectType", defectType);
        query.setParameter("limit", limit);

        return query.getResultList();
    }

    @GetMapping("/high-risk-lots")
    @Operation(summary = "Get high-risk lots above threshold",
               description = "Returns lots with predicted risk above specified threshold with detailed information")
    public List<HighRiskLotDto> getHighRiskLots(
            @Parameter(description = "Risk threshold (0.0 to 1.0)")
            @RequestParam(defaultValue = "0.7") BigDecimal threshold,
            @Parameter(description = "Limit results (default 50)")
            @RequestParam(defaultValue = "50") Integer limit) {

        String sql = """
            SELECT
                l.lot_number,
                pt.product_name,
                e.equipment_code,
                qr.predicted_risk,
                qr.risk_score,
                qr.test_datetime,
                qr.classification
            FROM quality_result qr
            JOIN lot l ON qr.lot_id = l.lot_id
            JOIN product_type pt ON l.product_type_id = pt.product_type_id
            JOIN equipment e ON l.equipment_id = e.equipment_id
            WHERE qr.predicted_risk >= :threshold
            ORDER BY qr.predicted_risk DESC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("threshold", threshold);
        query.setParameter("limit", limit);
        List<Object[]> results = query.getResultList();

        List<HighRiskLotDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            dtos.add(new HighRiskLotDto(
                    (String) row[0],                 // lot_number
                    (String) row[1],                 // product_name
                    (String) row[2],                 // equipment_code
                    row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO,  // predicted_risk
                    row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO,  // risk_score
                    row[5] != null ? ((java.sql.Timestamp) row[5]).toLocalDateTime() : null, // test_datetime
                    ((Number) row[6]).intValue()     // classification
            ));
        }
        return dtos;
    }

    @GetMapping("/defect-distribution")
    @Operation(summary = "Get defect type distribution",
               description = "Returns distribution of defect types across failed lots")
    public List<Object> getDefectDistribution() {
        String sql = """
            SELECT
                defect_type,
                COUNT(*) as count,
                ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM quality_result WHERE classification = 1), 2) as percentage
            FROM quality_result
            WHERE classification = 1 AND defect_type IS NOT NULL
            GROUP BY defect_type
            ORDER BY count DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    @GetMapping("/risk-distribution")
    @Operation(summary = "Get risk score distribution",
               description = "Returns histogram of predicted risk scores")
    public List<Object> getRiskDistribution() {
        String sql = """
            SELECT
                FLOOR(predicted_risk * 10) / 10 as risk_bucket,
                COUNT(*) as lot_count,
                SUM(CASE WHEN classification = 1 THEN 1 ELSE 0 END) as actual_failures
            FROM quality_result
            WHERE predicted_risk IS NOT NULL
            GROUP BY risk_bucket
            ORDER BY risk_bucket
            """;

        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }
}
