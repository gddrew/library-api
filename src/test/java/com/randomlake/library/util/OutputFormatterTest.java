package com.randomlake.library.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OutputFormatterTest {

  private OutputFormatter outputFormatter;

  @BeforeEach
  public void setup() {
    outputFormatter = new OutputFormatter();
  }

  @Test
  public void testFormatBarcodeId_ValidFormat() {
    String barcodeId = "12345678901234";
    String expectedFormat = "1-2345-67890123-4";

    String result = outputFormatter.formatBarcodeId(barcodeId);
    assertEquals(expectedFormat, result);
  }

  @Test
  public void testFormatBarcodeId_InvalidLength() {
    String invalidBarcodeId = "12345";

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              outputFormatter.formatBarcodeId(invalidBarcodeId);
            });

    assertEquals("Invalid barcode ID length", exception.getMessage());
  }

  @Test
  public void testFormatBarcodeID_Null() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              outputFormatter.formatBarcodeId(null);
            });

    assertEquals("Invalid barcode ID length", exception.getMessage());
  }
}
