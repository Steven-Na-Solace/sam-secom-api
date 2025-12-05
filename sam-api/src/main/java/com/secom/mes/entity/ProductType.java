package com.secom.mes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_type_id")
    private Integer productTypeId;

    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_family", nullable = false, length = 50)
    private String productFamily;

    @Column(name = "target_yield", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetYield;

    @Column(name = "specification_version", length = 20)
    private String specificationVersion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "productType")
    @JsonIgnore
    private List<Lot> lots;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
