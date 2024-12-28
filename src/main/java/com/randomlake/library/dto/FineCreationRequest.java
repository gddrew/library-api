package com.randomlake.library.dto;

import com.randomlake.library.enums.FineType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

public class FineCreationRequest {

  @Getter
  @Setter
  @NotNull(message = "Patron ID is required")
  private Integer patronId;

  @Getter
  @Setter
  @NotNull(message = "Media ID is required")
  private Integer mediaId;

  @Getter
  @Setter
  @NotNull(message = "Fine type is required")
  private FineType fineType;

  @Getter
  @Setter
  @NotNull(message = "Fine amount is required")
  @Positive(message = "Fine amount must be greater than zero")
  private int amount;
}
