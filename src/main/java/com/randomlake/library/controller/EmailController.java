package com.randomlake.library.controller;

import com.randomlake.library.dto.EmailRequest;
import com.randomlake.library.service.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

  private final EmailService emailService;

  public EmailController(EmailService emailService) {
    this.emailService = emailService;
  }

  @PostMapping("/api/send-html-email")
  public String sendHtmlEmail(@RequestBody EmailRequest emailRequest) {
    emailService.sendHtmlEmail(
        emailRequest.getFrom(),
        emailRequest.getTo(),
        emailRequest.getSubject(),
        emailRequest.getBody());
    return "Email sent successfully";
  }

  @PostMapping("/api/send-email")
  public String sendEmail(@RequestBody EmailRequest emailRequest) {
    emailService.sendEmail(
        emailRequest.getFrom(),
        emailRequest.getTo(),
        emailRequest.getSubject(),
        emailRequest.getBody());
    return "Email sent successfully";
  }
}
