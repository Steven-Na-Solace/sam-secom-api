package com.secom.mes.repository;

import com.secom.mes.entity.FeatureImportance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureImportanceRepository extends JpaRepository<FeatureImportance, Integer> {

    List<FeatureImportance> findByDefectType(String defectType);

    List<FeatureImportance> findByDefectTypeOrderByImportanceScoreDesc(String defectType);

    List<FeatureImportance> findByFeature_FeatureId(Integer featureId);

    List<FeatureImportance> findTop10ByDefectTypeOrderByImportanceScoreDesc(String defectType);
}
