package com.randomlake.library.dto;

import com.randomlake.library.enums.LoanStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanResponse {
  private int loanId;
  private int patronId;
  private LoanStatus status;
  private LocalDateTime createdDate;
  private LocalDateTime lastUpdateDate;
  private List<LoanItemResponse> items;
  private List<TransactionLogResponse> transactionLogs;
}
