package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  @Mock private MimeMessage mimeMessage;

  @InjectMocks private EmailService emailService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testSendEmail_Success() {
    // Arrange
    String from = "test@example.com";
    String to = "recipient@example.com";
    String subject = "Test Subject";
    String body = "Test Body";

    // Act
    emailService.sendEmail(from, to, subject, body);

    // Assert
    ArgumentCaptor<SimpleMailMessage> messageCaptor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender, times(1)).send(messageCaptor.capture());

    SimpleMailMessage sentMessage = messageCaptor.getValue();
    assertNotNull(sentMessage.getTo());
    assertEquals(from, sentMessage.getFrom());
    assertEquals(to, sentMessage.getTo()[0]);
    assertEquals(subject, sentMessage.getSubject());
    assertEquals(body, sentMessage.getText());
  }

  @Test
  public void testSendEmail_Failure() {
    // Arrange
    String from = "test@example.com";
    String to = "recipient@example.com";
    String subject = "Test Subject";
    String body = "Test Body";

    doThrow(new MailException("Mail sending failed") {})
        .when(mailSender)
        .send(any(SimpleMailMessage.class));

    // Act & Assert
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              emailService.sendEmail(from, to, subject, body);
            });

    assertEquals("Failed to send email to recipient@example.com", exception.getMessage());
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }
}
