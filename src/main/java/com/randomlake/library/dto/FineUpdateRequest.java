package com.randomlake.library.dto;

import com.randomlake.library.enums.FineType;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

public class FineUpdateRequest {

  @Getter @Setter private int patronId;

  @Getter @Setter private int mediaId;

  @Getter @Setter @Positive private int amount;

  @Getter @Setter private FineType fineType;

  private boolean isPaid;
  private boolean isWaived;

  public boolean isPaid() {
    return isPaid;
  }

  public void setPaid(boolean paid) {
    isPaid = paid;
  }

  public boolean isWaived() {
    return isWaived;
  }

  public void setWaived(boolean waived) {
    isWaived = waived;
  }
}
