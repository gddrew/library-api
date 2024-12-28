package com.randomlake.library.service;

import com.randomlake.library.enums.MediaStatus;
import com.randomlake.library.model.Media;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MediaUpdateService {

  private static final Logger log = LoggerFactory.getLogger(MediaUpdateService.class);

  public void applyPartialUpdates(Media media, Map<String, Object> updates) {

    updates.forEach(
        (key, value) -> {
          switch (key) {
            case "mediaTitle":
              media.setMediaTitle((String) value);
              break;
            case "authorName":
              media.setAuthorName((String) value);
              break;
            case "isbnId":
              media.setIsbnId((String) value);
              break;
            case "barCodeId":
              media.setBarCodeId((String) value);
              break;
            case "publicationYear":
              media.setPublicationYear((String) value);
              break;
            case "mediaType":
              media.setMediaType((String) value);
              break;
            case "mediaFormat":
              media.setMediaFormat((String) value);
              break;
            case "numberPages":
              if (value instanceof Integer) {
                media.setNumberPages((Integer) value);
              } else {
                throw new IllegalArgumentException(
                    "Invalid type for numberPages. Expected an Integer.");
              }
              break;
            case "classificationCategory":
              media.setClassificationCategory((String) value);
              break;
            case "classificationSubCategory":
              media.setClassificationSubCategory((String) value);
              break;
            case "publisherName":
              media.setPublisherName((String) value);
              break;
            case "disposalDisposition":
              media.setDisposalDisposition((String) value);
              break;
            case "acquisitionDate":
              if (value instanceof String) {
                media.setAcquisitionDate(LocalDate.parse((String) value));
              } else if (value instanceof LocalDate) {
                media.setAcquisitionDate((LocalDate) value);
              } else {
                throw new IllegalArgumentException(
                    "Invalid type for acquisitionDate. Expected a String or LocalDate.");
              }
              break;
            case "status":
              if (value instanceof String) {
                try {
                  media.setStatus(MediaStatus.valueOf((String) value));
                } catch (IllegalArgumentException e) {
                  throw new IllegalArgumentException("Invalid media status: " + value);
                }
              } else {
                throw new IllegalArgumentException("Invalid type for status. Expected a String.");
              }
              break;
            default:
              log.error("Attempted to update unknown property '{}' with value '{}'", key, value);
              throw new IllegalArgumentException("Unknown property: " + key);
          }
          media.setLastUpdateDate(LocalDateTime.now());
        });
  }

  public void applyFullUpdate(Media media, Media fullUpdate) {
    media.setMediaTitle(fullUpdate.getMediaTitle());
    media.setAuthorName(fullUpdate.getAuthorName());
    media.setIsbnId(fullUpdate.getIsbnId());
    media.setPublicationYear(fullUpdate.getPublicationYear());
    media.setMediaType(fullUpdate.getMediaType());
    media.setMediaFormat(fullUpdate.getMediaFormat());
    media.setNumberPages(fullUpdate.getNumberPages());
    media.setClassificationCategory(fullUpdate.getClassificationCategory());
    media.setClassificationSubCategory(fullUpdate.getClassificationSubCategory());
    media.setPublisherName(fullUpdate.getPublisherName());
    media.setDisposalDisposition(fullUpdate.getDisposalDisposition());
    media.setAcquisitionDate(fullUpdate.getAcquisitionDate());
    media.setLastUpdateDate(LocalDateTime.now());
  }
}
