package com.randomlake.library.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randomlake.library.config.TestSecurityConfig;
import com.randomlake.library.dto.FineCreationRequest;
import com.randomlake.library.dto.FineUpdateRequest;
import com.randomlake.library.enums.FineType;
import com.randomlake.library.model.Fine;
import com.randomlake.library.service.FineService;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ContextConfiguration(classes = {TestSecurityConfig.class})
@WebMvcTest(FineController.class)
public class FineControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private FineService fineService;

  private Fine fine;
  private FineCreationRequest fineCreationRequest;
  private FineUpdateRequest fineUpdateRequest;

  @BeforeEach
  public void setUp() {
    fine = new Fine();
    fine.setFineId(1);
    fine.setPatronId(123);
    fine.setMediaId(456);
    fine.setFineType(FineType.OVERDUE_ITEM);
    fine.setAmount(10);
    fine.setDateAssessed(LocalDateTime.now());
    fine.setDatePaid(null);
    fine.setPaid(false);
    fine.setWaived(false);

    fineCreationRequest = new FineCreationRequest();
    fineCreationRequest.setPatronId(123);
    fineCreationRequest.setMediaId(456);
    fineCreationRequest.setFineType(FineType.OVERDUE_ITEM);
    fineCreationRequest.setAmount(10);

    fineUpdateRequest = new FineUpdateRequest();
    fineUpdateRequest.setAmount(5);
    fineUpdateRequest.setPaid(true);
    fineUpdateRequest.setWaived(false);
    fineUpdateRequest.setFineType(FineType.LOST_ITEM);
  }

  @Test
  public void createFine_Success() throws Exception {
    when(fineService.assessFine(anyInt(), anyInt(), any(FineType.class), anyInt()))
        .thenReturn(fine);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/fines/fine/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(fineCreationRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.fineId").value(1))
        .andExpect(jsonPath("$.patronId").value(123))
        .andExpect(jsonPath("$.mediaId").value(456))
        .andExpect(jsonPath("$.fineType").value("OVERDUE_ITEM"))
        .andExpect(jsonPath("$.amount").value(10))
        .andDo(print());
  }

  @Test
  public void testUpdateFine_Success() throws Exception {
    when(fineService.updateFine(anyInt(), anyInt(), anyBoolean(), anyBoolean(), any()))
        .thenReturn(fine);

    mockMvc
        .perform(
            put("/api/fines/fine/{fineId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fineUpdateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fineType").value("OVERDUE_ITEM"))
        .andExpect(jsonPath("$.amount").value("10"));
  }

  @Test
  public void getFinesByPatronId_Success() throws Exception {
    when(fineService.getFinesByPatronId(123)).thenReturn(Collections.singletonList(fine));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/fines/patron/123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].fineId").value(1))
        .andExpect(jsonPath("$[0].patronId").value(123))
        .andDo(print());
  }

  @Test
  public void getFinesByPatronId_NotFound() throws Exception {
    when(fineService.getFinesByPatronId(123)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/fines/patron/123"))
        .andExpect(status().isNotFound())
        .andExpect(MockMvcResultMatchers.content().string("[]"))
        .andDo(print());
  }

  @Test
  public void getFineById_Success() throws Exception {
    when(fineService.getFineById(1)).thenReturn(fine);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/fines/fine/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fineId").value(1))
        .andExpect(jsonPath("$.patronId").value(123))
        .andExpect(jsonPath("$.mediaId").value(456))
        .andDo(print());
  }

  @Test
  public void getFineById_NotFound() throws Exception {
    when(fineService.getFineById(63)).thenReturn(null);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/fines/fine/63"))
        .andExpect(status().isNotFound());
  }
}
