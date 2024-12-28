package com.randomlake.library.service;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.MediaStatus;
import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Media;
import com.randomlake.library.model.Patron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

  private final PatronService patronService;
  private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

  @Autowired
  public ValidationService(PatronService patronService) {
    this.patronService = patronService;
  }

  public void validateMediaForStatus(Media media, MediaStatus expectedStatus) {
    if (media.getStatus() != expectedStatus) {
      log.error(
          "Media item {} has status {}, expected {}",
          media.getMediaId(),
          media.getStatus(),
          expectedStatus);
      throw new GeneralException(
          ExceptionType.MEDIA_NOT_AVAILABLE,
          "Invalid media status for action",
          HttpStatus.FORBIDDEN);
    }
  }

  public void validatePatronForCheckout(Patron patron, Media media) {
    if (patron.getStatus().equals(PatronStatus.SUSPENDED)) {
      log.error("Patron {} is suspended and cannot checkout items", patron.getPatronId());
      throw new GeneralException(
          ExceptionType.PATRON_INELIGIBLE,
          "Patron is suspended and not eligible for checkout",
          HttpStatus.FORBIDDEN);
    }
    if (patronService.isMinor(patron.getDateOfBirth()) && media.isSensitive()) {
      log.error(
          "Minor patron {} cannot checkout sensitive media {}",
          patron.getPatronId(),
          media.getMediaId());
      throw new GeneralException(
          ExceptionType.PATRON_INELIGIBLE,
          "Minor patrons cannot checkout sensitive media",
          HttpStatus.FORBIDDEN);
    }
  }
}
