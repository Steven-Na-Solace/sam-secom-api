package com.secom.mes.repository;

import com.secom.mes.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Integer> {

    Optional<Operator> findByOperatorCode(String operatorCode);

    List<Operator> findByDepartment(String department);

    List<Operator> findByStatus(String status);
}
