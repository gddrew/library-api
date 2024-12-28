package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.FineType;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Fine;
import com.randomlake.library.model.Media;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.FineRepository;
import com.randomlake.library.repository.MediaRepository;
import com.randomlake.library.repository.PatronRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FineServiceTest {

  @Mock private FineRepository fineRepository;

  @Mock private PatronRepository patronRepository;

  @Mock private MediaRepository mediaRepository;

  @Mock private SequenceGenerator sequenceGenerator;

  @InjectMocks private FineService fineService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testAssessFine_Success() {
    int patronId = 1;
    int mediaId = 2;
    FineType fineType = FineType.LOST_ITEM;
    int amount = 100;

    Media media = new Media();
    media.setMediaId(mediaId);

    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.of(new Patron()));
    when(mediaRepository.findByMediaId(mediaId)).thenReturn(Optional.of(media));
    when(fineRepository.existsByPatronIdAndMediaIdAndFineType(patronId, mediaId, fineType))
        .thenReturn(false);
    when(sequenceGenerator.getNextSequenceValueForFine()).thenReturn(123);
    when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Fine fine = fineService.assessFine(patronId, mediaId, fineType, amount);

    assertNotNull(fine);
    assertEquals(patronId, fine.getPatronId());
    assertEquals(mediaId, fine.getMediaId());
    assertEquals(fineType, fine.getFineType());
    assertEquals(amount, fine.getAmount());

    verify(mediaRepository).save(media);
  }

  @Test
  public void testAssessFine_InvalidAmountNotWaived() {
    int patronId = 1;
    int mediaId = 2;
    FineType fineType = FineType.LOST_ITEM;
    int amount = 0; // Invalid amount

    Media media = new Media();
    media.setMediaId(mediaId);

    // Mock repository behavior to validate the flow up to the amount check
    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.of(new Patron()));
    when(mediaRepository.findByMediaId(mediaId)).thenReturn(Optional.of(media));
    when(fineRepository.existsByPatronIdAndMediaIdAndFineType(patronId, mediaId, fineType))
        .thenReturn(false);

    // Assert that the exception is thrown
    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              fineService.assessFine(patronId, mediaId, fineType, amount);
            });

    // Verify exception details
    assertEquals(ExceptionType.INVALID_OPERATION, exception.getType());
    assertEquals("Fine amount must be greater than zero", exception.getMessage());

    // Verify that save methods are never called
    verify(fineRepository, never()).save(any(Fine.class));
    verify(mediaRepository, never()).save(any(Media.class));
  }

  @Test
  void testAssessFine_FineAlreadyExists() {
    int patronId = 1;
    int mediaId = 2;
    FineType fineType = FineType.LOST_ITEM;
    int amount = 100;

    Media media = new Media();
    media.setMediaId(mediaId);
    when(mediaRepository.findByMediaId(mediaId)).thenReturn(Optional.of(media));

    Patron patron = new Patron();
    patron.setPatronId(patronId);
    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.of(patron));

    when(fineRepository.existsByPatronIdAndMediaIdAndFineType(patronId, mediaId, fineType))
        .thenReturn(true);

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              fineService.assessFine(patronId, mediaId, fineType, amount);
            });

    assertEquals(ExceptionType.FINE_ALREADY_EXISTS, exception.getType());
  }

  @Test
  public void testCalculateTotalFines() {
    int patronId = 1;
    Fine fine1 = new Fine();
    fine1.setAmount(100);
    Fine fine2 = new Fine();
    fine2.setAmount(50);

    when(fineRepository.findActiveFinesByPatronId(patronId)).thenReturn(List.of(fine1, fine2));

    double total = fineService.calculateTotalFines(patronId);

    assertEquals(150, total);
  }

  @Test
  public void testUpdateFine_Success() {
    int fineId = 1;
    int amount = 150;
    FineType fineType = FineType.OVERDUE_ITEM;
    Fine fine = new Fine();
    fine.setFineId(fineId);
    fine.setAmount(150);

    when(fineRepository.findByFineId(fineId)).thenReturn(Optional.of(fine));
    when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Fine updatedFine = fineService.updateFine(fineId, amount, false, true, fineType);

    assertNotNull(updatedFine);
    assertEquals(amount, updatedFine.getAmount());
    assertTrue(updatedFine.isPaid());
    assertEquals(fineType, updatedFine.getFineType());
  }

  @Test
  public void testUpdateFine_NotFound() {
    int fineId = 1;

    when(fineRepository.findByFineId(fineId)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              fineService.updateFine(fineId, 100, null, null, null);
            });

    assertEquals(ExceptionType.FINE_NOT_FOUND, exception.getType());
  }

  @Test
  public void testUpdateFine_InvalidAmount() {
    int fineId = 1;
    int amount = 0;
    Fine fine = new Fine();
    fine.setFineId(fineId);
    fine.setAmount(150);

    when(fineRepository.findByFineId(fineId)).thenReturn(Optional.of(fine));

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              fineService.updateFine(fineId, 0, false, true, null);
            });

    assertEquals(ExceptionType.INVALID_OPERATION, exception.getType());
    assertEquals("Fine amount must be greater than zero", exception.getMessage());
    verify(fineRepository, never()).save(any(Fine.class));
  }

  @Test
  public void testValidatePatronExists_PatronNotFound() {
    int patronId = 1;
    int mediaId = 2;

    Media media = new Media();
    media.setMediaId(mediaId);
    when(mediaRepository.findByMediaId(mediaId)).thenReturn(Optional.of(media));

    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              fineService.assessFine(patronId, 2, FineType.LOST_ITEM, 100);
            });

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
  }

  @Test
  public void testValidateMediaExists_MediaNotFound() {
    int patronId = 1;
    int mediaId = 2;

    Patron patron = new Patron();
    patron.setPatronId(patronId);
    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.of(patron));

    when(mediaRepository.findByMediaId(mediaId)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> {
              fineService.assessFine(1, mediaId, FineType.LOST_ITEM, 100);
            });

    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());
  }

  @Test
  public void testGetFinesByPatronId_Success() {
    int patronId = 1;

    // Mock data
    Fine fine1 = new Fine();
    fine1.setFineId(101);
    fine1.setPatronId(patronId);
    fine1.setAmount(50);

    Fine fine2 = new Fine();
    fine2.setFineId(102);
    fine2.setPatronId(patronId);
    fine2.setAmount(100);

    List<Fine> mockFines = List.of(fine1, fine2);

    // Mock repository behavior
    when(fineRepository.findByPatronId(patronId)).thenReturn(mockFines);

    // Call the method
    List<Fine> fines = fineService.getFinesByPatronId(patronId);

    // Verify results
    assertNotNull(fines);
    assertEquals(2, fines.size());
    assertEquals(patronId, fines.get(0).getPatronId());
    assertEquals(50, fines.get(0).getAmount());
    assertEquals(100, fines.get(1).getAmount());

    // Verify interaction with repository
    verify(fineRepository, times(1)).findByPatronId(patronId);
  }
}
