package com.randomlake.library.mapper;

import com.randomlake.library.dto.LoanItemResponse;
import com.randomlake.library.dto.LoanResponse;
import com.randomlake.library.dto.TransactionLogResponse;
import com.randomlake.library.model.Loan;
import java.util.ArrayList;
import java.util.List;

public class LoanMapper {

  public static LoanResponse toLoanResponse(Loan loan) {
    if (loan == null) {
      return null;
    }

    LoanResponse response = new LoanResponse();
    response.setLoanId(loan.getLoanId());
    response.setPatronId(loan.getPatronId());
    response.setStatus(loan.getStatus());
    response.setCreatedDate(loan.getCreatedDate());
    response.setLastUpdateDate(loan.getLastUpdateDate());

    // Map Loan Items
    List<LoanItemResponse> itemResponses = new ArrayList<>();
    if (loan.getItems() != null) {
      for (Loan.LoanItem item : loan.getItems()) {
        LoanItemResponse itemResponse = new LoanItemResponse();
        itemResponse.setMediaId(item.getMediaId());
        itemResponse.setCheckoutDate(item.getCheckoutDate());
        itemResponse.setDueDate(item.getDueDate());
        itemResponse.setReturnDate(item.getReturnDate());
        itemResponse.setStatus(item.getStatus());
        itemResponses.add(itemResponse);
      }
    }
    response.setItems(itemResponses);

    // Map Transaction Logs
    List<TransactionLogResponse> logResponses = new ArrayList<>();
    if (loan.getTransactionLog() != null) {
      for (Loan.TransactionLog logEntry : loan.getTransactionLog()) {
        TransactionLogResponse logResponse = new TransactionLogResponse();
        logResponse.setTransactionType(logEntry.getTransactionType());
        logResponse.setTransactionDate(logEntry.getTransactionDate());
        logResponse.setMediaIds(logEntry.getMediaIds());
        logResponses.add(logResponse);
      }
    }
    response.setTransactionLogs(logResponses);

    return response;
  }
}
