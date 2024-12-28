package com.randomlake.library.dto;

import com.randomlake.library.enums.CardStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardResponse {
  private int cardId;
  private String barCodeId;
  private int patronId;
  private CardStatus status;
  private LocalDateTime createdDate;
  private LocalDateTime lastUpdateDate;
  private LocalDateTime lastUsedDate;
}
