package com.randomlake.library.util;

import com.randomlake.library.config.LibraryConfig;
import com.randomlake.library.enums.BarcodeType;
import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SequenceGenerator {

  private final LibraryConfig libraryConfig;
  private final CheckDigitCalculator digitCalculator;
  private final MongoTemplate mongoTemplate;

  private static final Logger log = LoggerFactory.getLogger(SequenceGenerator.class);

  @Autowired
  public SequenceGenerator(
      LibraryConfig libraryConfig,
      CheckDigitCalculator digitCalculator,
      MongoTemplate mongoTemplate) {
    this.libraryConfig = libraryConfig;
    this.digitCalculator = digitCalculator;
    this.mongoTemplate = mongoTemplate;
  }

  public String generateBarcode(BarcodeType type, int sequenceValue) {
    String prefix = getPrefixForType(type);
    String barcodeWithoutCheckDigit = prefix + libraryConfig.getLibraryIdCode() + sequenceValue;

    int checkDigit = digitCalculator.calculateCheckDigit(barcodeWithoutCheckDigit);
    return barcodeWithoutCheckDigit + checkDigit;
  }

  // Prefix for library cards and media items as per configuration
  private String getPrefixForType(BarcodeType type) {
    return switch (type) {
      case CARD -> libraryConfig.getCardBarcodePrefix();
      case MEDIA -> libraryConfig.getMediaBarcodePrefix();
      default -> {
        log.error("Unknown BarcodeType: {}", type);
        throw new IllegalArgumentException("Unknown BarcodeType: " + type);
      }
    };
  }

  public int getNextSequenceValue(String sequenceName) {
    Query query = new Query(Criteria.where("_id").is(sequenceName));
    Update update = new Update().inc("sequence_value", 1);
    FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

    Counter counter = mongoTemplate.findAndModify(query, update, options, Counter.class);

    if (counter == null) {
      log.error("Unable to get sequence value for sequence: {}", sequenceName);
      throw new GeneralException(
          ExceptionType.SEQUENCE_GENERATION_FAILED,
          "Failed to generate sequence value for: " + sequenceName,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return counter.getSequenceValue();
  }

  public int getNextSequenceValue(BarcodeType type) {
    return getNextSequenceValue(type.getCounterId());
  }

  // Helper methods for non-barcode sequences
  // Patrons and loans have their own sequence names and no barcode is generated

  public int getNextSequenceValueForPatron() {
    return getNextSequenceValue("patronId");
  }

  public int getNextSequenceValueForLoan() {
    return getNextSequenceValue("loanId");
  }

  public int getNextSequenceValueForFine() {
    return getNextSequenceValue("fineId");
  }

  public int getNextSequenceValueForInvoice() {
    return getNextSequenceValue("invoiceId");
  }
}
