package com.randomlake.library.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OutputFormatter {

  private static final Logger log = LoggerFactory.getLogger(OutputFormatter.class);

  /**
   * Formats the barcode ID by inserting dashes at specific positions.
   *
   * @param barcodeId The barcode ID to format.
   * @return The formatted barcode ID.
   * @throws IllegalArgumentException If the barcode ID is null or has an invalid length.
   */
  public String formatBarcodeId(String barcodeId) {
    // Ensure the barcode ID has the correct length
    if (barcodeId == null || barcodeId.length() != 14) {
      log.error("Invalid barcode ID length");
      throw new IllegalArgumentException("Invalid barcode ID length");
    }

    // Use regex to format the barcode ID
    return barcodeId.replaceAll("(.{1})(.{4})(.{8})(.{1})", "$1-$2-$3-$4");
  }
}
