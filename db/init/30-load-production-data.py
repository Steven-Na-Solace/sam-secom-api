#!/usr/bin/env python3
"""
SECOM Production Data Loader - Option C (Hybrid)
Loads production lots, measurements, and quality results with AI enhancements
"""

import pymysql
import time
import random
import json
from datetime import datetime, timedelta

# Database connection parameters
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'rootpassword',
    'database': 'secom',
    'charset': 'utf8mb4'
}

# Data file paths (check both container and local paths)
import os
if os.path.exists('/data/secom.data'):
    DATA_FILE = '/data/secom.data'
    LABELS_FILE = '/data/secom_labels.data'
else:
    # Running from host machine
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(os.path.dirname(script_dir))
    DATA_FILE = os.path.join(project_root, 'data', 'secom.data')
    LABELS_FILE = os.path.join(project_root, 'data', 'secom_labels.data')

# Defect type distribution (for 104 failures)
DEFECT_TYPES = [
    ('electrical_fail', 0.40, ['Open circuit', 'Short circuit', 'Leakage current high']),
    ('dimensional_oor', 0.30, ['CD out of spec', 'Thickness variation', 'Overlay error']),
    ('surface_defect', 0.20, ['Scratch detected', 'Particle contamination', 'Film defect']),
    ('contamination', 0.10, ['Chemical residue', 'Particle count high', 'Foreign material'])
]

def wait_for_db():
    """Wait for database to be ready"""
    max_retries = 30
    retry_count = 0

    while retry_count < max_retries:
        try:
            conn = pymysql.connect(**DB_CONFIG)
            conn.close()
            print("✓ Database connection successful!")
            return True
        except Exception as e:
            retry_count += 1
            print(f"⏳ Waiting for database... ({retry_count}/{max_retries})")
            time.sleep(2)

    raise Exception("❌ Could not connect to database after maximum retries")

def parse_features_line(line):
    """Parse a line from secom.data into a list of values"""
    values = line.strip().split()
    parsed = []
    for val in values:
        if val.upper() == 'NAN':
            parsed.append(None)
        else:
            try:
                parsed.append(float(val))
            except ValueError:
                parsed.append(None)
    return parsed

def parse_labels_line(line):
    """Parse a line from secom_labels.data and convert to 2025 timeline"""
    parts = line.strip().split(maxsplit=1)
    classification = int(parts[0])
    timestamp_str = parts[1].strip('"') if len(parts) > 1 else None

    test_datetime = None
    if timestamp_str:
        try:
            # Parse original 2008 timestamp
            original_dt = datetime.strptime(timestamp_str, "%d/%m/%Y %H:%M:%S")

            # Convert 2008 dates to 2025 dates
            # Original data: July-September 2008
            # New data: September-November 2025
            # Map: July 2008 -> September 2025, August 2008 -> October 2025, September 2008 -> November 2025
            year_offset = 2025 - original_dt.year
            month_offset = 2  # Shift July->September, August->October, September->November

            new_month = original_dt.month + month_offset
            new_year = original_dt.year + year_offset

            # Handle month overflow
            if new_month > 12:
                new_month -= 12
                new_year += 1

            test_datetime = datetime(new_year, new_month, original_dt.day,
                                    original_dt.hour, original_dt.minute, original_dt.second)
        except ValueError:
            pass

    return classification, timestamp_str, test_datetime

def get_shift_id(hour):
    """Determine shift ID based on hour"""
    if 8 <= hour < 16:
        return 1  # DAY
    elif 16 <= hour < 24:
        return 2  # SWING
    else:  # 0 <= hour < 8
        return 3  # NIGHT

def get_operator_id(shift_id, lot_index):
    """Get operator ID based on shift and round-robin"""
    # Day shift: OPR 1-5, Swing: OPR 6-10, Night: OPR 11-15
    base_id = (shift_id - 1) * 5 + 1
    return base_id + (lot_index % 5)

def get_equipment_id(lot_index):
    """Distribute lots across equipment (ETCH machines get more)"""
    # CVD-01: 12.5%, CVD-02: 12.5%, ETCH-01: 25%, ETCH-02: 25%,
    # PHOTO-01: 12.5%, PHOTO-02: 12.5%
    dist = lot_index % 8
    if dist < 1:
        return 1  # CVD-01
    elif dist < 2:
        return 2  # CVD-02
    elif dist < 4:
        return 3  # ETCH-01
    elif dist < 6:
        return 4  # ETCH-02
    elif dist < 7:
        return 5  # PHOTO-01
    else:
        return 6  # PHOTO-02

def get_product_type_id(lot_index):
    """Distribute lots across product types"""
    # LOGIC-A100: 40%, LOGIC-B200: 30%, MEMORY-M300: 20%, ANALOG-X400: 10%
    pct = (lot_index % 100) / 100.0
    if pct < 0.40:
        return 1  # LOGIC-A100
    elif pct < 0.70:
        return 2  # LOGIC-B200
    elif pct < 0.90:
        return 3  # MEMORY-M300
    else:
        return 4  # ANALOG-X400

def calculate_quality_score(classification):
    """Generate quality score based on classification"""
    if classification == -1:  # Pass
        return round(random.uniform(85.0, 100.0), 2)
    else:  # Fail
        return round(random.uniform(40.0, 75.0), 2)

def generate_predicted_risk(classification, anomaly_count):
    """Generate AI predicted risk score"""
    if classification == 1:  # Failed
        base_risk = 0.75 + random.uniform(-0.15, 0.20)
    else:  # Passed
        if random.random() < 0.05:  # 5% false positive rate
            base_risk = random.uniform(0.5, 0.7)
        else:
            base_risk = random.uniform(0.0, 0.4)

    # Adjust based on anomaly count
    risk_adjustment = min(anomaly_count * 0.05, 0.25)
    final_risk = min(max(base_risk + risk_adjustment, 0.0), 1.0)

    return round(final_risk, 4)

def generate_risk_factors(features, anomaly_features):
    """Generate JSON risk factors for high-risk lots"""
    if len(anomaly_features) == 0:
        return None

    # Take top 3-5 anomalous features
    top_features = sorted(anomaly_features, key=lambda x: abs(x[1]), reverse=True)[:min(5, len(anomaly_features))]

    risk_factors = {}
    for feature_id, deviation in top_features:
        feature_code = f'F{feature_id}'
        factor_name = f'{feature_code}_{"high" if deviation > 0 else "low"}'
        contribution = round(abs(deviation) / sum(abs(d) for _, d in top_features), 4)
        risk_factors[factor_name] = contribution

    return json.dumps(risk_factors)

def assign_defect_type(fail_index):
    """Assign defect type to failed lots"""
    cumulative = 0.0
    rand = random.random()

    for defect_type, probability, notes_list in DEFECT_TYPES:
        cumulative += probability
        if rand <= cumulative:
            note = random.choice(notes_list)
            return defect_type, note

    return DEFECT_TYPES[0][0], DEFECT_TYPES[0][2][0]

def load_data():
    """Main data loading function"""
    print("="*70)
    print("SECOM Production Data Loader - Option C")
    print("="*70)
    print()

    # Wait for database
    wait_for_db()

    # Connect to database
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()

    try:
        # Read data files
        print("📂 Reading source data files...")
        with open(DATA_FILE, 'r') as f:
            features_lines = f.readlines()

        with open(LABELS_FILE, 'r') as f:
            labels_lines = f.readlines()

        if len(features_lines) != len(labels_lines):
            raise ValueError(f"Mismatch: {len(features_lines)} features vs {len(labels_lines)} labels")

        total_samples = len(features_lines)
        print(f"✓ Loaded {total_samples} samples")
        print()

        # Get feature ranges from database
        print("📊 Loading feature metadata for range checking...")
        cursor.execute("SELECT feature_id, normal_range_min, normal_range_max FROM feature_meta ORDER BY feature_id")
        feature_ranges = cursor.fetchall()
        print(f"✓ Loaded {len(feature_ranges)} feature definitions")
        print()

        # Prepare SQL statements
        insert_lot = """
            INSERT INTO lot (lot_number, product_type_id, equipment_id, operator_id, shift_id,
                           production_start, production_end, wafer_count, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, 'completed')
        """

        insert_measurement = """
            INSERT INTO lot_measurement (lot_id, feature_id, measurement_value, is_out_of_spec, measured_at)
            VALUES (%s, %s, %s, %s, %s)
        """

        insert_quality = """
            INSERT INTO quality_result
            (lot_id, classification, test_timestamp_raw, test_datetime, predicted_risk, risk_score,
             risk_factors, model_version, quality_score, defect_type, defect_code, inspector_id, notes, disposition)
            VALUES (%s, %s, %s, %s, %s, %s, %s, 'v1.0.0', %s, %s, %s, %s, %s, %s)
        """

        # Process each sample
        print("🔄 Processing samples and creating lots...")
        fail_index = 0
        measurement_batch = []
        measurement_count = 0

        for idx in range(total_samples):
            # Parse data
            features = parse_features_line(features_lines[idx])
            classification, timestamp_str, test_datetime = parse_labels_line(labels_lines[idx])

            if test_datetime is None:
                # Default to September-November 2025 timeline (ending before Nov 28 presentation)
                test_datetime = datetime(2025, 9, 15, 12, 0, 0) + timedelta(minutes=idx * 30)

            # Determine assignments
            hour = test_datetime.hour
            shift_id = get_shift_id(hour)
            operator_id = get_operator_id(shift_id, idx)
            equipment_id = get_equipment_id(idx)
            product_type_id = get_product_type_id(idx)

            # Create lot with 2025 timeline
            lot_number = f"LOT-{test_datetime.strftime('%Y%m')}-{idx+1:04d}"
            production_start = test_datetime - timedelta(hours=random.randint(4, 12))
            production_end = test_datetime
            wafer_count = random.choice([23, 24, 25])

            cursor.execute(insert_lot, (
                lot_number, product_type_id, equipment_id, operator_id, shift_id,
                production_start, production_end, wafer_count
            ))
            lot_id = cursor.lastrowid

            # Process measurements and detect anomalies
            anomaly_features = []
            for fid, value in enumerate(features):
                if value is not None and fid < len(feature_ranges):
                    feature_id, min_val, max_val = feature_ranges[fid]

                    # Check if out of spec
                    is_out_of_spec = False
                    if min_val is not None and max_val is not None:
                        if value < min_val or value > max_val:
                            is_out_of_spec = True
                            # Calculate deviation from midpoint
                            midpoint = (min_val + max_val) / 2.0
                            deviation = value - midpoint
                            anomaly_features.append((fid, deviation))

                    # Batch measurements for insertion
                    measurement_batch.append((lot_id, feature_id, value, is_out_of_spec, production_end))
                    measurement_count += 1

            # Insert measurements in batches
            if len(measurement_batch) >= 10000:
                cursor.executemany(insert_measurement, measurement_batch)
                measurement_batch = []

            # Generate AI predictions
            quality_score = calculate_quality_score(classification)
            predicted_risk = generate_predicted_risk(classification, len(anomaly_features))
            risk_score = round(predicted_risk * 100, 2)
            risk_factors = generate_risk_factors(features, anomaly_features[:5])

            # Assign defect details for failures
            defect_type = None
            defect_code = None
            inspector_note = None
            disposition = 'released'

            if classification == 1:  # Failed
                defect_type, inspector_note = assign_defect_type(fail_index)
                defect_code = f'{defect_type.upper()}-{fail_index+1:03d}'
                disposition = random.choice(['scrap', 'rework', 'scrap', 'rework', 'pending'])
                fail_index += 1

            # Assign inspector (Quality department operators: 3, 8, 13)
            inspector_id = random.choice([3, 8, 13])

            # Insert quality result
            cursor.execute(insert_quality, (
                lot_id, classification, timestamp_str, test_datetime, predicted_risk, risk_score,
                risk_factors, quality_score, defect_type, defect_code, inspector_id,
                inspector_note, disposition
            ))

            # Progress indicator
            if (idx + 1) % 100 == 0:
                conn.commit()
                print(f"  ✓ Processed {idx + 1}/{total_samples} lots ({measurement_count:,} measurements so far)")

        # Insert remaining measurements
        if measurement_batch:
            cursor.executemany(insert_measurement, measurement_batch)

        conn.commit()
        print()
        print(f"✓ All {total_samples} lots processed")
        print(f"✓ Total measurements inserted: {measurement_count:,}")
        print()

        # Verification
        print("🔍 Verifying data integrity...")
        cursor.execute("SELECT COUNT(*) FROM lot")
        lot_count = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM lot_measurement")
        meas_count = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM quality_result")
        qual_count = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM quality_result WHERE classification = 1")
        fail_count = cursor.fetchone()[0]

        cursor.execute("SELECT AVG(predicted_risk) FROM quality_result WHERE classification = 1")
        avg_risk_fail = cursor.fetchone()[0]

        cursor.execute("SELECT AVG(predicted_risk) FROM quality_result WHERE classification = -1")
        avg_risk_pass = cursor.fetchone()[0]

        print()
        print("="*70)
        print("DATA LOAD SUMMARY")
        print("="*70)
        print(f"Total lots loaded:          {lot_count:,}")
        print(f"Total measurements:         {meas_count:,}")
        print(f"Total quality results:      {qual_count:,}")
        print(f"Pass count:                 {lot_count - fail_count:,}")
        print(f"Fail count:                 {fail_count:,}")
        print(f"Fail rate:                  {fail_count * 100.0 / lot_count:.2f}%")
        print(f"Avg predicted risk (fail):  {avg_risk_fail:.4f}")
        print(f"Avg predicted risk (pass):  {avg_risk_pass:.4f}")
        print("="*70)
        print()
        print("✅ Production data loading complete!")

    except Exception as e:
        print(f"❌ Error loading data: {e}")
        import traceback
        traceback.print_exc()
        conn.rollback()
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    load_data()
