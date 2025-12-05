-- ============================================================================
-- SECOM MES Database Schema - Option C (Hybrid)
-- Manufacturing Execution System for Semiconductor Production
-- ============================================================================

USE secom;

-- Drop existing tables if any (for clean reinstall)
DROP TABLE IF EXISTS feature_importance;
DROP TABLE IF EXISTS lot_measurement;
DROP TABLE IF EXISTS quality_result;
DROP TABLE IF EXISTS lot;
DROP TABLE IF EXISTS feature_meta;
DROP TABLE IF EXISTS product_type;
DROP TABLE IF EXISTS operator;
DROP TABLE IF EXISTS shift;
DROP TABLE IF EXISTS equipment;

-- ============================================================================
-- MASTER TABLES (Reference Data)
-- ============================================================================

-- Equipment: Manufacturing machines and test equipment
CREATE TABLE equipment (
    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'e.g., CVD-01, ETCH-02',
    equipment_name VARCHAR(100) NOT NULL COMMENT 'e.g., CVD Machine Line 1',
    equipment_type VARCHAR(50) NOT NULL COMMENT 'CVD, Etcher, Tester, Photolithography',
    location VARCHAR(100) COMMENT 'Clean Room A, Bay 2, etc.',
    manufacturer VARCHAR(100) COMMENT 'Applied Materials, ASML, etc.',
    install_date DATE,
    status ENUM('active', 'maintenance', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_equipment_type (equipment_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Manufacturing equipment and test machines';

-- Shift: Work shift definitions
CREATE TABLE shift (
    shift_id INT AUTO_INCREMENT PRIMARY KEY,
    shift_code VARCHAR(20) NOT NULL UNIQUE COMMENT 'DAY, SWING, NIGHT',
    shift_name VARCHAR(50) NOT NULL COMMENT 'Day Shift, Swing Shift, Night Shift',
    start_time TIME NOT NULL COMMENT '08:00:00, 16:00:00, 00:00:00',
    end_time TIME NOT NULL COMMENT '16:00:00, 00:00:00, 08:00:00',
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Work shift definitions (Day/Swing/Night)';

-- Operator: Production operators and inspectors
CREATE TABLE operator (
    operator_id INT AUTO_INCREMENT PRIMARY KEY,
    operator_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'OPR-001, OPR-002',
    operator_name VARCHAR(100) NOT NULL COMMENT 'John Smith, Jane Doe',
    employee_number VARCHAR(50) UNIQUE,
    department VARCHAR(100) COMMENT 'Fabrication, Testing, Quality',
    hire_date DATE,
    email VARCHAR(100),
    status ENUM('active', 'inactive', 'on_leave') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_department (department),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Production operators and quality inspectors';

-- Product Type: Product specifications
CREATE TABLE product_type (
    product_type_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'LOGIC-A100, MEMORY-M300',
    product_name VARCHAR(100) NOT NULL COMMENT 'Logic Chip A100',
    product_family VARCHAR(50) NOT NULL COMMENT 'Logic, Memory, Analog',
    target_yield DECIMAL(5,2) NOT NULL COMMENT 'Target yield percentage, e.g., 95.00',
    specification_version VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_family (product_family)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Product types and specifications';

-- Feature Meta: Sensor and measurement definitions
CREATE TABLE feature_meta (
    feature_id INT AUTO_INCREMENT PRIMARY KEY,
    feature_code VARCHAR(20) NOT NULL UNIQUE COMMENT 'F0, F1, ... F589',
    feature_name VARCHAR(100) NOT NULL COMMENT 'Descriptive name',
    feature_category VARCHAR(50) NOT NULL COMMENT 'CVD_Process, Etch_Process, etc.',
    process_stage VARCHAR(50) COMMENT 'Deposition, Etching, Testing, etc.',
    measurement_type VARCHAR(50) COMMENT 'temperature, pressure, voltage, etc.',
    unit VARCHAR(20) COMMENT '°C, Pa, V, mA, nm, etc.',
    normal_range_min DOUBLE COMMENT 'Expected minimum value',
    normal_range_max DOUBLE COMMENT 'Expected maximum value',
    description TEXT,
    is_critical BOOLEAN DEFAULT FALSE COMMENT 'Critical quality parameter flag',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_feature_code (feature_code),
    INDEX idx_category (feature_category),
    INDEX idx_critical (is_critical)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Feature/sensor metadata with normal ranges';

-- ============================================================================
-- PRODUCTION DATA TABLES
-- ============================================================================

-- Lot: Production lots (main entity)
CREATE TABLE lot (
    lot_id INT AUTO_INCREMENT PRIMARY KEY,
    lot_number VARCHAR(100) NOT NULL UNIQUE COMMENT 'LOT-2008-07-0001, LOT-2008-07-0002',
    product_type_id INT NOT NULL COMMENT 'FK to product_type',
    equipment_id INT NOT NULL COMMENT 'FK to equipment (primary processing equipment)',
    operator_id INT NOT NULL COMMENT 'FK to operator (primary operator)',
    shift_id INT NOT NULL COMMENT 'FK to shift',
    production_start DATETIME NOT NULL COMMENT 'When lot entered production',
    production_end DATETIME COMMENT 'When lot completed (derived from test timestamp)',
    wafer_count INT DEFAULT 25 COMMENT 'Number of wafers in lot',
    status ENUM('in_progress', 'completed', 'quality_hold', 'released', 'scrapped') DEFAULT 'completed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_type_id) REFERENCES product_type(product_type_id),
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
    FOREIGN KEY (operator_id) REFERENCES operator(operator_id),
    FOREIGN KEY (shift_id) REFERENCES shift(shift_id),
    INDEX idx_lot_number (lot_number),
    INDEX idx_product_type (product_type_id),
    INDEX idx_equipment (equipment_id),
    INDEX idx_operator (operator_id),
    INDEX idx_shift (shift_id),
    INDEX idx_production_start (production_start),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Production lots with metadata';

-- Lot Measurement: Sensor measurements in long format
CREATE TABLE lot_measurement (
    measurement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lot_id INT NOT NULL COMMENT 'FK to lot',
    feature_id INT NOT NULL COMMENT 'FK to feature_meta',
    measurement_value DOUBLE COMMENT 'Actual measured value (NULL if NaN)',
    is_out_of_spec BOOLEAN DEFAULT FALSE COMMENT 'TRUE if outside normal range',
    measured_at DATETIME COMMENT 'Measurement timestamp (same as production_end)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lot_id) REFERENCES lot(lot_id) ON DELETE CASCADE,
    FOREIGN KEY (feature_id) REFERENCES feature_meta(feature_id),
    INDEX idx_lot_feature (lot_id, feature_id),
    INDEX idx_feature (feature_id),
    INDEX idx_out_of_spec (is_out_of_spec),
    INDEX idx_measured_at (measured_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Sensor measurements in normalized long format';

-- Quality Result: Inspection results with AI/ML enhancements
CREATE TABLE quality_result (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    lot_id INT NOT NULL UNIQUE COMMENT 'FK to lot (1:1 relationship)',
    classification TINYINT NOT NULL COMMENT '-1 = pass, 1 = fail',
    test_timestamp_raw VARCHAR(50) NOT NULL COMMENT 'Original timestamp string from dataset',
    test_datetime DATETIME COMMENT 'Parsed datetime',

    -- AI/ML Enhancement Fields (Option C)
    predicted_risk DECIMAL(5,4) DEFAULT NULL COMMENT 'ML predicted failure probability (0.0000-1.0000)',
    risk_score DECIMAL(5,2) DEFAULT NULL COMMENT 'Composite risk score (0-100)',
    risk_factors JSON DEFAULT NULL COMMENT 'Contributing factors: {"F123_high": 0.35, "F456_low": 0.28}',
    model_version VARCHAR(20) DEFAULT 'v1.0.0' COMMENT 'Prediction model version',

    -- Quality Details
    quality_score DECIMAL(5,2) DEFAULT NULL COMMENT 'Overall quality score 0-100',
    defect_type VARCHAR(100) DEFAULT NULL COMMENT 'If failed: electrical_fail, dimensional_oor, surface_defect, contamination',
    defect_code VARCHAR(50) DEFAULT NULL COMMENT 'Specific defect code',
    defect_location VARCHAR(100) DEFAULT NULL COMMENT 'Wafer position, zone, etc.',

    -- Inspection Tracking
    inspector_id INT DEFAULT NULL COMMENT 'FK to operator (who inspected)',
    notes TEXT COMMENT 'Inspector notes',
    reviewed_by INT DEFAULT NULL COMMENT 'FK to operator (who reviewed)',
    reviewed_at DATETIME COMMENT 'Review timestamp',
    disposition VARCHAR(20) DEFAULT 'pending' COMMENT 'Disposition: released, rework, scrap, pending, reviewed, etc.',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (lot_id) REFERENCES lot(lot_id) ON DELETE CASCADE,
    FOREIGN KEY (inspector_id) REFERENCES operator(operator_id),
    FOREIGN KEY (reviewed_by) REFERENCES operator(operator_id),

    INDEX idx_classification (classification),
    INDEX idx_test_datetime (test_datetime),
    INDEX idx_predicted_risk (predicted_risk),
    INDEX idx_defect_type (defect_type),
    INDEX idx_disposition (disposition)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Quality inspection results with AI/ML risk predictions';

-- ============================================================================
-- ANALYTICS TABLES
-- ============================================================================

-- Feature Importance: Pre-computed feature importance scores
CREATE TABLE feature_importance (
    importance_id INT AUTO_INCREMENT PRIMARY KEY,
    feature_id INT NOT NULL COMMENT 'FK to feature_meta',
    defect_type VARCHAR(50) DEFAULT 'overall' COMMENT 'Specific defect or "overall"',
    importance_score DECIMAL(5,4) NOT NULL COMMENT 'Feature importance 0.0000-1.0000',
    correlation_coefficient DECIMAL(6,4) COMMENT 'Pearson correlation with failures (-1 to +1)',
    sample_count INT NOT NULL COMMENT 'Number of samples used in calculation',
    calculated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'When this was calculated',
    calculation_method VARCHAR(50) DEFAULT 'correlation' COMMENT 'correlation, random_forest, chi_square, etc.',
    metadata JSON COMMENT 'Additional calculation metadata',

    FOREIGN KEY (feature_id) REFERENCES feature_meta(feature_id),

    INDEX idx_feature_defect (feature_id, defect_type),
    INDEX idx_defect_type (defect_type),
    INDEX idx_importance_score (importance_score DESC),
    INDEX idx_calculated_at (calculated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Pre-computed feature importance for root cause analysis';

-- ============================================================================
-- ANALYTICS VIEWS
-- ============================================================================

-- Production Summary: Overall KPIs
CREATE OR REPLACE VIEW production_summary AS
SELECT
    COUNT(DISTINCT l.lot_id) as total_lots,
    COUNT(DISTINCT l.equipment_id) as active_equipment_count,
    COUNT(DISTINCT l.operator_id) as active_operator_count,
    SUM(CASE WHEN qr.classification = -1 THEN 1 ELSE 0 END) as pass_count,
    SUM(CASE WHEN qr.classification = 1 THEN 1 ELSE 0 END) as fail_count,
    ROUND(SUM(CASE WHEN qr.classification = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as fail_rate_pct,
    ROUND(AVG(qr.quality_score), 2) as avg_quality_score,
    MIN(l.production_start) as first_production_date,
    MAX(l.production_end) as last_production_date
FROM lot l
LEFT JOIN quality_result qr ON l.lot_id = qr.lot_id;

-- Equipment Health Stats: Per-equipment performance metrics
CREATE OR REPLACE VIEW equipment_health_stats AS
SELECT
    e.equipment_id,
    e.equipment_code,
    e.equipment_name,
    e.equipment_type,
    e.status as equipment_status,
    COUNT(l.lot_id) as total_lots_processed,
    SUM(CASE WHEN qr.classification = 1 THEN 1 ELSE 0 END) as failed_lots,
    ROUND(SUM(CASE WHEN qr.classification = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(l.lot_id), 0), 2) as equipment_fail_rate_pct,
    ROUND(AVG(qr.quality_score), 2) as avg_quality_score,
    COUNT(DISTINCT DATE(l.production_start)) as days_operated,
    ROUND(COUNT(l.lot_id) / NULLIF(SUM(CASE WHEN qr.classification = 1 THEN 1 ELSE 0 END), 0), 2) as lots_between_failures,
    ROUND(100 - (SUM(CASE WHEN qr.classification = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(l.lot_id), 0) * 1.5), 2) as health_score
FROM equipment e
LEFT JOIN lot l ON e.equipment_id = l.equipment_id
LEFT JOIN quality_result qr ON l.lot_id = qr.lot_id
GROUP BY e.equipment_id, e.equipment_code, e.equipment_name, e.equipment_type, e.status;

-- Shift Performance Comparison: Shift-level analysis
CREATE OR REPLACE VIEW shift_performance_comparison AS
SELECT
    s.shift_id,
    s.shift_code,
    s.shift_name,
    COUNT(l.lot_id) as total_lots,
    SUM(CASE WHEN qr.classification = -1 THEN 1 ELSE 0 END) as pass_count,
    SUM(CASE WHEN qr.classification = 1 THEN 1 ELSE 0 END) as fail_count,
    ROUND(SUM(CASE WHEN qr.classification = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(l.lot_id), 0), 2) as fail_rate_pct,
    ROUND(AVG(qr.quality_score), 2) as avg_quality_score,
    COUNT(DISTINCT l.operator_id) as operator_count,
    COUNT(DISTINCT l.equipment_id) as equipment_used
FROM shift s
LEFT JOIN lot l ON s.shift_id = l.shift_id
LEFT JOIN quality_result qr ON l.lot_id = qr.lot_id
GROUP BY s.shift_id, s.shift_code, s.shift_name;

-- Quality Analytics Summary: Defect type analysis
CREATE OR REPLACE VIEW quality_analytics_summary AS
SELECT
    qr.defect_type,
    COUNT(*) as occurrence_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM quality_result WHERE classification = 1), 2) as pct_of_failures,
    ROUND(AVG(qr.quality_score), 2) as avg_quality_score,
    GROUP_CONCAT(DISTINCT pt.product_family ORDER BY pt.product_family SEPARATOR ', ') as affected_product_families,
    GROUP_CONCAT(DISTINCT e.equipment_type ORDER BY e.equipment_type SEPARATOR ', ') as affected_equipment_types
FROM quality_result qr
INNER JOIN lot l ON qr.lot_id = l.lot_id
LEFT JOIN product_type pt ON l.product_type_id = pt.product_type_id
LEFT JOIN equipment e ON l.equipment_id = e.equipment_id
WHERE qr.classification = 1 AND qr.defect_type IS NOT NULL
GROUP BY qr.defect_type
ORDER BY occurrence_count DESC;

-- ============================================================================
-- Schema creation complete
-- ============================================================================
