package com.randomlake.library.dto;

import com.randomlake.library.enums.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class CardStatusUpdateRequest {

  @NotNull @Getter @Setter private CardStatus newStatus;
}
