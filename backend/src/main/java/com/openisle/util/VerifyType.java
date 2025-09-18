package com.openisle.util;

/**
 * 验证码类型
 * @author smallclover
 * @since  2025-09-08
 */
public enum VerifyType {
  REGISTER(1),
  RESET_PASSWORD(2);

  private final int code;

  VerifyType(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
