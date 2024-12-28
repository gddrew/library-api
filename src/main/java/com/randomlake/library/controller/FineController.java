package com.randomlake.library.controller;

import static com.randomlake.library.mapper.FineMapper.toDto;

import com.randomlake.library.dto.FineCreationRequest;
import com.randomlake.library.dto.FineResponse;
import com.randomlake.library.dto.FineUpdateRequest;
import com.randomlake.library.mapper.FineMapper;
import com.randomlake.library.model.Fine;
import com.randomlake.library.service.FineService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fines")
public class FineController {

  private final FineService fineService;

  public FineController(FineService fineService) {
    this.fineService = fineService;
  }

  @PostMapping("/fine/create")
  public ResponseEntity<FineResponse> createFine(@Valid @RequestBody FineCreationRequest request) {
    Fine createdFine =
        fineService.assessFine(
            request.getPatronId(),
            request.getMediaId(),
            request.getFineType(),
            request.getAmount());
    FineResponse response = toDto(createdFine);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/fine/{fineId}")
  public ResponseEntity<FineResponse> updateFine(
      @PathVariable int fineId, @Valid @RequestBody FineUpdateRequest request) {
    Fine updatedFine =
        fineService.updateFine(
            fineId,
            request.getAmount(),
            request.isWaived(),
            request.isPaid(),
            request.getFineType());
    FineResponse response = toDto(updatedFine);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/patron/{patronId}")
  public ResponseEntity<List<FineResponse>> getFinesByPatronId(@PathVariable int patronId) {
    List<Fine> fines = fineService.getFinesByPatronId(patronId);
    List<FineResponse> responses =
        fines.stream().map(FineMapper::toDto).collect(Collectors.toList());
    if (fines.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responses);
    }
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/fine/{fineId}")
  public ResponseEntity<FineResponse> getFineById(@PathVariable int fineId) {
    Fine fine = fineService.getFineById(fineId);
    FineResponse response = toDto(fine);
    return fine != null
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
  }
}
