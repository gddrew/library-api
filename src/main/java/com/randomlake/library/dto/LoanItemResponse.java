package com.randomlake.library.dto;

import com.randomlake.library.enums.ItemStatus;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanItemResponse {
  private int mediaId;
  private LocalDate checkoutDate;
  private LocalDate dueDate;
  private LocalDate returnDate;
  private ItemStatus status;
}
