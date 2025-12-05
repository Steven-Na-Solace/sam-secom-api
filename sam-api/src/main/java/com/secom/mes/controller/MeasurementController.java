package com.secom.mes.controller;

import com.secom.mes.entity.LotMeasurement;
import com.secom.mes.repository.LotMeasurementRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/measurements")
@Tag(name = "Measurements", description = "Lot measurement endpoints")
public class MeasurementController {

    private final LotMeasurementRepository measurementRepository;

    public MeasurementController(LotMeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @GetMapping("/lot/{lotId}")
    @Operation(summary = "Get measurements for a lot",
               description = "Returns all sensor measurements for a specific lot (typically 590 measurements)")
    public List<LotMeasurement> getMeasurementsByLot(@PathVariable Integer lotId) {
        return measurementRepository.findByLot_LotId(lotId);
    }

    @GetMapping("/lot/{lotId}/anomalies")
    @Operation(summary = "Get out-of-spec measurements for a lot",
               description = "Returns only measurements that are outside normal specification ranges")
    public List<LotMeasurement> getAnomaliesByLot(@PathVariable Integer lotId) {
        return measurementRepository.findByLot_LotIdAndIsOutOfSpec(lotId, true);
    }

    @GetMapping("/feature/{featureId}")
    @Operation(summary = "Get measurements for a feature",
               description = "Returns all measurements for a specific sensor/feature across all lots")
    public List<LotMeasurement> getMeasurementsByFeature(@PathVariable Integer featureId) {
        return measurementRepository.findByFeature_FeatureId(featureId);
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Get all out-of-spec measurements",
               description = "Returns paginated list of all measurements outside normal specification ranges")
    public Page<LotMeasurement> getAllAnomalies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return measurementRepository.findByIsOutOfSpec(true, pageable);
    }

    @PostMapping
    @Operation(summary = "Create new measurement", description = "Creates a new lot measurement record")
    public LotMeasurement createMeasurement(@RequestBody LotMeasurement measurement) {
        return measurementRepository.save(measurement);
    }

    @DeleteMapping("/{measurementId}")
    @Operation(summary = "Delete measurement", description = "Deletes a measurement record")
    public ResponseEntity<Void> deleteMeasurement(@PathVariable Long measurementId) {
        return measurementRepository.findById(measurementId)
                .map(measurement -> {
                    measurementRepository.delete(measurement);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
