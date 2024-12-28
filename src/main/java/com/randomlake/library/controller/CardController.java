package com.randomlake.library.controller;

import com.randomlake.library.dto.CardResponse;
import com.randomlake.library.dto.CardStatusUpdateRequest;
import com.randomlake.library.mapper.CardMapper;
import com.randomlake.library.model.Card;
import com.randomlake.library.service.CardService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardController {

  private final CardService cardService;

  public CardController(CardService cardService) {
    this.cardService = cardService;
  }

  @GetMapping
  public ResponseEntity<List<CardResponse>> getAllCards() {
    List<Card> cards = cardService.getAllCards();
    List<CardResponse> dtoList =
        cards.stream().map(CardMapper::toCardResponse).collect(Collectors.toList());
    return new ResponseEntity<>(dtoList, HttpStatus.OK);
  }

  @GetMapping("/{cardId}")
  public ResponseEntity<CardResponse> getCardByID(@PathVariable int cardId) {
    Card card = cardService.getCardById(cardId);
    CardResponse response = CardMapper.toCardResponse(card);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/patron/{patronId}")
  public ResponseEntity<List<CardResponse>> getCardsByPatronId(@PathVariable int patronId) {
    List<Card> cards = cardService.getCardByPatronId(patronId);
    List<CardResponse> dtoList =
        cards.stream().map(CardMapper::toCardResponse).collect(Collectors.toList());
    return new ResponseEntity<>(dtoList, HttpStatus.OK);
  }

  @GetMapping("/patron/{patronId}/active")
  public ResponseEntity<CardResponse> getActiveCardByPatronId(@PathVariable int patronId) {
    Card card = cardService.getActiveCardByPatronId(patronId);
    CardResponse activeCard = CardMapper.toCardResponse(card);
    return new ResponseEntity<>(activeCard, HttpStatus.OK);
  }

  @PostMapping("/patron/{patronId}")
  public ResponseEntity<CardResponse> addNewCard(@PathVariable int patronId) {
    Card card = cardService.addNewCard(patronId);
    CardResponse savedCard = CardMapper.toCardResponse(card);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedCard);
  }

  @PutMapping("/{cardId}/status")
  public ResponseEntity<CardResponse> updateCardStatus(
      @PathVariable int cardId, @Valid @RequestBody CardStatusUpdateRequest request) {
    Card card = cardService.updateCardStatus(cardId, request.getNewStatus());
    CardResponse updatedCard = CardMapper.toCardResponse(card);
    return ResponseEntity.ok(updatedCard);
  }
}
