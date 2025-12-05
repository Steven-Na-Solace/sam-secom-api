-- ============================================================================
-- SECOM MES Master Data - Option C (Hybrid)
-- Reference data for equipment, shifts, operators, and product types
-- ============================================================================

USE secom;

-- ============================================================================
-- EQUIPMENT DATA (8 units)
-- ============================================================================

INSERT INTO equipment (equipment_code, equipment_name, equipment_type, location, manufacturer, install_date, status) VALUES
('CVD-01', 'CVD Machine Line 1', 'CVD', 'Clean Room A - Bay 1', 'Applied Materials', '2023-03-15', 'active'),
('CVD-02', 'CVD Machine Line 2', 'CVD', 'Clean Room A - Bay 2', 'Applied Materials', '2023-03-15', 'active'),
('ETCH-01', 'Plasma Etcher Unit 1', 'Etcher', 'Clean Room B - Bay 1', 'Lam Research', '2023-06-20', 'active'),
('ETCH-02', 'Plasma Etcher Unit 2', 'Etcher', 'Clean Room B - Bay 2', 'Lam Research', '2023-06-20', 'active'),
('PHOTO-01', 'Photolithography Scanner 1', 'Photolithography', 'Clean Room C - Bay 1', 'ASML', '2024-01-10', 'active'),
('PHOTO-02', 'Photolithography Scanner 2', 'Photolithography', 'Clean Room C - Bay 2', 'ASML', '2024-01-10', 'active'),
('TEST-01', 'Automated Test Equipment 1', 'Tester', 'Test Floor - Zone A', 'KLA-Tencor', '2024-05-15', 'active'),
('TEST-02', 'Automated Test Equipment 2', 'Tester', 'Test Floor - Zone B', 'KLA-Tencor', '2024-05-15', 'active');

-- ============================================================================
-- SHIFT DATA (3 shifts)
-- ============================================================================

INSERT INTO shift (shift_code, shift_name, start_time, end_time, description) VALUES
('DAY', 'Day Shift', '08:00:00', '16:00:00', 'Primary day shift - 8:00 AM to 4:00 PM'),
('SWING', 'Swing Shift', '16:00:00', '00:00:00', 'Evening shift - 4:00 PM to 12:00 AM'),
('NIGHT', 'Night Shift', '00:00:00', '08:00:00', 'Night shift - 12:00 AM to 8:00 AM');

-- ============================================================================
-- OPERATOR DATA (15 operators - 5 per shift)
-- ============================================================================

-- Day Shift Operators (08:00-16:00)
INSERT INTO operator (operator_code, operator_name, employee_number, department, hire_date, email, status) VALUES
('OPR-001', 'James Wilson', 'EMP001', 'Fabrication', '2023-03-15', 'james.wilson@secom.com', 'active'),
('OPR-002', 'Maria Garcia', 'EMP002', 'Fabrication', '2023-06-20', 'maria.garcia@secom.com', 'active'),
('OPR-003', 'David Chen', 'EMP003', 'Quality', '2024-01-10', 'david.chen@secom.com', 'active'),
('OPR-004', 'Sarah Johnson', 'EMP004', 'Fabrication', '2024-04-22', 'sarah.johnson@secom.com', 'active'),
('OPR-005', 'Michael Park', 'EMP005', 'Testing', '2024-08-15', 'michael.park@secom.com', 'active'),

-- Swing Shift Operators (16:00-00:00)
('OPR-006', 'Jennifer Lee', 'EMP006', 'Fabrication', '2023-09-12', 'jennifer.lee@secom.com', 'active'),
('OPR-007', 'Robert Martinez', 'EMP007', 'Fabrication', '2024-02-18', 'robert.martinez@secom.com', 'active'),
('OPR-008', 'Lisa Anderson', 'EMP008', 'Quality', '2024-05-30', 'lisa.anderson@secom.com', 'active'),
('OPR-009', 'Kevin Zhang', 'EMP009', 'Fabrication', '2024-09-08', 'kevin.zhang@secom.com', 'active'),
('OPR-010', 'Emily Taylor', 'EMP010', 'Testing', '2025-01-15', 'emily.taylor@secom.com', 'active'),

-- Night Shift Operators (00:00-08:00)
('OPR-011', 'Daniel Kim', 'EMP011', 'Fabrication', '2023-11-22', 'daniel.kim@secom.com', 'active'),
('OPR-012', 'Amanda Wong', 'EMP012', 'Fabrication', '2024-03-07', 'amanda.wong@secom.com', 'active'),
('OPR-013', 'Thomas Brown', 'EMP013', 'Quality', '2024-07-14', 'thomas.brown@secom.com', 'active'),
('OPR-014', 'Jessica Nguyen', 'EMP014', 'Fabrication', '2024-10-25', 'jessica.nguyen@secom.com', 'active'),
('OPR-015', 'Christopher Davis', 'EMP015', 'Testing', '2025-02-28', 'christopher.davis@secom.com', 'active');

-- ============================================================================
-- PRODUCT TYPE DATA (4 product types)
-- ============================================================================

INSERT INTO product_type (product_code, product_name, product_family, target_yield, specification_version) VALUES
('LOGIC-A100', 'Logic Chip A100 Series', 'Logic', 95.00, 'v2.3'),
('LOGIC-B200', 'Logic Chip B200 Series', 'Logic', 93.00, 'v1.8'),
('MEMORY-M300', 'Memory Module M300', 'Memory', 92.00, 'v3.1'),
('ANALOG-X400', 'Analog IC X400', 'Analog', 90.00, 'v1.5');

-- ============================================================================
-- Master data insertion complete
-- ============================================================================

SELECT 'Master data loaded successfully' as status,
       (SELECT COUNT(*) FROM equipment) as equipment_count,
       (SELECT COUNT(*) FROM shift) as shift_count,
       (SELECT COUNT(*) FROM operator) as operator_count,
       (SELECT COUNT(*) FROM product_type) as product_type_count;
