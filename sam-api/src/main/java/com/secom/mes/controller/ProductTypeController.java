package com.secom.mes.controller;

import com.secom.mes.entity.ProductType;
import com.secom.mes.repository.ProductTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Product Types", description = "Product type management endpoints")
public class ProductTypeController {

    private final ProductTypeRepository productTypeRepository;

    public ProductTypeController(ProductTypeRepository productTypeRepository) {
        this.productTypeRepository = productTypeRepository;
    }

    @GetMapping
    @Operation(summary = "Get all product types", description = "Returns list of all product types")
    public List<ProductType> getAllProductTypes() {
        return productTypeRepository.findAll();
    }

    @GetMapping("/{productTypeId}")
    @Operation(summary = "Get product type by ID", description = "Returns a single product type by product type ID")
    public ResponseEntity<ProductType> getProductTypeById(@PathVariable Integer productTypeId) {
        return productTypeRepository.findById(productTypeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get product type by code", description = "Returns product type by unique product code")
    public ResponseEntity<ProductType> getProductTypeByCode(@PathVariable String code) {
        return productTypeRepository.findByProductCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/family/{family}")
    @Operation(summary = "Get product types by family", description = "Returns all product types in a specific family (Logic, Memory, Analog)")
    public List<ProductType> getProductTypesByFamily(@PathVariable String family) {
        return productTypeRepository.findByProductFamily(family);
    }

    @PostMapping
    @Operation(summary = "Create new product type", description = "Creates a new product type record")
    public ProductType createProductType(@RequestBody ProductType productType) {
        return productTypeRepository.save(productType);
    }

    @PutMapping("/{productTypeId}")
    @Operation(summary = "Update product type", description = "Updates an existing product type record")
    public ResponseEntity<ProductType> updateProductType(
            @PathVariable Integer productTypeId,
            @RequestBody ProductType productTypeDetails) {
        return productTypeRepository.findById(productTypeId)
                .map(productType -> {
                    productType.setProductName(productTypeDetails.getProductName());
                    productType.setProductFamily(productTypeDetails.getProductFamily());
                    productType.setTargetYield(productTypeDetails.getTargetYield());
                    productType.setSpecificationVersion(productTypeDetails.getSpecificationVersion());
                    return ResponseEntity.ok(productTypeRepository.save(productType));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{productTypeId}")
    @Operation(summary = "Delete product type", description = "Deletes a product type record")
    public ResponseEntity<Void> deleteProductType(@PathVariable Integer productTypeId) {
        return productTypeRepository.findById(productTypeId)
                .map(productType -> {
                    productTypeRepository.delete(productType);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
