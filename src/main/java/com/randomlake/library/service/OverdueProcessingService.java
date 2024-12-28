package com.randomlake.library.service;

import static java.time.temporal.ChronoUnit.DAYS;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.FineType;
import com.randomlake.library.enums.ItemStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Loan;
import com.randomlake.library.repository.LoanRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OverdueProcessingService {

  private final LoanRepository loanRepository;
  private final PatronService patronService;
  private final FineService fineService;

  private static final Logger log = LoggerFactory.getLogger(OverdueProcessingService.class);

  @Value("${fine.daily.amount}")
  private int finePerDay;

  @Value("${overdue.threshold.days}")
  private int overdueThresholdDays;

  public OverdueProcessingService(
      LoanRepository loanRepository, PatronService patronService, FineService fineService) {
    this.loanRepository = loanRepository;
    this.patronService = patronService;
    this.fineService = fineService;
  }

  @Scheduled(cron = "${overdue.cron}")
  public void processOverdueLoans() {
    suspendOverduePatrons();
  }

  protected void suspendOverduePatrons() {
    LocalDate overdueThreshold = LocalDate.now().minusDays(overdueThresholdDays);

    // Find loans that have items overdue by 30 days or more
    List<Loan> overdueLoans = loanRepository.findLoansWithOverdueItems(overdueThreshold);

    for (Loan loan : overdueLoans) {
      int patronId = loan.getPatronId();

      // Suspend patron if not already suspended
      if (!patronService.isPatronSuspended(patronId)) {
        patronService.suspendPatron(patronId);
        log.info("Patron {} suspended due to overdue items", patronId);
      }

      // Assess fines for overdue items
      for (Loan.LoanItem item : loan.getItems()) {
        if (item.getStatus() == ItemStatus.CHECKED_OUT
            && item.getDueDate().isBefore(overdueThreshold)
            && !fineService.hasFineBeenAssessed(
                patronId, item.getMediaId(), FineType.OVERDUE_ITEM)) {

          int fineAmount = calculateFineAmount(item);

          try {
            fineService.assessFine(patronId, item.getMediaId(), FineType.OVERDUE_ITEM, fineAmount);
          } catch (GeneralException ex) {
            if (ex.getType() == ExceptionType.FINE_ALREADY_EXISTS) {
              // Fine already exists; log and continue
              log.info(
                  "Fine already exists for patronId: {}, mediaId: {}", patronId, item.getMediaId());
            } else {
              // Handle other exceptions or rethrow
              throw ex;
            }
          }
        }
      }
    }
  }

  protected int calculateFineAmount(Loan.LoanItem item) {
    int daysOverdue = (int) DAYS.between(item.getDueDate(), LocalDate.now());
    return daysOverdue * finePerDay;
  }
}
