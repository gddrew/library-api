package com.randomlake.library.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randomlake.library.config.TestSecurityConfig;
import com.randomlake.library.dto.InvoiceRequest;
import com.randomlake.library.model.Invoice;
import com.randomlake.library.service.InvoiceService;
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
@WebMvcTest(InvoiceController.class)
public class InvoiceControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private InvoiceService invoiceService;

  private Invoice invoice;
  private InvoiceRequest invoiceRequest;

  @BeforeEach
  void setUp() {
    invoice = new Invoice();
    invoice.setInvoiceId(100);
    invoice.setPatronId(1);
    invoice.setCampaign("Some campaign");
    invoice.setAmount(1500);
    invoice.setStatus("pending");

    invoiceRequest = new InvoiceRequest();
    invoiceRequest.setPatronId(1);
    invoiceRequest.setCampaign("Some campaign");
    invoiceRequest.setAmount(1500);
    invoiceRequest.setStatus("pending");
  }

  @Test
  public void testCreateInvoice_Success() throws Exception {
    when(invoiceService.createInvoice(anyInt(), anyInt(), any(), any())).thenReturn(invoice);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/invoices/invoice/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invoiceRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.invoiceId").value(100))
        .andExpect(jsonPath("$.patronId").value(1))
        .andExpect(jsonPath("$.amount").value(1500))
        .andExpect(jsonPath("$.status").value("pending"))
        .andExpect(jsonPath("$.campaign").value("Some campaign"))
        .andDo(print());
  }

  @Test
  public void testUpdateInvoice_Success() throws Exception {
    when(invoiceService.updateInvoice(anyInt(), anyInt(), anyInt(), any(), any()))
        .thenReturn(invoice);

    mockMvc
        .perform(
            put("/api/invoices/invoice/{invoiceId}", 100)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoiceRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.campaign").value("Some campaign"))
        .andExpect(jsonPath("$.amount").value(1500));
  }

  @Test
  public void testGetInvoiceByPatronId_Success() throws Exception {
    when(invoiceService.getInvoicesByPatronId(1)).thenReturn(Collections.singletonList(invoice));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/invoices/invoice/patron/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].patronId").value(1))
        .andDo(print());
  }

  @Test
  public void testGetInvoiceByPatronId_NotFound() throws Exception {
    when(invoiceService.getInvoicesByPatronId(1)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/invoices/invoice/patron/1"))
        .andExpect(status().isNotFound())
        .andExpect(MockMvcResultMatchers.content().string("[]"))
        .andDo(print());
  }

  @Test
  public void testGetInvoiceIdByInvoiceId_Success() throws Exception {
    when(invoiceService.getInvoiceById(100)).thenReturn(invoice);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/invoices/invoice/100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.patronId").value(1))
        .andExpect(jsonPath("$.amount").value(1500))
        .andDo(print());
  }

  @Test
  public void testGetInvoiceByInvoiceId_NotFound() throws Exception {
    when(invoiceService.getInvoiceById(200)).thenReturn(null);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/invoices/invoice/200"))
        .andExpect(status().isNotFound());
  }
}
