package com.randomlake.library.controller;

import com.randomlake.library.enums.BarcodeType;
import com.randomlake.library.util.BarcodeUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdatesController {

  @Autowired BarcodeUpdater barcodeUpdater;

  @PatchMapping("/api/collection/media/update/barcodes")
  public ResponseEntity<String> updateMediaBarcodes() {
    barcodeUpdater.updateExistingEntitiesWithBarcodes(BarcodeType.MEDIA);
    return ResponseEntity.ok("Barcode updated for existing media");
  }

  @PatchMapping("/api/cards/update/barcodes")
  public ResponseEntity<String> updateCardBarcodes() {
    barcodeUpdater.updateExistingEntitiesWithBarcodes(BarcodeType.CARD);
    return ResponseEntity.ok("Barcode updated for existing cards");
  }
}
