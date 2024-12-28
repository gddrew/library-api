package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.randomlake.library.enums.BarcodeType;
import com.randomlake.library.enums.CardStatus;
import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Card;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.CardRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

  @Mock private CardRepository cardRepository;

  @Mock private SequenceGenerator sequenceGenerator;

  @Mock private PatronService patronService;

  @InjectMocks private CardService cardService;

  private Card card;
  private String barCodeId;

  @BeforeEach
  public void setup() {

    card = new Card();
    card.setCardId(6);
    card.setPatronId(1);
    card.setBarCodeId("12345");
    card.setStatus(CardStatus.ACTIVE);
  }

  @Test
  public void testGetAllCards() {
    when(cardRepository.findAll()).thenReturn(List.of(card));

    List<Card> result = cardService.getAllCards();

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(cardRepository, times(1)).findAll();
  }

  @Test
  public void testGetCardByID_Found() {
    when(cardRepository.findByCardId(6)).thenReturn(Optional.of(card));

    Card result = cardService.getCardById(6);

    assertNotNull(result);
    assertEquals(6, result.getCardId());
    verify(cardRepository, times(1)).findByCardId(6);
  }

  @Test
  public void testGetCardByID_NotFound() {
    when(cardRepository.findByCardId(5)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> cardService.getCardById(5));

    assertEquals(ExceptionType.CARD_NOT_FOUND, exception.getType());
    assertEquals("No card with this ID found in collection", exception.getMessage());
    verify(cardRepository, times(1)).findByCardId(5);
  }

  @Test
  public void testGetCardByPatronID_Found() {
    when(cardRepository.findByPatronId(1)).thenReturn(List.of(card));

    List<Card> result = cardService.getCardByPatronId(1);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(cardRepository, times(1)).findByPatronId(1);
  }

  @Test
  public void testGetCardByPatronID_NotFound() {
    when(cardRepository.findByPatronId(2)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> cardService.getCardByPatronId(2));

    assertEquals(ExceptionType.CARD_NOT_FOUND, exception.getType());
    assertEquals("No card for patron with this ID found in collection", exception.getMessage());
    verify(cardRepository, times(1)).findByPatronId(2);
  }

  @Test
  public void testGetActiveCardByPatronID_Found() {
    Card activeCard = new Card();
    activeCard.setStatus(CardStatus.ACTIVE);
    activeCard.setCardId(1);
    activeCard.setPatronId(1);

    Card inactiveCard = new Card();
    inactiveCard.setStatus(CardStatus.INACTIVE);
    inactiveCard.setCardId(2);
    inactiveCard.setPatronId(1);

    List<Card> cards = List.of(activeCard, inactiveCard);

    when(cardRepository.findByPatronId(1)).thenReturn(cards);

    Card result = cardService.getActiveCardByPatronId(1);

    assertNotNull(result);
    assertEquals(CardStatus.ACTIVE, result.getStatus());
    assertEquals(1, result.getCardId());
    verify(cardRepository, times(1)).findByPatronId(1);
  }

  @Test
  public void testGetActiveCardByPatronID_NotFound() {
    when(cardRepository.findByPatronId(1)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> cardService.getActiveCardByPatronId(1));

    assertEquals(ExceptionType.CARD_NOT_FOUND, exception.getType());
    assertEquals("No active card for this patron", exception.getMessage());
    verify(cardRepository, times(1)).findByPatronId(1);
  }

  @Test
  public void testUpdateCardStatus_Success() {
    // Arrange
    Card existingCard = new Card();
    existingCard.setCardId(1);
    existingCard.setStatus(CardStatus.ACTIVE);

    when(cardRepository.findByCardId(1)).thenReturn(Optional.of(existingCard));
    when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Card updatedCard = cardService.updateCardStatus(1, CardStatus.INACTIVE);

    // Assert
    assertNotNull(updatedCard);
    assertEquals(CardStatus.INACTIVE, updatedCard.getStatus());
    verify(cardRepository, times(1)).findByCardId(1);
    verify(cardRepository, times(1)).save(existingCard);
  }

  @Test
  public void testUpdateCardStatus_LostCardReactivation() {
    // Arrange
    Card lostCard = new Card();
    lostCard.setCardId(1);
    lostCard.setStatus(CardStatus.LOST);

    when(cardRepository.findByCardId(1)).thenReturn(Optional.of(lostCard));

    // Act & Assert
    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> cardService.updateCardStatus(1, CardStatus.ACTIVE));

    assertEquals(ExceptionType.CARD_LOST, exception.getType());
    assertEquals(
        "A lost card cannot be reactivated. A new card must be issued.", exception.getMessage());
    verify(cardRepository, times(1)).findByCardId(1);
    verify(cardRepository, never()).save(any(Card.class)); // Ensure save is never called
  }

  @Test
  public void testUpdateCardStatus_CardNotFound() {
    // Arrange
    when(cardRepository.findByCardId(1)).thenReturn(Optional.empty());

    // Act & Assert
    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> cardService.updateCardStatus(1, CardStatus.ACTIVE));

    assertEquals(ExceptionType.CARD_NOT_FOUND, exception.getType());
    assertEquals("No card with this ID found", exception.getMessage());
    verify(cardRepository, times(1)).findByCardId(1);
    verify(cardRepository, never()).save(any(Card.class));
  }

  @Test
  public void testAddNewCard_Success() {
    // Arrange
    int patronId = 1;
    Patron patron = new Patron();
    patron.setPatronId(patronId);
    patron.setStatus(PatronStatus.ACTIVE);

    when(patronService.getPatronById(patronId)).thenReturn(patron);
    when(cardRepository.existsByPatronIdAndStatus(patronId, CardStatus.ACTIVE)).thenReturn(false);
    when(sequenceGenerator.getNextSequenceValue(BarcodeType.CARD)).thenReturn(12345);
    when(sequenceGenerator.generateBarcode(BarcodeType.CARD, 12345)).thenReturn("12345");
    when(cardRepository.save(any(Card.class)))
        .thenAnswer(
            invocation -> {
              Card savedCard = invocation.getArgument(0);
              savedCard.setCardId(12345);
              return savedCard;
            });

    // Act
    Card savedCard = cardService.addNewCard(patronId);

    // Assert
    assertNotNull(savedCard);
    assertEquals(CardStatus.ACTIVE, savedCard.getStatus());
    assertEquals("12345", savedCard.getBarCodeId());
    assertEquals(patronId, savedCard.getPatronId());
    verify(patronService, times(1)).getPatronById(patronId);
    verify(cardRepository, times(1)).existsByPatronIdAndStatus(patronId, CardStatus.ACTIVE);
    verify(sequenceGenerator, times(1)).getNextSequenceValue(BarcodeType.CARD);
    verify(sequenceGenerator, times(1)).generateBarcode(BarcodeType.CARD, 12345);
    verify(cardRepository, times(1)).save(any(Card.class));
  }

  @Test
  public void testAddNewCard_PatronSuspended() {
    // Arrange
    int patronId = 1;
    Patron patron = new Patron();
    patron.setPatronId(patronId);
    patron.setStatus(PatronStatus.SUSPENDED);

    when(patronService.getPatronById(patronId)).thenReturn(patron);

    // Act & Assert
    GeneralException exception =
        assertThrows(GeneralException.class, () -> cardService.addNewCard(patronId));

    assertEquals(ExceptionType.PATRON_INELIGIBLE, exception.getType());
    assertEquals("Patron is suspended and cannot be issued a new card.", exception.getMessage());
    verify(patronService, times(1)).getPatronById(patronId);
    verify(cardRepository, never()).existsByPatronIdAndStatus(anyInt(), any(CardStatus.class));
    verify(cardRepository, never()).save(any(Card.class));
  }

  @Test
  public void testAddNewCard_PatronHasActiveCard() {
    // Arrange
    int patronId = 1;
    Patron patron = new Patron();
    patron.setPatronId(patronId);
    patron.setStatus(PatronStatus.ACTIVE);

    when(patronService.getPatronById(patronId)).thenReturn(patron);
    when(cardRepository.existsByPatronIdAndStatus(patronId, CardStatus.ACTIVE)).thenReturn(true);

    // Act & Assert
    GeneralException exception =
        assertThrows(GeneralException.class, () -> cardService.addNewCard(patronId));

    assertEquals(ExceptionType.CARD_ALREADY_EXISTS, exception.getType());
    assertEquals("Patron already has an active library card.", exception.getMessage());
    verify(patronService, times(1)).getPatronById(patronId);
    verify(cardRepository, times(1)).existsByPatronIdAndStatus(patronId, CardStatus.ACTIVE);
    verify(cardRepository, never()).save(any(Card.class));
  }

  @Test
  public void testAddNewCard_PatronNotFound() {
    // Arrange
    int patronId = 0;
    when(patronService.getPatronById(patronId))
        .thenThrow(
            new GeneralException(
                ExceptionType.PATRON_NOT_FOUND,
                "Patron with this ID not found",
                HttpStatus.NOT_FOUND));

    // Act & Assert
    GeneralException exception =
        assertThrows(GeneralException.class, () -> cardService.addNewCard(patronId));

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals("Patron with this ID not found", exception.getMessage());

    verify(patronService, times(1)).getPatronById(patronId);
    verify(cardRepository, never()).save(any(Card.class));
  }
}
