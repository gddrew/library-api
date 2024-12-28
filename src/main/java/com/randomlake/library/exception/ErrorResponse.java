package com.randomlake.library.exception;

import com.randomlake.library.enums.ExceptionType;
import lombok.Getter;
import lombok.Setter;

public class ErrorResponse {

  @Getter @Setter private String message;

  @Getter @Setter private String errorCode;

  public ErrorResponse(String message, ExceptionType errorCode) {
    this.message = message;
    this.errorCode = errorCode.name();
  }
}
