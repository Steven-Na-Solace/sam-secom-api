package com.secom.mes.repository;

import com.secom.mes.entity.LotMeasurement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotMeasurementRepository extends JpaRepository<LotMeasurement, Long> {

    List<LotMeasurement> findByLot_LotId(Integer lotId);

    List<LotMeasurement> findByFeature_FeatureId(Integer featureId);

    List<LotMeasurement> findByIsOutOfSpec(Boolean isOutOfSpec);

    List<LotMeasurement> findByLot_LotIdAndIsOutOfSpec(Integer lotId, Boolean isOutOfSpec);

    Page<LotMeasurement> findByIsOutOfSpec(Boolean isOutOfSpec, Pageable pageable);
}
