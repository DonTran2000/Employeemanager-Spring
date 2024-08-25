package com.dom.employeemanager.exception;

public class WrongPhoneNumberOrPasswordException extends RuntimeException {
  public WrongPhoneNumberOrPasswordException(String message) {
    super(message);
  }
}
