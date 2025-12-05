package com.secom.mes.controller;

import com.secom.mes.entity.Lot;
import com.secom.mes.repository.LotRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/lots")
@Tag(name = "Lots", description = "Production lot management endpoints")
public class LotController {

    private final LotRepository lotRepository;

    public LotController(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @GetMapping
    @Operation(summary = "Get all lots with filters",
               description = "Returns paginated list of lots with optional filters for equipment, operator, status, and date range")
    public Page<Lot> getAllLots(
            @Parameter(description = "Equipment ID filter")
            @RequestParam(required = false) Integer equipmentId,
            @Parameter(description = "Operator ID filter")
            @RequestParam(required = false) Integer operatorId,
            @Parameter(description = "Status filter (in_progress, completed, quality_hold, released, scrapped)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Start date filter (ISO format: 2008-07-01T00:00:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date filter (ISO format: 2008-09-30T23:59:59)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return lotRepository.findByFilters(equipmentId, operatorId, status, startDate, endDate, pageable);
    }

    @GetMapping("/{lotId}")
    @Operation(summary = "Get lot by ID", description = "Returns a single lot by lot ID")
    public ResponseEntity<Lot> getLotById(@PathVariable Integer lotId) {
        return lotRepository.findById(lotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{lotNumber}")
    @Operation(summary = "Get lot by lot number", description = "Returns lot by unique lot number")
    public ResponseEntity<Lot> getLotByNumber(@PathVariable String lotNumber) {
        return lotRepository.findByLotNumber(lotNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new lot", description = "Creates a new production lot record")
    public Lot createLot(@RequestBody Lot lot) {
        return lotRepository.save(lot);
    }

    @PutMapping("/{lotId}")
    @Operation(summary = "Update lot", description = "Updates an existing lot record")
    public ResponseEntity<Lot> updateLot(
            @PathVariable Integer lotId,
            @RequestBody Lot lotDetails) {
        return lotRepository.findById(lotId)
                .map(lot -> {
                    lot.setStatus(lotDetails.getStatus());
                    lot.setProductionEnd(lotDetails.getProductionEnd());
                    lot.setWaferCount(lotDetails.getWaferCount());
                    return ResponseEntity.ok(lotRepository.save(lot));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{lotId}")
    @Operation(summary = "Delete lot", description = "Deletes a lot record")
    public ResponseEntity<Void> deleteLot(@PathVariable Integer lotId) {
        return lotRepository.findById(lotId)
                .map(lot -> {
                    lotRepository.delete(lot);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
