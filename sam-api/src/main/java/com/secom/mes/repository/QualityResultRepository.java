package com.secom.mes.repository;

import com.secom.mes.entity.QualityResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface QualityResultRepository extends JpaRepository<QualityResult, Integer> {

    Optional<QualityResult> findByLot_LotId(Integer lotId);

    List<QualityResult> findByClassification(Integer classification);

    List<QualityResult> findByPredictedRiskGreaterThanEqual(BigDecimal threshold);

    List<QualityResult> findByDefectType(String defectType);

    Page<QualityResult> findAll(Pageable pageable);

    Page<QualityResult> findByClassification(Integer classification, Pageable pageable);
}
