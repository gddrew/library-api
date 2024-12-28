package com.randomlake.library.mapper;

import com.randomlake.library.dto.CardResponse;
import com.randomlake.library.model.Card;

public class CardMapper {

  public static CardResponse toCardResponse(Card card) {
    if (card == null) {
      return null;
    }

    CardResponse response = new CardResponse();
    response.setCardId(card.getCardId());
    response.setBarCodeId(card.getBarCodeId());
    response.setPatronId(card.getPatronId());
    response.setStatus(card.getStatus());
    response.setCreatedDate(card.getCreatedDate());
    response.setLastUpdateDate(card.getLastUpdateDate());
    response.setLastUsedDate(card.getLastUsedDate());

    return response;
  }

  public static Card toCardEntity(CardResponse cardResponse) {
    if (cardResponse == null) {
      return null;
    }

    Card card = new Card();
    card.setCardId(cardResponse.getCardId());
    card.setBarCodeId(cardResponse.getBarCodeId());
    card.setPatronId(cardResponse.getPatronId());
    card.setStatus(cardResponse.getStatus());
    card.setCreatedDate(cardResponse.getCreatedDate());
    card.setLastUpdateDate(cardResponse.getLastUpdateDate());
    card.setLastUsedDate(cardResponse.getLastUsedDate());

    return card;
  }
}
