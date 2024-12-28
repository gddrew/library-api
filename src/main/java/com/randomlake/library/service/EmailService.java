package com.randomlake.library.service;

import com.randomlake.library.exception.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final JavaMailSender mailSender;

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * Sends a plain text email.
   *
   * @param from Sender's email address
   * @param to Recipient's email address
   * @param subject Subject of the email
   * @param body Body content of the email
   */
  public void sendEmail(String from, String to, String subject, String body) {

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(from);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);

      mailSender.send(message);
      log.info("Email sent successfully to {}", to);
    } catch (MailException e) {
      log.error("Failed to send email to {}", to, e);
      throw new EmailException("Failed to send email to " + to, e);
    }
  }

  /**
   * Sends an HTML email.
   *
   * @param to Recipient's email address
   * @param subject Subject of the email
   * @param htmlBody HTML content of the email
   */
  public void sendHtmlEmail(String from, String to, String subject, String htmlBody) {

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(from);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlBody, true);

      mailSender.send(message);
      log.info("Email sent to {}", to);
    } catch (MailException | MessagingException e) {
      log.error("Failed to send email", e);
      throw new EmailException("Failed to send HTML email to " + to, e);
    }
  }
}
