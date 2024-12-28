package com.randomlake.library.controller;

import com.randomlake.library.dto.PatronRequest;
import com.randomlake.library.dto.PatronResponse;
import com.randomlake.library.mapper.PatronMapper;
import com.randomlake.library.model.Patron;
import com.randomlake.library.service.PatronService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patrons")
public class PatronController {

  @Autowired private PatronService patronService;

  @GetMapping
  public ResponseEntity<List<PatronResponse>> getAllPatrons() {
    List<Patron> patrons = patronService.getAllPatrons();
    List<PatronResponse> patronResponses =
        patrons.stream().map(PatronMapper::toDto).collect(Collectors.toList());
    return new ResponseEntity<>(patronResponses, HttpStatus.OK);
  }

  @GetMapping("/{patronId}")
  public ResponseEntity<PatronResponse> getPatronByID(@PathVariable int patronId) {
    Patron patron = patronService.getPatronById(patronId);
    if (patron == null) {
      return ResponseEntity.notFound().build();
    }
    PatronResponse patronResponse = PatronMapper.toDto(patron);
    return ResponseEntity.ok(patronResponse);
  }

  @GetMapping("/patron/name/{patronName}")
  public ResponseEntity<List<PatronResponse>> getPatronByName(@PathVariable String patronName) {
    List<Patron> patrons = patronService.getPatronByName(patronName);
    if (patrons == null) {
      return ResponseEntity.notFound().build();
    }
    List<PatronResponse> patronResponses =
        patrons.stream().map(PatronMapper::toDto).collect(Collectors.toList());
    return ResponseEntity.ok(patronResponses);
  }

  @GetMapping("/patron/dob/{dateOfBirth}")
  public ResponseEntity<List<PatronResponse>> getPatronByDateOfBirth(
      @PathVariable LocalDate dateOfBirth) {
    List<Patron> patrons = patronService.getPatronByDateOfBirth(dateOfBirth);
    if (patrons == null) {
      return ResponseEntity.notFound().build();
    }
    List<PatronResponse> patronResponses =
        patrons.stream().map(PatronMapper::toDto).collect(Collectors.toList());
    return ResponseEntity.ok(patronResponses);
  }

  @GetMapping("patron/telephone/{telephone}")
  public ResponseEntity<List<PatronResponse>> getPatronByTelephone(@PathVariable String telephone) {
    List<Patron> patrons = patronService.getPatronByTelephone(telephone);
    if (patrons == null) {
      return ResponseEntity.notFound().build();
    }
    List<PatronResponse> patronResponses =
        patrons.stream().map(PatronMapper::toDto).collect(Collectors.toList());
    return ResponseEntity.ok(patronResponses);
  }

  @GetMapping("/patron/email/{emailAddress}")
  public ResponseEntity<List<PatronResponse>> getPatronByEmail(@PathVariable String emailAddress) {
    List<Patron> patrons = patronService.getPatronByEmail(emailAddress);
    if (patrons == null) {
      return ResponseEntity.notFound().build();
    }
    List<PatronResponse> patronResponses =
        patrons.stream().map(PatronMapper::toDto).collect(Collectors.toList());
    return ResponseEntity.ok(patronResponses);
  }

  @PostMapping
  public ResponseEntity<PatronResponse> addNewPatron(
      @Valid @RequestBody PatronRequest patronRequest) {
    Patron patron = PatronMapper.toEntity(patronRequest);
    Patron addedPatron = patronService.addNewPatron(patron);
    PatronResponse patronResponse = PatronMapper.toDto(addedPatron);
    return new ResponseEntity<>(patronResponse, HttpStatus.CREATED);
  }

  @PutMapping("/{patronId}")
  public ResponseEntity<PatronResponse> updatePatron(
      @PathVariable("patronId") int patronId, @Valid @RequestBody PatronRequest patronRequest) {
    Patron patronDetails = PatronMapper.toEntity(patronRequest);
    Patron updatedPatron = patronService.updatePatron(patronId, null, patronDetails);
    PatronResponse patronResponse = PatronMapper.toDto(updatedPatron);
    return ResponseEntity.ok(patronResponse);
  }

  @PatchMapping("/{patronId}")
  public ResponseEntity<PatronResponse> patchUpdatePatron(
      @PathVariable("patronId") int patronId, @RequestBody Map<String, Object> updates) {
    Patron patchUpdatedPatron = patronService.updatePatron(patronId, updates, null);
    PatronResponse patronResponse = PatronMapper.toDto(patchUpdatedPatron);
    return ResponseEntity.ok(patronResponse);
  }
}
