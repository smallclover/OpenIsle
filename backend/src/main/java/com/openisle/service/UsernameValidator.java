package com.openisle.service;

import com.openisle.exception.FieldException;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

/**
 * Simple validator for usernames.
 */
@Service
public class UsernameValidator {

  /**
   * Validate the username string.
   *
   * @param username the username to validate
   */
  public void validate(String username) {
    if (username == null || username.isEmpty()) {
      throw new FieldException("username", "Username cannot be empty");
    }

    if (NumberUtils.isDigits(username)) {
      throw new FieldException("username", "Username cannot be pure number");
    }
  }
}
