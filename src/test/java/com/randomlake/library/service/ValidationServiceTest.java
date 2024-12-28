package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.MediaStatus;
import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Media;
import com.randomlake.library.model.Patron;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

public class ValidationServiceTest {

  @Mock private PatronService patronService;

  @InjectMocks private ValidationService validationService;

  @Mock private Media media;

  @Mock private Patron patron;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testValidateMediaForStatus_invalidStatus_throwsGeneralException() {
    // Arrange
    MediaStatus expectedStatus = MediaStatus.AVAILABLE;
    when(media.getStatus()).thenReturn(MediaStatus.CHECKED_OUT);
    when(media.getMediaId()).thenReturn(123);

    // Act & Assert
    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              validationService.validateMediaForStatus(media, expectedStatus);
            });
    assertEquals(ExceptionType.MEDIA_NOT_AVAILABLE, exception.getType());
    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
  }

  @Test
  void testValidateMediaForStatus_validStatus_doesNotThrow() {
    // Arrange
    MediaStatus expectedStatus = MediaStatus.AVAILABLE;
    when(media.getStatus()).thenReturn(MediaStatus.AVAILABLE);

    // Act & Assert
    assertDoesNotThrow(() -> validationService.validateMediaForStatus(media, expectedStatus));
  }

  @Test
  void testValidatePatronForCheckout_patronSuspended_throwsGeneralException() {
    // Arrange
    when(patron.getStatus()).thenReturn(PatronStatus.SUSPENDED);
    when(patron.getPatronId()).thenReturn(301530);

    // Act & Assert
    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              validationService.validatePatronForCheckout(patron, media);
            });
    assertEquals(ExceptionType.PATRON_INELIGIBLE, exception.getType());
    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
  }

  @Test
  void testValidatePatronForCheckout_minorPatronAndSensitiveMedia_throwsGeneralException() {
    // Arrange
    when(patron.getStatus()).thenReturn(PatronStatus.ACTIVE);
    when(patron.getDateOfBirth()).thenReturn(LocalDate.of(2010, 1, 1)); // Minor
    when(patron.getPatronId()).thenReturn(301530);
    when(media.isSensitive()).thenReturn(true);
    when(media.getMediaId()).thenReturn(123);

    when(patronService.isMinor(any())).thenReturn(true);

    // Act & Assert
    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              validationService.validatePatronForCheckout(patron, media);
            });
    assertEquals(ExceptionType.PATRON_INELIGIBLE, exception.getType());
    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
  }

  @Test
  void testValidatePatronForCheckout_validPatronAndMedia_doesNotThrow() {
    // Arrange
    when(patron.getStatus()).thenReturn(PatronStatus.ACTIVE);
    when(patron.getDateOfBirth()).thenReturn(LocalDate.of(2000, 1, 1)); // Adult
    when(patron.getPatronId()).thenReturn(123);
    when(media.isSensitive()).thenReturn(false);

    when(patronService.isMinor(any())).thenReturn(false);

    // Act & Assert
    assertDoesNotThrow(() -> validationService.validatePatronForCheckout(patron, media));
  }
}
