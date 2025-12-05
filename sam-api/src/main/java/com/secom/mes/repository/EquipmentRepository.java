package com.secom.mes.repository;

import com.secom.mes.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {

    Optional<Equipment> findByEquipmentCode(String equipmentCode);

    List<Equipment> findByEquipmentType(String equipmentType);

    List<Equipment> findByStatus(String status);
}
