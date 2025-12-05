package com.secom.mes.repository;

import com.secom.mes.entity.Lot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LotRepository extends JpaRepository<Lot, Integer> {

    Optional<Lot> findByLotNumber(String lotNumber);

    List<Lot> findByEquipment_EquipmentId(Integer equipmentId);

    List<Lot> findByOperator_OperatorId(Integer operatorId);

    List<Lot> findByProductionStartBetween(LocalDateTime start, LocalDateTime end);

    List<Lot> findByStatus(String status);

    Page<Lot> findAll(Pageable pageable);

    @Query("SELECT l FROM Lot l WHERE " +
           "(:equipmentId IS NULL OR l.equipment.equipmentId = :equipmentId) AND " +
           "(:operatorId IS NULL OR l.operator.operatorId = :operatorId) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:startDate IS NULL OR l.productionStart >= :startDate) AND " +
           "(:endDate IS NULL OR l.productionStart <= :endDate)")
    Page<Lot> findByFilters(
            @Param("equipmentId") Integer equipmentId,
            @Param("operatorId") Integer operatorId,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
