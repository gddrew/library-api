package com.randomlake.library.service;

import com.randomlake.library.enums.BarcodeType;
import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.MediaStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Loan;
import com.randomlake.library.model.Media;
import com.randomlake.library.repository.LoanRepository;
import com.randomlake.library.repository.MediaRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediaService {

  private final LoanRepository loanRepository;
  private final MediaRepository mediaRepository;
  private final MediaUpdateService mediaUpdateService;
  private final SequenceGenerator sequenceGenerator;
  private static final Logger log = LoggerFactory.getLogger(MediaService.class);

  public MediaService(
      LoanRepository loanRepository,
      MediaRepository mediaRepository,
      MediaUpdateService mediaUpdateService,
      SequenceGenerator sequenceGenerator) {
    this.loanRepository = loanRepository;
    this.mediaRepository = mediaRepository;
    this.mediaUpdateService = mediaUpdateService;
    this.sequenceGenerator = sequenceGenerator;
  }

  public List<Media> getAllMedia() {
    return mediaRepository.findAll();
  }

  public Media getMediaById(int mediaId) {
    return mediaRepository
        .findByMediaId(mediaId)
        .orElseThrow(
            () -> {
              log.error("No media with ID {} found in collection", mediaId);
              return new GeneralException(
                  ExceptionType.MEDIA_NOT_FOUND,
                  "No item with this ID found in collection",
                  HttpStatus.NOT_FOUND);
            });
  }

  public List<Media> getMediaByMediaTitle(String mediaTitle) {
    return checkMediaNotEmpty(
        mediaRepository.findByMediaTitle(mediaTitle),
        "No item with this title found in collection");
  }

  public List<Media> getMediaByAuthorName(String authorName) {
    return checkMediaNotEmpty(
        mediaRepository.findByAuthorName(authorName), "No item by this author found in collection");
  }

  public List<Media> getMediaByPublisherName(String publisherName) {
    return checkMediaNotEmpty(
        mediaRepository.findByPublisherName(publisherName),
        "No item by this publisher found in collection");
  }

  public List<Media> getMediaByIsbnId(String isbnId) {
    return checkMediaNotEmpty(
        mediaRepository.findByIsbnId(isbnId), "No item by this ISBN ID found in collection");
  }

  /* This is the entry point for updating a media record. It retrieves the record from the repository, applies the updates
   * using the MediaUpdateService. It sets the lastUpdate date, saves it back to the database and returns the updated record
   */
  @Transactional
  public Media updateMediaPartial(int mediaId, Map<String, Object> updates) {
    if (updates == null || updates.isEmpty()) {
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION, "No updates provided", HttpStatus.BAD_REQUEST);
    }
    Media media = getMediaById(mediaId);
    mediaUpdateService.applyPartialUpdates(media, updates);
    media.setLastUpdateDate(LocalDateTime.now());
    return mediaRepository.save(media);
  }

  @Transactional
  public Media updateMediaFull(int mediaId, Media fullUpdate) {
    if (fullUpdate == null) {
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION, "No updates provided", HttpStatus.BAD_REQUEST);
    }
    Media media = getMediaById(mediaId);
    mediaUpdateService.applyFullUpdate(media, fullUpdate);
    media.setLastUpdateDate(LocalDateTime.now());
    return mediaRepository.save(media);
  }

  @Transactional
  public void deleteMedia(int mediaId) {
    Media media = getMediaById(mediaId);

    if (hasActiveLoans(mediaId)) {
      log.error("Attempt to delete media with active loans: ID {}", mediaId);
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION,
          "Cannot delete media with active loans or holds",
          HttpStatus.CONFLICT);
    }

    mediaRepository.delete(media);
    log.info("Media with ID {} deleted", mediaId);
  }

  private boolean hasActiveLoans(int mediaId) {
    List<Loan> activeLoans = loanRepository.findByMediaId(mediaId);
    return activeLoans.stream()
        .flatMap(loan -> loan.getItems().stream())
        .anyMatch(item -> item.getReturnDate() == null);
  }

  @Transactional
  public Media addNewMedia(Media media) {
    if (media == null) {
      throw new GeneralException(
          ExceptionType.INVALID_INPUT, "Media object cannot be null", HttpStatus.BAD_REQUEST);
    }
    validateMedia(media);

    int nextMediaNumber = sequenceGenerator.getNextSequenceValue(BarcodeType.MEDIA);
    media.setMediaId(nextMediaNumber);

    String barCode = sequenceGenerator.generateBarcode(BarcodeType.MEDIA, nextMediaNumber);
    media.setBarCodeId(barCode);

    LocalDateTime now = LocalDateTime.now();
    media.setCreated_date(now);
    media.setLastUpdateDate(now);
    media.setStatus(MediaStatus.AVAILABLE);

    if (media.getAcquisitionDate() == null) {
      media.setAcquisitionDate(LocalDate.now());
    }

    Media savedMedia = mediaRepository.save(media);

    return savedMedia;
  }

  private void validateMedia(Media media) {
    if (media.getMediaTitle() == null || media.getMediaTitle().isEmpty()) {
      throw new GeneralException(
          ExceptionType.INVALID_INPUT, "Media title cannot be empty", HttpStatus.BAD_REQUEST);
    }
  }

  private List<Media> checkMediaNotEmpty(List<Media> media, String errorMessage) {
    if (media.isEmpty()) {
      log.error(errorMessage);
      throw new GeneralException(ExceptionType.MEDIA_NOT_FOUND, errorMessage, HttpStatus.NOT_FOUND);
    }
    return media;
  }

  @Transactional
  public void updateMediaStatus(Media media, MediaStatus status) {
    media.setStatus(status);
    media.setLastUpdateDate(LocalDateTime.now());
    mediaRepository.save(media);
    log.info("Updated media status for mediaId {} to {}", media.getMediaId(), status);
  }
}
