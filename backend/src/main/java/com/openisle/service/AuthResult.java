package com.openisle.service;

import com.openisle.model.User;
import lombok.Value;

/** Result for OAuth authentication indicating whether a new user was created. */
@Value
public class AuthResult {

  User user;
  boolean newUser;
}
