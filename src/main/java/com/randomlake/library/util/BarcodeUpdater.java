package com.randomlake.library.util;

import com.randomlake.library.enums.BarcodeType;
import com.randomlake.library.model.Card;
import com.randomlake.library.model.Media;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class BarcodeUpdater {

  @Autowired private MongoTemplate mongoTemplate;

  @Autowired private SequenceGenerator sequenceGenerator;

  private static final Logger log = LoggerFactory.getLogger(BarcodeUpdater.class);

  public void updateExistingEntitiesWithBarcodes(BarcodeType type) {
    List<?> entities;
    String idField;
    String barcodeField;
    Class<?> entityClass;

    switch (type) {
      case CARD:
        idField = "cardId";
        barcodeField = "bar_code";
        entityClass = Card.class;
        break;
      case MEDIA:
        idField = "mediaId";
        barcodeField = "bar_code";
        entityClass = Media.class;
        break;
      default:
        throw new IllegalArgumentException("Unknown BarcodeType: " + type);
    }

    // Find all entities where the barcode is null or not set
    Query query = new Query(Criteria.where(barcodeField).is(null));
    entities = mongoTemplate.find(query, entityClass);

    // Iterate over each entity and update the barcode
    for (Object entity : entities) {
      int sequenceValue;
      if (entity instanceof Media) {
        sequenceValue = ((Media) entity).getMediaId();
      } else if (entity instanceof Card) {
        sequenceValue = ((Card) entity).getCardId();
      } else {
        log.error("Unknown entity type: {}", entity.getClass().getSimpleName());
        throw new IllegalArgumentException(
            "Unknown entity type: " + entity.getClass().getSimpleName());
      }

      // Generate the barcode number using SequenceGenerator
      String barCode = sequenceGenerator.generateBarcode(type, sequenceValue);

      // Update the entity's barcode field and last_update_date
      Update update =
          new Update().set(barcodeField, barCode).set("last_update_date", LocalDateTime.now());

      mongoTemplate.updateFirst(
          Query.query(Criteria.where(idField).is(sequenceValue)), update, entityClass);
    }

    System.out.println(
        "Barcodes updated for all existing "
            + type.name().toLowerCase()
            + " entities without barcodes.");
  }
}
