package com.randomlake.library.repository;

import com.randomlake.library.enums.CardStatus;
import com.randomlake.library.model.Card;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CardRepository extends MongoRepository<Card, ObjectId>, CardRepositoryCustom {

  Optional<Card> findByCardId(int cardId);

  List<Card> findByPatronId(int patronId);

  void deleteById(int cardId);

  boolean existsByPatronIdAndStatus(int patronId, CardStatus cardStatus);
}
