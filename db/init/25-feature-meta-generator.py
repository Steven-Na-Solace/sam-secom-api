#!/usr/bin/env python3
"""
Feature Metadata Generator
Generates SQL INSERT statements for 590 SECOM features with descriptive names,
categories, measurement types, and units.
"""

import sys

# Feature category definitions
CATEGORIES = {
    'CVD_Process': {
        'range': (0, 99),
        'stage': 'Deposition',
        'types': [
            ('temperature', '°C', 200, 800),
            ('chamber_pressure', 'mTorr', 0.1, 100),
            ('gas_flow_rate', 'sccm', 0, 500),
            ('RF_power', 'W', 0, 3000),
        ]
    },
    'Etch_Process': {
        'range': (100, 199),
        'stage': 'Etching',
        'types': [
            ('chamber_pressure', 'mTorr', 1, 200),
            ('RF_power', 'W', 100, 2000),
            ('etch_time', 'seconds', 10, 300),
            ('gas_ratio', 'ratio', 0.1, 10),
        ]
    },
    'Photo_Process': {
        'range': (200, 299),
        'stage': 'Photolithography',
        'types': [
            ('exposure_dose', 'mJ/cm²', 10, 100),
            ('focus_offset', 'nm', -100, 100),
            ('overlay_error', 'nm', 0, 50),
            ('resist_thickness', 'Å', 1000, 10000),
        ]
    },
    'Test_Electrical': {
        'range': (300, 399),
        'stage': 'Electrical Testing',
        'types': [
            ('voltage', 'V', 0, 5),
            ('current', 'mA', 0, 1000),
            ('resistance', 'Ω', 0, 1000000),
            ('capacitance', 'pF', 0, 100),
        ]
    },
    'Test_Physical': {
        'range': (400, 489),
        'stage': 'Physical Testing',
        'types': [
            ('film_thickness', 'nm', 0, 1000),
            ('critical_dimension', 'nm', 0, 500),
            ('surface_roughness', 'Å', 0, 100),
            ('uniformity', '%', 0, 100),
        ]
    },
    'Environmental': {
        'range': (490, 589),
        'stage': 'Environmental Monitoring',
        'types': [
            ('humidity', '%RH', 0, 100),
            ('ambient_temperature', '°C', 18, 25),
            ('particle_count', 'counts/m³', 0, 1000),
            ('vibration', 'Hz', 0, 100),
        ]
    }
}

def generate_feature_inserts():
    """Generate SQL INSERT statements for all 590 features"""

    inserts = []
    feature_id = 0

    for category, config in CATEGORIES.items():
        start_id, end_id = config['range']
        stage = config['stage']
        types = config['types']

        for fid in range(start_id, end_id + 1):
            # Cycle through measurement types
            mtype, unit, min_val, max_val = types[fid % len(types)]

            feature_code = f'F{fid}'
            feature_name = f'{category}_{mtype}_{fid % len(types) + 1}'

            # Descriptive name
            if fid < 10:
                zone = 'Zone_A'
            elif fid < 20:
                zone = 'Zone_B'
            else:
                zone = f'Sensor_{fid % 10}'

            description = f'{stage} {mtype} measurement from {zone}'

            # Mark some features as critical (every 20th feature)
            is_critical = 'TRUE' if fid % 20 == 0 else 'FALSE'

            insert = f"""({fid + 1}, '{feature_code}', '{feature_name}', '{category}', '{stage}', '{mtype}', '{unit}', {min_val}, {max_val}, '{description}', {is_critical})"""
            inserts.append(insert)

            feature_id += 1

    return inserts

def main():
    """Main function to generate and output SQL"""

    print("-- ============================================================================")
    print("-- Feature Metadata - 590 SECOM Features")
    print("-- Auto-generated descriptive feature definitions")
    print("-- ============================================================================")
    print()
    print("USE secom;")
    print()

    inserts = generate_feature_inserts()

    # Write in batches of 50 for readability
    batch_size = 50
    for i in range(0, len(inserts), batch_size):
        batch = inserts[i:i+batch_size]

        print(f"-- Features {i} to {min(i+batch_size-1, len(inserts)-1)}")
        print("INSERT INTO feature_meta (feature_id, feature_code, feature_name, feature_category, process_stage, measurement_type, unit, normal_range_min, normal_range_max, description, is_critical) VALUES")
        print(',\n'.join(batch))
        print(';')
        print()

    print("-- ============================================================================")
    print("-- Feature metadata generation complete")
    print("-- ============================================================================")
    print()
    print("SELECT 'Feature metadata loaded successfully' as status,")
    print("       COUNT(*) as total_features,")
    print("       COUNT(DISTINCT feature_category) as categories,")
    print("       SUM(CASE WHEN is_critical THEN 1 ELSE 0 END) as critical_features")
    print("FROM feature_meta;")

if __name__ == '__main__':
    main()
