package com.openisle.exception;

/**
 * Thrown when email sending fails so callers can surface a clear error upstream.
 */
public class EmailSendException extends RuntimeException {

  public EmailSendException(String message) {
    super(message);
  }

  public EmailSendException(String message, Throwable cause) {
    super(message, cause);
  }
}
