package com.randomlake.library.dto;

import com.randomlake.library.enums.FineType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FineResponse {
  private int fineId;
  private int patronId;
  private int mediaId;
  private FineType fineType;
  private int amount;
  private LocalDateTime dateAssessed;
  private LocalDateTime datePaid;
  private boolean isPaid;
  private boolean isWaived;
}
