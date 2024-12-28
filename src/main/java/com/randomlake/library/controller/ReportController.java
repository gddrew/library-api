package com.randomlake.library.controller;

import com.randomlake.library.dto.ReportCardPatron;
import com.randomlake.library.dto.ReportLoanPatronMedia;
import com.randomlake.library.service.CardService;
import com.randomlake.library.service.LoanService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

  private final CardService cardService;
  private final LoanService loanService;

  @Autowired
  public ReportController(CardService cardService, LoanService loanService) {
    this.cardService = cardService;
    this.loanService = loanService;
  }

  @GetMapping("/cards-patrons")
  public ResponseEntity<List<ReportCardPatron>> getReportCardPatron(
      @RequestParam(required = false) Integer cardId,
      @RequestParam(required = false) Integer patronId) {
    List<ReportCardPatron> report =
        cardService.getReportCardPatron(Optional.ofNullable(cardId), Optional.ofNullable(patronId));
    return ResponseEntity.ok(report);
  }

  @GetMapping("/loans-patrons")
  public ResponseEntity<List<ReportLoanPatronMedia>> getReportLoanPatronMedia(
      @RequestParam(required = true) Integer patronId) {
    List<ReportLoanPatronMedia> report =
        loanService.getReportLoanPatronMedia(Optional.ofNullable(patronId));
    return ResponseEntity.ok(report);
  }
}
