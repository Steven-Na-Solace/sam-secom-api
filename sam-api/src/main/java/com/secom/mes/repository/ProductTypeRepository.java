package com.secom.mes.repository;

import com.secom.mes.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Integer> {

    Optional<ProductType> findByProductCode(String productCode);

    List<ProductType> findByProductFamily(String productFamily);
}
