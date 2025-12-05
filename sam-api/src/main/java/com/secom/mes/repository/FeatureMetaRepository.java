package com.secom.mes.repository;

import com.secom.mes.entity.FeatureMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureMetaRepository extends JpaRepository<FeatureMeta, Integer> {

    Optional<FeatureMeta> findByFeatureCode(String featureCode);

    List<FeatureMeta> findByFeatureCategory(String featureCategory);

    List<FeatureMeta> findByIsCritical(Boolean isCritical);

    @Query("SELECT f FROM FeatureMeta f WHERE " +
           "LOWER(f.featureName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.featureCategory) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<FeatureMeta> searchByNameOrCategory(@Param("query") String query);
}
