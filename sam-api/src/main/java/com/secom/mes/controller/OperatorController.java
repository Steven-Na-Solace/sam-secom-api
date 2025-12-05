package com.secom.mes.controller;

import com.secom.mes.entity.Lot;
import com.secom.mes.entity.Operator;
import com.secom.mes.repository.LotRepository;
import com.secom.mes.repository.OperatorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operators")
@Tag(name = "Operators", description = "Production operator management endpoints")
public class OperatorController {

    private final OperatorRepository operatorRepository;
    private final LotRepository lotRepository;

    public OperatorController(OperatorRepository operatorRepository, LotRepository lotRepository) {
        this.operatorRepository = operatorRepository;
        this.lotRepository = lotRepository;
    }

    @GetMapping
    @Operation(summary = "Get all operators", description = "Returns list of all production operators")
    public List<Operator> getAllOperators() {
        return operatorRepository.findAll();
    }

    @GetMapping("/{operatorId}")
    @Operation(summary = "Get operator by ID", description = "Returns a single operator by operator ID")
    public ResponseEntity<Operator> getOperatorById(@PathVariable Integer operatorId) {
        return operatorRepository.findById(operatorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get operator by code", description = "Returns operator by unique operator code")
    public ResponseEntity<Operator> getOperatorByCode(@PathVariable String code) {
        return operatorRepository.findByOperatorCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get operators by department", description = "Returns all operators in a specific department")
    public List<Operator> getOperatorsByDepartment(@PathVariable String department) {
        return operatorRepository.findByDepartment(department);
    }

    @GetMapping("/{operatorId}/lots")
    @Operation(summary = "Get operator's lots", description = "Returns all lots processed by a specific operator")
    public ResponseEntity<List<Lot>> getOperatorLots(@PathVariable Integer operatorId) {
        return operatorRepository.findById(operatorId)
                .map(operator -> ResponseEntity.ok(lotRepository.findByOperator_OperatorId(operatorId)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new operator", description = "Creates a new operator record")
    public Operator createOperator(@RequestBody Operator operator) {
        return operatorRepository.save(operator);
    }

    @PutMapping("/{operatorId}")
    @Operation(summary = "Update operator", description = "Updates an existing operator record")
    public ResponseEntity<Operator> updateOperator(
            @PathVariable Integer operatorId,
            @RequestBody Operator operatorDetails) {
        return operatorRepository.findById(operatorId)
                .map(operator -> {
                    operator.setOperatorName(operatorDetails.getOperatorName());
                    operator.setDepartment(operatorDetails.getDepartment());
                    operator.setEmail(operatorDetails.getEmail());
                    operator.setStatus(operatorDetails.getStatus());
                    return ResponseEntity.ok(operatorRepository.save(operator));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{operatorId}")
    @Operation(summary = "Delete operator", description = "Deletes an operator record")
    public ResponseEntity<Void> deleteOperator(@PathVariable Integer operatorId) {
        return operatorRepository.findById(operatorId)
                .map(operator -> {
                    operatorRepository.delete(operator);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
