package com.randomlake.library.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randomlake.library.config.TestSecurityConfig;
import com.randomlake.library.dto.LoanActionRequest;
import com.randomlake.library.dto.LoanResponse;
import com.randomlake.library.dto.TransactionResponse;
import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.ItemStatus;
import com.randomlake.library.enums.LoanStatus;
import com.randomlake.library.enums.TransactionType;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.exception.GlobalExceptionHandler;
import com.randomlake.library.model.Loan;
import com.randomlake.library.service.LoanService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(GlobalExceptionHandler.class)
@ContextConfiguration(classes = {TestSecurityConfig.class})
@WebMvcTest(LoanController.class)
public class LoanControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private LoanService loanService;

  private Loan loan;

  private int patronId;
  private int mediaId;
  private int loanId;
  private List<Integer> mediaIds;

  @BeforeEach
  public void setup() {

    patronId = 1;
    mediaId = 100;
    loanId = 1001;
    mediaIds = List.of(mediaId);

    loan = new Loan();
    loan.setLoanId(loanId);
    loan.setPatronId(patronId);
    loan.setStatus(LoanStatus.ACTIVE);

    Loan.LoanItem loanItem = new Loan.LoanItem();
    loanItem.setMediaId(mediaId);
    loanItem.setStatus(ItemStatus.CHECKED_OUT);

    loan.setItems(List.of(loanItem));

    Loan.TransactionLog transactionLog = new Loan.TransactionLog();
    transactionLog.setTransactionType(TransactionType.CHECKOUT);
    transactionLog.setTransactionDate(LocalDateTime.now());
    transactionLog.setMediaIds(List.of(mediaId));

    loan.setTransactionLog(List.of(transactionLog));
  }

  @Test
  public void testCheckoutItems_Success() throws Exception {
    // Prepare the response object
    TransactionResponse response = new TransactionResponse();
    response.setLoanId(loanId);
    response.setMessage("Items checked out successfully");
    // Prepare the list of media items
    TransactionResponse.MediaItem mediaItem = new TransactionResponse.MediaItem();
    mediaItem.setMediaTitle("Sample Book");
    mediaItem.setMediaStatus("CHECKED_OUT");
    mediaItem.setFormattedBarcodeId("3-9900-10000002-2");
    response.setMediaItems(List.of(mediaItem));

    // Prepare LoanActionRequest
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(patronId);
    loanActionRequest.setMediaIds(mediaIds);
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    // Mock the loanService.checkoutItems method
    when(loanService.processLoanAction(any(LoanActionRequest.class))).thenReturn(response);

    // Prepare the request body
    String loanActionRequestJson = objectMapper.writeValueAsString(loanActionRequest);

    // Perform the request
    mockMvc
        .perform(
            post("/api/loans/action")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loanActionRequestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.loanId").value(loanId))
        .andExpect(jsonPath("$.message").value("Items checked out successfully"))
        .andExpect(jsonPath("$.mediaItems[0].mediaTitle").value("Sample Book"))
        .andExpect(jsonPath("$.mediaItems[0].mediaStatus").value("CHECKED_OUT"))
        .andExpect(jsonPath("$.mediaItems[0].formattedBarcodeId").value("3-9900-10000002-2"));

    verify(loanService, times(1)).processLoanAction(any(LoanActionRequest.class));
  }

  @Test
  public void testReturnItems_Success() throws Exception {
    TransactionResponse response = new TransactionResponse();
    response.setLoanId(loanId);
    response.setMessage("Items returned successfully");
    TransactionResponse.MediaItem mediaItem = new TransactionResponse.MediaItem();
    mediaItem.setMediaTitle("Sample Book");
    mediaItem.setMediaStatus("AVAILABLE");
    mediaItem.setFormattedBarcodeId("3-9900-10000002-2");
    response.setMediaItems(List.of(mediaItem));

    // Prepare LoanActionRequest
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(patronId);
    loanActionRequest.setMediaIds(mediaIds);
    loanActionRequest.setTransactionType(TransactionType.RETURN);

    // Mock the loanService.processLoanAction method
    when(loanService.processLoanAction(any(LoanActionRequest.class))).thenReturn(response);

    // Serialize LoanActionRequest to JSON
    String loanActionRequestJson = objectMapper.writeValueAsString(loanActionRequest);

    mockMvc
        .perform(
            post("/api/loans/action")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loanActionRequestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.loanId").value(loanId))
        .andExpect(jsonPath("$.message").value("Items returned successfully"))
        .andExpect(jsonPath("$.mediaItems[0].mediaTitle").value("Sample Book"))
        .andExpect(jsonPath("$.mediaItems[0].mediaStatus").value("AVAILABLE"))
        .andExpect(jsonPath("$.mediaItems[0].formattedBarcodeId").value("3-9900-10000002-2"));

    verify(loanService, times(1)).processLoanAction(any(LoanActionRequest.class));
  }

  @Test
  public void testCheckoutItems_PatronNotFound() throws Exception {
    // Prepare LoanActionRequest
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(patronId);
    loanActionRequest.setMediaIds(mediaIds);
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    // Mock the service to throw an exception
    when(loanService.processLoanAction(any(LoanActionRequest.class)))
        .thenThrow(
            new GeneralException(
                ExceptionType.PATRON_NOT_FOUND, "Patron not found", HttpStatus.NOT_FOUND));

    // Serialize LoanActionRequest to JSON
    String loanActionRequestJson = objectMapper.writeValueAsString(loanActionRequest);

    // Perform the request
    mockMvc
        .perform(
            post("/api/loans/action")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loanActionRequestJson))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Patron not found"))
        .andExpect(jsonPath("$.errorCode").value("PATRON_NOT_FOUND"));

    verify(loanService, times(1)).processLoanAction(any(LoanActionRequest.class));
  }

  @Test
  public void testReturnItems_MediaNotFound() throws Exception {
    // Prepare LoanActionRequest
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(patronId);
    loanActionRequest.setMediaIds(mediaIds);
    loanActionRequest.setTransactionType(TransactionType.RETURN);

    // Mock the service to throw an exception
    when(loanService.processLoanAction(any(LoanActionRequest.class)))
        .thenThrow(
            new GeneralException(
                ExceptionType.MEDIA_NOT_FOUND, "Media item not found", HttpStatus.NOT_FOUND));

    // Serialize LoanActionRequest to JSON
    String loanActionRequestJson = objectMapper.writeValueAsString(loanActionRequest);

    // Perform the request
    mockMvc
        .perform(
            post("/api/loans/action")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loanActionRequestJson))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Media item not found"))
        .andExpect(jsonPath("$.errorCode").value("MEDIA_NOT_FOUND"));

    verify(loanService, times(1)).processLoanAction(any(LoanActionRequest.class));
  }

  @Test
  public void testGetLoanHistoryByPatronId_Success() throws Exception {
    // Prepare LoanResponse
    LoanResponse loanResponse = new LoanResponse();
    loanResponse.setLoanId(loanId);
    loanResponse.setPatronId(patronId);
    loanResponse.setStatus(LoanStatus.ACTIVE);
    // Additional fields can be set as necessary

    List<LoanResponse> loans = List.of(loanResponse);

    when(loanService.findLoansByPatronId(patronId)).thenReturn(loans);

    mockMvc
        .perform(
            get("/api/loans/history/patron/{patronId}", patronId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].loanId").value(loanId))
        .andExpect(jsonPath("$[0].patronId").value(patronId))
        .andExpect(jsonPath("$[0].status").value("ACTIVE"));

    verify(loanService, times(1)).findLoansByPatronId(patronId);
  }

  @Test
  public void testGetLoanHistoryByMediaId_Success() throws Exception {
    // Prepare LoanResponse
    LoanResponse loanResponse = new LoanResponse();
    loanResponse.setLoanId(loanId);
    loanResponse.setPatronId(patronId);
    loanResponse.setStatus(LoanStatus.ACTIVE);
    // Additional fields can be set as necessary

    List<LoanResponse> loans = List.of(loanResponse);

    when(loanService.findLoansByMediaId(mediaId)).thenReturn(loans);

    mockMvc
        .perform(
            get("/api/loans/history/media/{mediaId}", mediaId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].loanId").value(loanId))
        .andExpect(jsonPath("$[0].patronId").value(patronId))
        .andExpect(jsonPath("$[0].status").value("ACTIVE"));

    verify(loanService, times(1)).findLoansByMediaId(mediaId);
  }

  @Test
  public void testGetLoanHistoryByPatronId_NoContent() throws Exception {
    when(loanService.findLoansByPatronId(9999)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/loans/history/patron/9999").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verify(loanService, times(1)).findLoansByPatronId(9999);
  }

  @Test
  public void testGetLoanHistoryByMediaId_NoContent() throws Exception {
    when(loanService.findLoansByMediaId(8888)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/loans/history/media/8888").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verify(loanService, times(1)).findLoansByMediaId(8888);
  }

  @Test
  public void testDeleteLoanById_Success() throws Exception {
    doNothing().when(loanService).deleteLoanById(loanId);

    mockMvc
        .perform(
            delete("/api/loans/history/loan/{loanId}", loanId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verify(loanService, times(1)).deleteLoanById(loanId);
  }

  @Test
  public void testCheckoutItems_InvalidPatronId() throws Exception {
    int invalidPatronId = 909;

    // Prepare LoanActionRequest
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(invalidPatronId);
    loanActionRequest.setMediaIds(mediaIds);
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    // Mock the service to throw an exception
    when(loanService.processLoanAction(any(LoanActionRequest.class)))
        .thenThrow(
            new GeneralException(
                ExceptionType.PATRON_NOT_FOUND,
                "No patron found with ID: " + invalidPatronId,
                HttpStatus.NOT_FOUND));

    // Serialize LoanActionRequest to JSON
    String loanActionRequestJson = objectMapper.writeValueAsString(loanActionRequest);

    mockMvc
        .perform(
            post("/api/loans/action")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loanActionRequestJson))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("No patron found with ID: " + invalidPatronId))
        .andExpect(jsonPath("$.errorCode").value("PATRON_NOT_FOUND"));

    verify(loanService, times(1)).processLoanAction(any(LoanActionRequest.class));
  }

  @Test
  public void testDeleteLoanById_NotFound() throws Exception {
    int nonExistentLoanId = 9999;
    doThrow(
            new GeneralException(
                ExceptionType.INVALID_LOAN, "Loan not found", HttpStatus.NOT_FOUND))
        .when(loanService)
        .deleteLoanById(nonExistentLoanId);

    mockMvc
        .perform(
            delete("/api/loans/history/loan/{loanId}", nonExistentLoanId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Loan not found"))
        .andExpect(jsonPath("$.errorCode").value("INVALID_LOAN"));

    verify(loanService, times(1)).deleteLoanById(nonExistentLoanId);
  }
}
