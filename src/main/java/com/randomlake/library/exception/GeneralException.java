package com.randomlake.library.exception;

import com.randomlake.library.enums.ExceptionType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GeneralException extends RuntimeException {

  private final ExceptionType type;
  private final HttpStatus httpStatus;

  public GeneralException(ExceptionType type, String message, HttpStatus httpStatus) {
    super(message);
    this.type = type;
    this.httpStatus = httpStatus;
  }
}
