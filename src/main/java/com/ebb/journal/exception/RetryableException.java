package com.ebb.journal.exception;

public class RetryableException extends RuntimeException {

  public RetryableException(String message) {
    super(message);
  }

  public RetryableException(String message, Throwable e) {
    super(message, e);
  }
}
