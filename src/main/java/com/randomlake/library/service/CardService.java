package com.randomlake.library.service;

import com.randomlake.library.dto.ReportCardPatron;
import com.randomlake.library.enums.BarcodeType;
import com.randomlake.library.enums.CardStatus;
import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Card;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.CardRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

  private final CardRepository cardRepository;
  private final PatronService patronService;
  private final SequenceGenerator sequenceGenerator;

  private static final Logger log = LoggerFactory.getLogger(CardService.class);

  public CardService(
      CardRepository cardRepository,
      PatronService patronService,
      SequenceGenerator sequenceGenerator) {
    this.cardRepository = cardRepository;
    this.patronService = patronService;
    this.sequenceGenerator = sequenceGenerator;
  }

  public List<Card> getAllCards() {
    return cardRepository.findAll();
  }

  public Card getCardById(int cardId) {
    return cardRepository
        .findByCardId(cardId)
        .orElseThrow(
            () -> {
              log.error("No card with ID {} found in collection", cardId);
              return new GeneralException(
                  ExceptionType.CARD_NOT_FOUND,
                  "No card with this ID found in collection",
                  HttpStatus.NOT_FOUND);
            });
  }

  public List<Card> getCardByPatronId(int patronId) {
    List<Card> cards = cardRepository.findByPatronId(patronId);
    if (cards.isEmpty()) {
      log.error("No card for patron with ID {} found in collection", patronId);
      throw new GeneralException(
          ExceptionType.CARD_NOT_FOUND,
          "No card for patron with this ID found in collection",
          HttpStatus.NOT_FOUND);
    }
    return cards;
  }

  public Card getActiveCardByPatronId(int patronId) {
    return cardRepository.findByPatronId(patronId).stream()
        .filter(c -> c.getStatus() == CardStatus.ACTIVE)
        .findFirst()
        .orElseThrow(
            () -> {
              log.error("No active card for patron with ID {} found", patronId);
              return new GeneralException(
                  ExceptionType.CARD_NOT_FOUND,
                  "No active card for this patron",
                  HttpStatus.NOT_FOUND);
            });
  }

  // Method to update card status
  @Transactional
  public Card updateCardStatus(int cardId, CardStatus newStatus) {
    Card card =
        cardRepository
            .findByCardId(cardId)
            .orElseThrow(
                () -> {
                  log.error("No card with ID {} found for update", cardId);
                  return new GeneralException(
                      ExceptionType.CARD_NOT_FOUND,
                      "No card with this ID found",
                      HttpStatus.NOT_FOUND);
                });

    // Check if the card is lost and trying to reactivate it
    if (card.getStatus() == CardStatus.LOST && newStatus == CardStatus.ACTIVE) {
      log.error("A lost card cannot be reactivated. A new card must be issued.");
      throw new GeneralException(
          ExceptionType.CARD_LOST,
          "A lost card cannot be reactivated. A new card must be issued.",
          HttpStatus.FORBIDDEN);
    }

    // Update the card status if not trying to reactivate a lost card
    card.setStatus(newStatus);
    card.setLastUpdateDate(LocalDateTime.now());
    Card updatedCard = cardRepository.save(card);

    log.info("Updated status of card ID {} to {}", cardId, newStatus);
    return updatedCard;
  }

  @Transactional
  public Card addNewCard(int patronId) {
    Patron patron = patronService.getPatronById(patronId);

    // Check if patron is suspended
    if (patron.getStatus() == PatronStatus.SUSPENDED) {
      log.error("Patron is suspended and cannot be issued a new card");
      throw new GeneralException(
          ExceptionType.PATRON_INELIGIBLE,
          "Patron is suspended and cannot be issued a new card.",
          HttpStatus.FORBIDDEN);
    }

    // Check if the patron already has an active card
    boolean hasActiveCard =
        cardRepository.existsByPatronIdAndStatus(patron.getPatronId(), CardStatus.ACTIVE);

    if (hasActiveCard) {
      log.error("Patron already has an active library card");
      throw new GeneralException(
          ExceptionType.CARD_ALREADY_EXISTS,
          "Patron already has an active library card.",
          HttpStatus.CONFLICT);
    }

    // Create a new card instance
    Card card = new Card();
    card.setPatronId(patronId);

    // Generate the next card number and barcode
    int nextCardNumber = sequenceGenerator.getNextSequenceValue(BarcodeType.CARD);
    card.setCardId(nextCardNumber);

    String barCode = sequenceGenerator.generateBarcode(BarcodeType.CARD, nextCardNumber);
    card.setBarCodeId(barCode);

    // Set creation and update timestamps
    LocalDateTime now = LocalDateTime.now();
    card.setCreatedDate(now);
    card.setLastUpdateDate(now);
    card.setStatus(CardStatus.ACTIVE);

    // Save the new card to the database
    Card savedCard = cardRepository.save(card);

    log.info(
        "Issued new card with ID {} to patron ID {}",
        savedCard.getCardId(),
        savedCard.getPatronId());
    return savedCard;
  }

  public List<ReportCardPatron> getReportCardPatron(
      Optional<Integer> cardIdOpt, Optional<Integer> patronIdOpt) {
    return cardRepository.getReportCardPatron(cardIdOpt, patronIdOpt);
  }
}
