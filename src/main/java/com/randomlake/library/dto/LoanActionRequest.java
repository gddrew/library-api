package com.randomlake.library.dto;

import com.randomlake.library.enums.TransactionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanActionRequest {

  @NotNull(message = "Patron ID is required")
  private int patronId;

  @NotEmpty(message = "At least one media ID must be provided")
  private List<Integer> mediaIds;

  @NotNull(message = "Action type is required")
  private TransactionType transactionType;
}
