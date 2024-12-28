package com.randomlake.library.service;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.FineType;
import com.randomlake.library.enums.MediaStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Fine;
import com.randomlake.library.model.Media;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.FineRepository;
import com.randomlake.library.repository.MediaRepository;
import com.randomlake.library.repository.PatronRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FineService {

  private final FineRepository fineRepository;
  private final PatronRepository patronRepository;
  private final MediaRepository mediaRepository;
  private final SequenceGenerator sequenceGenerator;

  private static final Logger log = LoggerFactory.getLogger(FineService.class);

  public FineService(
      FineRepository fineRepository,
      PatronRepository patronRepository,
      MediaRepository mediaRepository,
      SequenceGenerator sequenceGenerator) {
    this.fineRepository = fineRepository;
    this.patronRepository = patronRepository;
    this.mediaRepository = mediaRepository;
    this.sequenceGenerator = sequenceGenerator;
  }

  @Transactional
  public Fine assessFine(int patronId, int mediaId, FineType fineType, int amount) {
    // Validate input
    if (amount <= 0) {
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION,
          "Fine amount must be greater than zero",
          HttpStatus.BAD_REQUEST);
    }

    // Check if fine already exists for this patron and item
    if (hasFineBeenAssessed(patronId, mediaId, fineType)) {
      throw new GeneralException(
          ExceptionType.FINE_ALREADY_EXISTS,
          "Fine already exists for this patron for this media item and fine type",
          HttpStatus.CONFLICT);
    }

    // Validate patron and media existence
    validatePatronExists(patronId);
    Media media = validateMediaExists(mediaId);

    // Create fine
    Fine fine = createFine(patronId, mediaId, fineType, amount);

    // Update media status if necessary
    if (fineType == FineType.LOST_ITEM || fineType == FineType.DAMAGED_ITEM) {
      updateMediaStatus(media, MediaStatus.LOST_OR_DAMAGED);
    }

    return fine;
  }

  public double calculateTotalFines(int patronId) {
    List<Fine> fines = fineRepository.findActiveFinesByPatronId(patronId);
    return fines.stream().mapToDouble(Fine::getAmount).sum();
  }

  @Transactional
  public Fine updateFine(
      int fineId, int amount, Boolean isWaived, Boolean isPaid, FineType fineType) {
    Fine fine = getFineById(fineId);

    // Update fields if they are provided
    if (amount > 0) {
      fine.setAmount(amount);
    } else {
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION,
          "Fine amount must be greater than zero",
          HttpStatus.BAD_REQUEST);
    }

    Optional.ofNullable(fineType).ifPresent(fine::setFineType);
    Optional.ofNullable(isPaid).ifPresent(fine::setPaid);
    Optional.ofNullable(isWaived).ifPresent(fine::setWaived);

    return fineRepository.save(fine);
  }

  @Transactional
  public Fine createFine(int patronId, int mediaId, FineType fineType, int amount) {
    Fine fine = new Fine();
    fine.setFineId(sequenceGenerator.getNextSequenceValueForFine());
    fine.setPatronId(patronId);
    fine.setMediaId(mediaId);
    fine.setFineType(fineType);
    fine.setAmount(amount);
    fine.setDateAssessed(LocalDateTime.now());

    return fineRepository.save(fine);
  }

  @Transactional
  protected void updateMediaStatus(Media media, MediaStatus status) {
    media.setStatus(status);
    mediaRepository.save(media);
  }

  public List<Fine> getFinesByPatronId(int patronId) {
    return fineRepository.findByPatronId(patronId);
  }

  public Fine getFineById(int fineId) {
    return fineRepository
        .findByFineId(fineId)
        .orElseThrow(
            () ->
                new GeneralException(
                    ExceptionType.FINE_NOT_FOUND, "Fine not found", HttpStatus.NOT_FOUND));
  }

  // Helper methods
  private void validatePatronExists(int patronId) {
    Optional<Patron> patron = patronRepository.findByPatronId(patronId);
    if (!patron.isPresent()) {
      throw new GeneralException(
          ExceptionType.PATRON_NOT_FOUND, "Patron not found", HttpStatus.NOT_FOUND);
    }
  }

  private Media validateMediaExists(int mediaId) {
    return mediaRepository
        .findByMediaId(mediaId)
        .orElseThrow(
            () ->
                new GeneralException(
                    ExceptionType.MEDIA_NOT_FOUND, "Media not found", HttpStatus.NOT_FOUND));
  }

  public boolean hasFineBeenAssessed(int patronId, int mediaId, FineType fineType) {
    validateMediaExists(mediaId);
    validatePatronExists(patronId);
    return fineRepository.existsByPatronIdAndMediaIdAndFineType(patronId, mediaId, fineType);
  }
}
