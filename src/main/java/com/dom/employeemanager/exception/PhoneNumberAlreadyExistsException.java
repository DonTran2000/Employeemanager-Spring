package com.dom.employeemanager.exception;

public class PhoneNumberAlreadyExistsException extends RuntimeException {
  public PhoneNumberAlreadyExistsException(String message) {
    super(message);
  }
}
