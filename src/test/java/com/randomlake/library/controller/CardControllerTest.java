package com.randomlake.library.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randomlake.library.config.TestSecurityConfig;
import com.randomlake.library.dto.CardStatusUpdateRequest;
import com.randomlake.library.enums.CardStatus;
import com.randomlake.library.model.Card;
import com.randomlake.library.service.CardService;
import java.time.LocalDateTime;
import java.util.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = {TestSecurityConfig.class})
@WebMvcTest(CardController.class)
public class CardControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CardService cardService;

  // Declare reusable card instances
  private Card card1;
  private Card card2;

  @BeforeEach
  public void setup() {

    card1 =
        new Card(
            new ObjectId(),
            12345,
            LocalDateTime.of(2019, 4, 6, 10, 2, 45, 33),
            "19900101123450",
            1, // patron
            CardStatus.ACTIVE,
            LocalDateTime.of(2023, 12, 15, 13, 45, 0, 0),
            LocalDateTime.of(2024, 9, 2, 15, 52, 17, 2));
    card2 =
        new Card(
            new ObjectId(),
            67890,
            LocalDateTime.of(1988, 9, 1, 14, 2, 45, 33),
            "19900100678905",
            2,
            CardStatus.RESTRICTED,
            LocalDateTime.of(2023, 12, 15, 13, 45, 0, 0),
            LocalDateTime.of(2023, 5, 2, 15, 52, 17, 2));
  }

  @Test
  public void testGetAllCards_Success() throws Exception {
    // Mock the service to return the card list
    List<Card> cardList = Arrays.asList(card1, card2);
    when(cardService.getAllCards()).thenReturn(cardList);

    // Perform the GET request and verify the response
    mockMvc
        .perform(get("/api/cards"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].cardId").value(12345))
        .andExpect(jsonPath("$[0].patronId").value(1))
        .andExpect(jsonPath("$[1].cardId").value(67890))
        .andExpect(jsonPath("$[1].patronId").value(2));
  }

  @Test
  public void testGetCardById_Success() throws Exception {

    when(cardService.getCardById(12345)).thenReturn(card1);

    mockMvc
        .perform(get("/api/cards/{cardId}", 12345))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cardId", is(12345)))
        .andExpect(jsonPath("$.barCodeId", is("19900101123450")))
        .andExpect(jsonPath("$.patronId", is(1)))
        .andExpect(jsonPath("$.status", is("ACTIVE")));
  }

  @Test
  public void testGetCardByPatronId_Success() throws Exception {
    List<Card> cardList = Collections.singletonList(card1);

    when(cardService.getCardByPatronId(1)).thenReturn(cardList);

    mockMvc
        .perform(get("/api/cards/patron/{patronId}", 1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].cardId", is(12345)))
        .andExpect(jsonPath("$[0].barCodeId", is("19900101123450")))
        .andExpect(jsonPath("$[0].patronId", is(1)))
        .andExpect(jsonPath("$[0].status", is("ACTIVE")));
  }

  @Test
  public void testGetActiveCardByPatronId_Success() throws Exception {
    when(cardService.getActiveCardByPatronId(1)).thenReturn(card1);

    mockMvc
        .perform(get("/api/cards/patron/1/active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cardId", is(12345)))
        .andExpect(jsonPath("$.barCodeId", is("19900101123450")))
        .andExpect(jsonPath("$.patronId", is(1)))
        .andExpect(jsonPath("$.status", is("ACTIVE")));
  }

  @Test
  public void testAddCard_Success() throws Exception {

    when(cardService.addNewCard(1)).thenReturn(card1);

    mockMvc
        .perform(post("/api/cards/patron/1").contentType("application/json"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.patronId", is(1)))
        .andExpect(jsonPath("$.cardId", is(12345)));
  }

  @Test
  public void testUpdateCardStatus_Success() throws Exception {

    CardStatus newStatus = CardStatus.LOST;
    card1.setStatus(newStatus);

    when(cardService.updateCardStatus(1, newStatus)).thenReturn(card1);

    CardStatusUpdateRequest request = new CardStatusUpdateRequest();
    request.setNewStatus(newStatus);

    mockMvc
        .perform(
            put("/api/cards/{cardId}/status", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("LOST"));

    verify(cardService).updateCardStatus(1, newStatus);
  }
}
