package com.dom.employeemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  /*
  public void sendSimpleEmail(String toEmail, String subject, String body) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("trandon1811@gmail.com");  // Thay bằng email của bạn
    message.setTo(toEmail);
    message.setSubject(subject);
    message.setText(body);

    mailSender.send(message);
    System.out.println("Mail sent successfully...");
  }
  */

  // OTP
  public void sendOtpMessage(String to, String subject, String body) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom("trandon1811@gmail.com");
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);

      mailSender.send(message);
      System.out.println("Mail sent successfully to " + to);
    } catch (MailException ex) {
      System.err.println("Error while sending email: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

}

