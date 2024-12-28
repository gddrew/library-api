package com.randomlake.library.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceResponse {
  private int invoiceId;
  private int patronId;
  private int amount;
  private String status;
  private LocalDate date;
  private String campaign;
}
