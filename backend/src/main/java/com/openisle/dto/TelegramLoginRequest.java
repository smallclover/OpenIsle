package com.openisle.dto;

import lombok.Data;

/** Request for Telegram login. */
@Data
public class TelegramLoginRequest {

  private String id;
  private String firstName;
  private String lastName;
  private String username;
  private String photoUrl;
  private Long authDate;
  private String hash;
  private String inviteToken;
}
