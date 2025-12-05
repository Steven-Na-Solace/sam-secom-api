package com.secom.mes.controller;

import com.secom.mes.entity.Equipment;
import com.secom.mes.repository.EquipmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipment")
@Tag(name = "Equipment", description = "Equipment management endpoints")
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;

    public EquipmentController(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @GetMapping
    @Operation(summary = "Get all equipment", description = "Returns list of all manufacturing equipment")
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @GetMapping("/{equipmentId}")
    @Operation(summary = "Get equipment by ID", description = "Returns a single equipment by equipment ID")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Integer equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get equipment by code", description = "Returns equipment by unique equipment code")
    public ResponseEntity<Equipment> getEquipmentByCode(@PathVariable String code) {
        return equipmentRepository.findByEquipmentCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get equipment by type", description = "Returns all equipment of a specific type (CVD, Etcher, etc.)")
    public List<Equipment> getEquipmentByType(@PathVariable String type) {
        return equipmentRepository.findByEquipmentType(type);
    }

    @PostMapping
    @Operation(summary = "Create new equipment", description = "Creates a new equipment record")
    public Equipment createEquipment(@RequestBody Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    @PutMapping("/{equipmentId}")
    @Operation(summary = "Update equipment", description = "Updates an existing equipment record")
    public ResponseEntity<Equipment> updateEquipment(
            @PathVariable Integer equipmentId,
            @RequestBody Equipment equipmentDetails) {
        return equipmentRepository.findById(equipmentId)
                .map(equipment -> {
                    equipment.setEquipmentName(equipmentDetails.getEquipmentName());
                    equipment.setEquipmentType(equipmentDetails.getEquipmentType());
                    equipment.setLocation(equipmentDetails.getLocation());
                    equipment.setManufacturer(equipmentDetails.getManufacturer());
                    equipment.setStatus(equipmentDetails.getStatus());
                    return ResponseEntity.ok(equipmentRepository.save(equipment));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{equipmentId}")
    @Operation(summary = "Delete equipment", description = "Deletes an equipment record")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Integer equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .map(equipment -> {
                    equipmentRepository.delete(equipment);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
