package com.randomlake.library.controller;

import com.randomlake.library.dto.LoanActionRequest;
import com.randomlake.library.dto.LoanResponse;
import com.randomlake.library.dto.TransactionResponse;
import com.randomlake.library.service.LoanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@Validated
public class LoanController {

  private final LoanService loanService;

  public LoanController(LoanService loanService) {
    this.loanService = loanService;
  }

  @PostMapping("/action")
  public ResponseEntity<TransactionResponse> processLoanAction(
      @RequestBody @Valid LoanActionRequest loanActionRequest) {
    TransactionResponse transactionResponse = loanService.processLoanAction(loanActionRequest);
    return ResponseEntity.ok(transactionResponse);
  }

  @GetMapping("/history/patron/{patronId}")
  public ResponseEntity<List<LoanResponse>> getLoanHistoryByPatronId(@PathVariable int patronId) {
    List<LoanResponse> loans = loanService.findLoansByPatronId(patronId);
    if (loans.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(loans);
  }

  @GetMapping("/history/media/{mediaId}")
  public ResponseEntity<List<LoanResponse>> getLoanHistoryByMediaId(@PathVariable int mediaId) {
    List<LoanResponse> loans = loanService.findLoansByMediaId(mediaId);
    if (loans.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(loans);
  }

  @DeleteMapping("/history/loan/{loanId}")
  public ResponseEntity<Void> deleteLoanById(@PathVariable int loanId) {
    loanService.deleteLoanById(loanId);
    return ResponseEntity.noContent().build();
  }
}
