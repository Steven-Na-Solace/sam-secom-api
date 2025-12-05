package com.secom.mes.controller;

import com.secom.mes.entity.QualityResult;
import com.secom.mes.repository.QualityResultRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/quality")
@Tag(name = "Quality", description = "Quality result and inspection endpoints")
public class QualityController {

    private final QualityResultRepository qualityResultRepository;

    public QualityController(QualityResultRepository qualityResultRepository) {
        this.qualityResultRepository = qualityResultRepository;
    }

    @GetMapping("/results")
    @Operation(summary = "Get all quality results", description = "Returns paginated list of all quality inspection results")
    public Page<QualityResult> getAllResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return qualityResultRepository.findAll(pageable);
    }

    @GetMapping("/results/{qualityResultId}")
    @Operation(summary = "Get quality result by ID", description = "Returns a single quality result by quality result ID")
    public ResponseEntity<QualityResult> getResultById(@PathVariable Integer qualityResultId) {
        return qualityResultRepository.findById(qualityResultId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/results/lot/{lotId}")
    @Operation(summary = "Get quality result for a lot", description = "Returns quality result for a specific lot")
    public ResponseEntity<QualityResult> getResultByLotId(@PathVariable Integer lotId) {
        return qualityResultRepository.findByLot_LotId(lotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/results/failed")
    @Operation(summary = "Get failed lots", description = "Returns all lots that failed quality inspection (classification = 1)")
    public Page<QualityResult> getFailedResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return qualityResultRepository.findByClassification(1, pageable);
    }

    @GetMapping("/results/passed")
    @Operation(summary = "Get passed lots", description = "Returns all lots that passed quality inspection (classification = -1)")
    public Page<QualityResult> getPassedResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return qualityResultRepository.findByClassification(-1, pageable);
    }

    @GetMapping("/results/high-risk")
    @Operation(summary = "Get high-risk lots",
               description = "Returns lots with predicted risk above threshold (default 0.7)")
    public List<QualityResult> getHighRiskResults(
            @Parameter(description = "Risk threshold (0.0 to 1.0)")
            @RequestParam(defaultValue = "0.7") BigDecimal threshold) {
        return qualityResultRepository.findByPredictedRiskGreaterThanEqual(threshold);
    }

    @GetMapping("/results/defect/{type}")
    @Operation(summary = "Get results by defect type",
               description = "Returns quality results filtered by defect type (electrical_fail, dimensional_oor, etc.)")
    public List<QualityResult> getResultsByDefectType(@PathVariable String type) {
        return qualityResultRepository.findByDefectType(type);
    }

    @PostMapping("/results")
    @Operation(summary = "Create new quality result", description = "Creates a new quality inspection result")
    public QualityResult createResult(@RequestBody QualityResult result) {
        return qualityResultRepository.save(result);
    }

    @PutMapping("/results/{qualityResultId}")
    @Operation(summary = "Update quality result", description = "Updates an existing quality result")
    public ResponseEntity<QualityResult> updateResult(
            @PathVariable Integer qualityResultId,
            @RequestBody QualityResult resultDetails) {
        return qualityResultRepository.findById(qualityResultId)
                .map(result -> {
                    result.setQualityScore(resultDetails.getQualityScore());
                    result.setDefectType(resultDetails.getDefectType());
                    result.setDefectCode(resultDetails.getDefectCode());
                    result.setDefectLocation(resultDetails.getDefectLocation());
                    result.setNotes(resultDetails.getNotes());
                    result.setDisposition(resultDetails.getDisposition());
                    return ResponseEntity.ok(qualityResultRepository.save(result));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/results/{qualityResultId}")
    @Operation(summary = "Delete quality result", description = "Deletes a quality result record")
    public ResponseEntity<Void> deleteResult(@PathVariable Integer qualityResultId) {
        return qualityResultRepository.findById(qualityResultId)
                .map(result -> {
                    qualityResultRepository.delete(result);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
