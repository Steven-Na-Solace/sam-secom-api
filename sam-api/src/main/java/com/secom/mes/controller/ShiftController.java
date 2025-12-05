package com.secom.mes.controller;

import com.secom.mes.entity.Shift;
import com.secom.mes.repository.ShiftRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shifts")
@Tag(name = "Shifts", description = "Work shift management endpoints")
public class ShiftController {

    private final ShiftRepository shiftRepository;

    public ShiftController(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @GetMapping
    @Operation(summary = "Get all shifts", description = "Returns list of all work shifts")
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    @GetMapping("/{shiftId}")
    @Operation(summary = "Get shift by ID", description = "Returns a single shift by shift ID")
    public ResponseEntity<Shift> getShiftById(@PathVariable Integer shiftId) {
        return shiftRepository.findById(shiftId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get shift by code", description = "Returns shift by unique shift code (DAY, SWING, NIGHT)")
    public ResponseEntity<Shift> getShiftByCode(@PathVariable String code) {
        return shiftRepository.findByShiftCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new shift", description = "Creates a new shift record")
    public Shift createShift(@RequestBody Shift shift) {
        return shiftRepository.save(shift);
    }

    @PutMapping("/{shiftId}")
    @Operation(summary = "Update shift", description = "Updates an existing shift record")
    public ResponseEntity<Shift> updateShift(
            @PathVariable Integer shiftId,
            @RequestBody Shift shiftDetails) {
        return shiftRepository.findById(shiftId)
                .map(shift -> {
                    shift.setShiftName(shiftDetails.getShiftName());
                    shift.setStartTime(shiftDetails.getStartTime());
                    shift.setEndTime(shiftDetails.getEndTime());
                    shift.setDescription(shiftDetails.getDescription());
                    return ResponseEntity.ok(shiftRepository.save(shift));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{shiftId}")
    @Operation(summary = "Delete shift", description = "Deletes a shift record")
    public ResponseEntity<Void> deleteShift(@PathVariable Integer shiftId) {
        return shiftRepository.findById(shiftId)
                .map(shift -> {
                    shiftRepository.delete(shift);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
