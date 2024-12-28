package com.randomlake.library.controller;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randomlake.library.config.TestSecurityConfig;
import com.randomlake.library.dto.EmailRequest;
import com.randomlake.library.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = {TestSecurityConfig.class})
@WebMvcTest(EmailController.class)
public class EmailControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private EmailService emailService;

  private EmailRequest emailRequest;

  @BeforeEach
  void setUp() {
    emailRequest = new EmailRequest();
    emailRequest.setFrom("test@example.com");
    emailRequest.setTo("recipient@example.com");
    emailRequest.setSubject("Test Subject");
    emailRequest.setBody("<p>This is a test email.</p>");
  }

  @Test
  void testSendHtmlEmail_Success() throws Exception {
    // Arrange
    doNothing()
        .when(emailService)
        .sendHtmlEmail(
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

    // Act and Assert
    mockMvc
        .perform(
            post("/api/send-html-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string("Email sent successfully"));
  }

  @Test
  void testSendEmail_Success() throws Exception {
    // Arrange
    doNothing()
        .when(emailService)
        .sendEmail(
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

    // Act and Assert
    mockMvc
        .perform(
            post("/api/send-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string("Email sent successfully"));
  }
}
