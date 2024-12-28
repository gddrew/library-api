package com.randomlake.library.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.randomlake.library.model.Card;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class CardRepositoryTest {

  @Mock private CardRepository cardRepository;

  private Card card;
  private int cardId;
  private int patronId;
  private String barcodeId;

  @BeforeEach
  public void setup() {

    MockitoAnnotations.openMocks(this);

    cardId = 12345678;
    patronId = 87654321;
    barcodeId = "9876";

    card = new Card();
    card.setCardId(cardId);
    card.setPatronId(patronId);
    card.setBarCodeId(barcodeId);
  }

  @Test
  public void findByCardIdSuccess() {
    when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(card));

    Optional<Card> foundCard = cardRepository.findByCardId(cardId);

    assertTrue(foundCard.isPresent());
    assertEquals(card.getCardId(), foundCard.get().getCardId());
    assertEquals(card.getBarCodeId(), foundCard.get().getBarCodeId());
  }

  @Test
  public void findByPatronIdSuccess() {
    List<Card> cards = List.of(card);
    when(cardRepository.findByPatronId(patronId)).thenReturn(cards);

    List<Card> foundCards = cardRepository.findByPatronId(patronId);

    assertEquals(1, foundCards.size());
  }

  @Test
  public void deleteByIdSuccess() {
    doNothing().when(cardRepository).deleteById(cardId);

    cardRepository.deleteById(cardId);

    verify(cardRepository, times(1)).deleteById(cardId);
  }
}
