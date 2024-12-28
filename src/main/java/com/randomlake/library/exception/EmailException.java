package com.randomlake.library.exception;

/** Custom exception for email sending failures. */
public class EmailException extends RuntimeException {

  public EmailException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmailException(String message) {
    super(message);
  }
}
