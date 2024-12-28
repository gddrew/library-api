package com.randomlake.library.dto;

import com.randomlake.library.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionLogResponse {
  private TransactionType transactionType;
  private LocalDateTime transactionDate;
  private List<Integer> mediaIds;
}
