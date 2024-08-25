package com.dom.employeemanager.handler;

import com.dom.employeemanager.exception.EmailAlreadyExistsException;
import com.dom.employeemanager.exception.PhoneNumberAlreadyExistsException;
import com.dom.employeemanager.exception.WrongPhoneNumberOrPasswordException;
import com.dom.employeemanager.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
  public ResponseEntity<Object> handlePhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
  }

  @ExceptionHandler(IllegalAccessException.class)
  public ResponseEntity<Object> handleGenericException(Exception ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
  }

  @ExceptionHandler(WrongPhoneNumberOrPasswordException.class)
  public ResponseEntity<ErrorResponse> handleWrongPhoneNumberOrPasswordException(Exception e) {
    ErrorResponse errorResponse = new ErrorResponse("ERROR_CODE", e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
  }
}
