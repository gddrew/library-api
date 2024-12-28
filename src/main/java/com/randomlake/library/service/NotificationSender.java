package com.randomlake.library.service;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.PatronRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class NotificationSender {

  private final EmailService emailService;
  private final PatronRepository patronRepository;
  private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

  public NotificationSender(EmailService emailService, PatronRepository patronRepository) {
    this.emailService = emailService;
    this.patronRepository = patronRepository;
  }

  public void sendNotification(int patronId, String message) {
    // Fetch patron's email address based on patronId
    String recipientEmail = getEmailForPatron(patronId);

    // Send the email
    try {
      emailService.sendEmail(
          "librarian@anytownpubliclibrary.com", recipientEmail, "Library Notification", message);
      log.info("Email sent to {} with message length {}", recipientEmail, message.length());
    } catch (Exception e) {
      log.error("Failed to send email to " + recipientEmail, e);
    }
  }

  private String getEmailForPatron(int patronId) {
    return patronRepository
        .findByPatronId(patronId)
        .map(Patron::getEmailAddress)
        .orElseThrow(
            () ->
                new GeneralException(
                    ExceptionType.PATRON_NOT_FOUND, "Patron not found", HttpStatus.NOT_FOUND));
  }
}
