package com.randomlake.library.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.randomlake.library.config.TestSecurityConfig;
import com.randomlake.library.enums.BarcodeType;
import com.randomlake.library.util.BarcodeUpdater;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = {TestSecurityConfig.class})
@WebMvcTest(UpdatesController.class)
public class UpdatesControllerTest {

  @MockitoBean private BarcodeUpdater barcodeUpdater;

  @InjectMocks private UpdatesController updatesController;

  @Autowired private MockMvc mockMvc;

  @Test
  void testUpdateMediaBarcodes() throws Exception {
    mockMvc
        .perform(
            patch("/api/collection/media/update/barcodes").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("Barcode updated for existing media"));

    verify(barcodeUpdater).updateExistingEntitiesWithBarcodes(BarcodeType.MEDIA);
  }

  @Test
  void testUpdateCardBarcodes() throws Exception {
    mockMvc
        .perform(patch("/api/cards/update/barcodes").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("Barcode updated for existing cards"));

    verify(barcodeUpdater).updateExistingEntitiesWithBarcodes(BarcodeType.CARD);
  }
}
