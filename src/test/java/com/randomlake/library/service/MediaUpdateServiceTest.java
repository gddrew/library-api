package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.randomlake.library.enums.MediaStatus;
import com.randomlake.library.model.Media;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class MediaUpdateServiceTest {

  private MediaUpdateService mediaUpdateService;
  private Media media;

  @BeforeEach
  void setup() {
    mediaUpdateService = new MediaUpdateService();
    media = new Media();
  }

  @Test
  public void testApplyPartialUpdate_ValidFields() {
    Map<String, Object> updates = new HashMap<>();
    updates.put("mediaTitle", "Updated Title");
    updates.put("authorName", "Updated Author");
    updates.put("isbnId", "1234567890");
    updates.put("numberPages", 300);
    updates.put("acquisitionDate", LocalDate.of(2021, 10, 1));
    updates.put("status", "AVAILABLE");

    mediaUpdateService.applyPartialUpdates(media, updates);

    assertEquals("Updated Title", media.getMediaTitle());
    assertEquals("Updated Author", media.getAuthorName());
    assertEquals("1234567890", media.getIsbnId());
    assertEquals(300, media.getNumberPages());
    assertEquals(LocalDate.of(2021, 10, 1), media.getAcquisitionDate());
    assertEquals(MediaStatus.AVAILABLE, media.getStatus());
    assertNotNull(media.getLastUpdateDate());
  }

  @Test
  void testApplyPartialUpdates_InvalidNumberPages_ThrowsException() {
    Map<String, Object> updates = new HashMap<>();
    updates.put("numberPages", "invalid");

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              mediaUpdateService.applyPartialUpdates(media, updates);
            });

    assertEquals("Invalid type for numberPages. Expected an Integer.", exception.getMessage());
  }

  @Test
  void testApplyPartialUpdates_InvalidStatus_ThrowsException() {
    Map<String, Object> updates = new HashMap<>();
    updates.put("status", "INVALID_STATUS");

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              mediaUpdateService.applyPartialUpdates(media, updates);
            });

    assertEquals("Invalid media status: INVALID_STATUS", exception.getMessage());
  }

  @Test
  void testApplyFullUpdate_AllFields() {
    Media fullUpdate = new Media();
    fullUpdate.setMediaTitle("New Title");
    fullUpdate.setAuthorName("New Author");
    fullUpdate.setIsbnId("0987654321");
    fullUpdate.setNumberPages(500);
    fullUpdate.setAcquisitionDate(LocalDate.of(2022, 5, 10));

    mediaUpdateService.applyFullUpdate(media, fullUpdate);

    assertEquals("New Title", media.getMediaTitle());
    assertEquals("New Author", media.getAuthorName());
    assertEquals("0987654321", media.getIsbnId());
    assertEquals(500, media.getNumberPages());
    assertEquals(LocalDate.of(2022, 5, 10), media.getAcquisitionDate());
    assertNotNull(media.getLastUpdateDate());
  }
}
