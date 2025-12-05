package com.secom.mes.controller;

import com.secom.mes.entity.FeatureMeta;
import com.secom.mes.repository.FeatureMetaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/features")
@Tag(name = "Feature Metadata", description = "Feature/sensor metadata management endpoints")
public class FeatureMetaController {

    private final FeatureMetaRepository featureMetaRepository;

    public FeatureMetaController(FeatureMetaRepository featureMetaRepository) {
        this.featureMetaRepository = featureMetaRepository;
    }

    @GetMapping
    @Operation(summary = "Get all features", description = "Returns paginated list of all feature metadata (590 features)")
    public Page<FeatureMeta> getAllFeatures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return featureMetaRepository.findAll(pageable);
    }

    @GetMapping("/{featureId}")
    @Operation(summary = "Get feature by ID", description = "Returns a single feature metadata by feature ID")
    public ResponseEntity<FeatureMeta> getFeatureById(@PathVariable Integer featureId) {
        return featureMetaRepository.findById(featureId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get feature by code", description = "Returns feature metadata by unique feature code (F0-F589)")
    public ResponseEntity<FeatureMeta> getFeatureByCode(@PathVariable String code) {
        return featureMetaRepository.findByFeatureCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get features by category", description = "Returns all features in a specific category (CVD_Process, Etch_Process, etc.)")
    public List<FeatureMeta> getFeaturesByCategory(@PathVariable String category) {
        return featureMetaRepository.findByFeatureCategory(category);
    }

    @GetMapping("/critical")
    @Operation(summary = "Get critical features", description = "Returns all features marked as critical quality parameters")
    public List<FeatureMeta> getCriticalFeatures() {
        return featureMetaRepository.findByIsCritical(true);
    }

    @GetMapping("/search")
    @Operation(summary = "Search features", description = "Search features by name or category")
    public List<FeatureMeta> searchFeatures(@RequestParam String q) {
        return featureMetaRepository.searchByNameOrCategory(q);
    }

    @PostMapping
    @Operation(summary = "Create new feature", description = "Creates a new feature metadata record")
    public FeatureMeta createFeature(@RequestBody FeatureMeta featureMeta) {
        return featureMetaRepository.save(featureMeta);
    }

    @PutMapping("/{featureId}")
    @Operation(summary = "Update feature", description = "Updates an existing feature metadata record")
    public ResponseEntity<FeatureMeta> updateFeature(
            @PathVariable Integer featureId,
            @RequestBody FeatureMeta featureDetails) {
        return featureMetaRepository.findById(featureId)
                .map(feature -> {
                    feature.setFeatureName(featureDetails.getFeatureName());
                    feature.setFeatureCategory(featureDetails.getFeatureCategory());
                    feature.setProcessStage(featureDetails.getProcessStage());
                    feature.setMeasurementType(featureDetails.getMeasurementType());
                    feature.setUnit(featureDetails.getUnit());
                    feature.setNormalRangeMin(featureDetails.getNormalRangeMin());
                    feature.setNormalRangeMax(featureDetails.getNormalRangeMax());
                    feature.setDescription(featureDetails.getDescription());
                    feature.setIsCritical(featureDetails.getIsCritical());
                    return ResponseEntity.ok(featureMetaRepository.save(feature));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{featureId}")
    @Operation(summary = "Delete feature", description = "Deletes a feature metadata record")
    public ResponseEntity<Void> deleteFeature(@PathVariable Integer featureId) {
        return featureMetaRepository.findById(featureId)
                .map(feature -> {
                    featureMetaRepository.delete(feature);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
