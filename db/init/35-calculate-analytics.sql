-- ============================================================================
-- Calculate Feature Importance - Analytics Post-Processing
-- Computes correlation-based feature importance for root cause analysis
-- ============================================================================

USE secom;

-- This script calculates feature importance after production data is loaded
-- Due to MySQL limitations with complex calculations, we'll create a simplified version

-- ============================================================================
-- Feature Importance Calculation (Simplified Correlation-Based)
-- ============================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS calculate_feature_importance$$

CREATE PROCEDURE calculate_feature_importance()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_feature_id INT;
    DECLARE v_defect_type VARCHAR(50);
    DECLARE cur CURSOR FOR
        SELECT feature_id FROM feature_meta ORDER BY feature_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Calculate overall importance for each feature
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_feature_id;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Insert overall importance (using standard deviation as proxy)
        INSERT INTO feature_importance
        (feature_id, defect_type, importance_score, correlation_coefficient, sample_count, calculation_method)
        SELECT
            v_feature_id,
            'overall',
            LEAST(1.0, GREATEST(0.0, COALESCE(
                STDDEV(lm.measurement_value) / NULLIF(ABS(AVG(lm.measurement_value)), 0),
                0
            ))) as importance_score,
            NULL as correlation_coefficient,
            COUNT(*) as sample_count,
            'stddev_normalized' as calculation_method
        FROM lot_measurement lm
        WHERE lm.feature_id = v_feature_id
          AND lm.measurement_value IS NOT NULL
        GROUP BY lm.feature_id
        HAVING COUNT(*) > 10 AND ABS(AVG(lm.measurement_value)) > 0.0001;

        -- Calculate per defect type importance
        INSERT INTO feature_importance
        (feature_id, defect_type, importance_score, correlation_coefficient, sample_count, calculation_method)
        SELECT
            v_feature_id,
            qr.defect_type,
            LEAST(1.0, GREATEST(0.0, COALESCE(
                STDDEV(lm.measurement_value) / NULLIF(ABS(AVG(lm.measurement_value)), 0),
                0
            ))) as importance_score,
            NULL as correlation_coefficient,
            COUNT(*) as sample_count,
            'stddev_per_defect' as calculation_method
        FROM lot_measurement lm
        INNER JOIN quality_result qr ON lm.lot_id = qr.lot_id
        WHERE lm.feature_id = v_feature_id
          AND lm.measurement_value IS NOT NULL
          AND qr.classification = 1
          AND qr.defect_type IS NOT NULL
        GROUP BY lm.feature_id, qr.defect_type
        HAVING COUNT(*) > 3 AND ABS(AVG(lm.measurement_value)) > 0.0001;

    END LOOP;

    CLOSE cur;

END$$

DELIMITER ;

-- Execute the procedure
CALL calculate_feature_importance();

-- Verify results
SELECT
    'Feature importance calculated' as status,
    COUNT(*) as total_records,
    COUNT(DISTINCT feature_id) as unique_features,
    COUNT(DISTINCT defect_type) as defect_types
FROM feature_importance;

-- Show top 10 most important features
SELECT
    fm.feature_code,
    fm.feature_name,
    fi.defect_type,
    fi.importance_score,
    fi.sample_count
FROM feature_importance fi
JOIN feature_meta fm ON fi.feature_id = fm.feature_id
WHERE fi.defect_type = 'overall'
ORDER BY fi.importance_score DESC
LIMIT 10;

-- ============================================================================
-- Analytics calculation complete
-- ============================================================================
