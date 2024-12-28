package com.randomlake.library.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.randomlake.library.config.LibraryConfig;
import com.randomlake.library.enums.BarcodeType;
import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;

class SequenceGeneratorTest {

  @Mock private LibraryConfig libraryConfig;

  @Mock private CheckDigitCalculator digitCalculator;

  @Mock private MongoTemplate mongoTemplate;

  @InjectMocks private SequenceGenerator sequenceGenerator;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGenerateBarcode_ForMedia() {
    // Arrange
    when(libraryConfig.getLibraryIdCode()).thenReturn("123");
    when(libraryConfig.getMediaBarcodePrefix()).thenReturn("MED");
    when(digitCalculator.calculateCheckDigit(anyString())).thenReturn(7);

    // Act
    String barcode = sequenceGenerator.generateBarcode(BarcodeType.MEDIA, 1000);

    // Assert
    assertEquals("MED12310007", barcode); // barcodeWithoutCheckDigit + checkDigit
  }

  @Test
  void testGenerateBarcode_ForCard() {
    // Arrange
    when(libraryConfig.getLibraryIdCode()).thenReturn("123");
    when(libraryConfig.getCardBarcodePrefix()).thenReturn("CRD");
    when(digitCalculator.calculateCheckDigit(anyString())).thenReturn(9);

    // Act
    String barcode = sequenceGenerator.generateBarcode(BarcodeType.CARD, 2000);

    // Assert
    assertEquals("CRD12320009", barcode);
  }

  @Test
  void testGetNextSequenceValue_ForMedia() {
    // Arrange
    Counter mockCounter = new Counter();
    mockCounter.setSequenceValue(1001);

    when(mongoTemplate.findAndModify(
            any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Counter.class)))
        .thenReturn(mockCounter);

    // Act
    int nextSequenceValue = sequenceGenerator.getNextSequenceValue(BarcodeType.MEDIA);

    // Assert
    assertEquals(1001, nextSequenceValue);
  }

  @Test
  void testGetNextSequenceValueForLoan() {
    // Arrange
    Counter mockCounter = new Counter();
    mockCounter.setSequenceValue(10000001);

    when(mongoTemplate.findAndModify(
            any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Counter.class)))
        .thenReturn(mockCounter);

    // Act
    int nextSequenceValue = sequenceGenerator.getNextSequenceValueForLoan();

    // Assert
    assertEquals(10000001, nextSequenceValue);
  }

  @Test
  void testGetNextSequenceValue_FailureToRetrieveSequence_throwsGeneralException() {
    // Arrange
    when(mongoTemplate.findAndModify(
            any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Counter.class)))
        .thenReturn(null); // Simulating failure to retrieve the sequence value

    // Act & Assert
    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              sequenceGenerator.getNextSequenceValue(
                  BarcodeType.MEDIA.toString()); // Using a sequence name
            });

    assertEquals(ExceptionType.SEQUENCE_GENERATION_FAILED, exception.getType());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    assertTrue(exception.getMessage().contains("Failed to generate sequence value for:"));
  }
}
