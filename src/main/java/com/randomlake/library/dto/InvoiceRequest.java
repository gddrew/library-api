package com.randomlake.library.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceRequest {
  private int patronId;
  private int amount;
  private String status;
  private LocalDate date;
  private String campaign;
}
