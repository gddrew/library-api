package com.randomlake.library.enums;

import lombok.Getter;

@Getter
public enum BarcodeType {
  CARD("cardId"),
  MEDIA("mediaId");

  private final String counterId;

  BarcodeType(String counterId) {
    this.counterId = counterId;
  }
}
