package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.randomlake.library.dto.LoanActionRequest;
import com.randomlake.library.dto.TransactionResponse;
import com.randomlake.library.enums.*;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Loan;
import com.randomlake.library.model.Media;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.LoanRepository;
import com.randomlake.library.util.OutputFormatter;
import com.randomlake.library.util.SequenceGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

  @Mock private MediaService mediaService;
  @Mock private PatronService patronService;
  @Mock private LoanRepository loanRepository;
  @Mock private SequenceGenerator sequenceGenerator;
  @Mock private OutputFormatter outputFormatter;
  @Mock private ValidationService validationService;

  private LoanService loanService;

  private Media media;
  private Patron patron;
  private Loan loan;
  private LocalDateTime now;

  @BeforeEach
  public void setUp() {
    // Initialize LoanService with constructor injection
    loanService =
        new LoanService(
            mediaService,
            patronService,
            loanRepository,
            sequenceGenerator,
            outputFormatter,
            validationService);

    // Setup common test data
    now = LocalDateTime.of(2024, 10, 1, 12, 0);

    media = new Media();
    media.setStatus(MediaStatus.AVAILABLE);
    media.setBarCodeId("39900100000022");
    media.setMediaId(2);
    media.setMediaTitle("Sample Book");
    media.setLastUpdateDate(now);
    media.setSensitive(false);

    patron = new Patron();
    patron.setPatronId(1);
    patron.setStatus(PatronStatus.ACTIVE);
    patron.setDateOfBirth(LocalDate.of(1999, 1, 1));
    patron.setCheckedOutItems(new ArrayList<>());
    patron.setLastUpdateDate(now);

    Loan.LoanItem loanItem = new Loan.LoanItem();
    loanItem.setMediaId(2);
    loanItem.setStatus(ItemStatus.CHECKED_OUT);

    loan = new Loan();
    loan.setLoanId(1001);
    loan.setPatronId(patron.getPatronId());
    loan.setStatus(LoanStatus.ACTIVE);
    loan.setItems(new ArrayList<>(List.of(loanItem)));
    loan.setTransactionLog(new ArrayList<>());
  }

  @Test
  public void testCheckoutItems_Success() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    when(mediaService.getMediaById(2)).thenReturn(media);
    when(patronService.getPatronById(1)).thenReturn(patron);
    when(loanRepository.findFirstByPatronIdAndStatus(1, LoanStatus.ACTIVE))
        .thenReturn(Optional.empty());
    when(sequenceGenerator.getNextSequenceValueForLoan()).thenReturn(1001);
    when(outputFormatter.formatBarcodeId("39900100000022")).thenReturn("3-9900-10000002-2");

    doNothing().when(validationService).validateMediaForStatus(media, MediaStatus.AVAILABLE);
    doNothing().when(validationService).validatePatronForCheckout(patron, media);

    // Simlulate media status update
    doAnswer(
            invocation -> {
              Media mediaToUpdate = invocation.getArgument(0);
              MediaStatus newStatus = invocation.getArgument(1);
              mediaToUpdate.setStatus(newStatus);
              return null;
            })
        .when(mediaService)
        .updateMediaStatus(any(Media.class), eq(MediaStatus.CHECKED_OUT));

    TransactionResponse response = loanService.processLoanAction(loanActionRequest);

    assertNotNull(response);
    assertEquals("Item(s) checked out successfully", response.getMessage());
    assertEquals(1001, response.getLoanId());
    assertNotNull(response.getMediaItems());
    assertEquals(1, response.getMediaItems().size());

    TransactionResponse.MediaItem mediaItem = response.getMediaItems().get(0);
    assertEquals("Sample Book", mediaItem.getMediaTitle());
    assertEquals("CHECKED_OUT", mediaItem.getMediaStatus());
    assertEquals("3-9900-10000002-2", mediaItem.getFormattedBarcodeId());

    verify(validationService, times(1)).validateMediaForStatus(media, MediaStatus.AVAILABLE);
    verify(validationService, times(1)).validatePatronForCheckout(patron, media);
    verify(mediaService, times(1)).updateMediaStatus(media, MediaStatus.CHECKED_OUT);
    verify(patronService, times(1)).updatePatronAfterCheckout(patron, 2);
    verify(loanRepository, times(1)).save(any(Loan.class));
  }

  @Test
  public void testCheckoutItems_MediaNotAvailable() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    media.setStatus(MediaStatus.CHECKED_OUT);
    when(mediaService.getMediaById(2)).thenReturn(media);
    when(patronService.getPatronById(1)).thenReturn(patron);

    doThrow(
            new GeneralException(
                ExceptionType.MEDIA_NOT_AVAILABLE,
                "Invalid media status for action",
                HttpStatus.FORBIDDEN))
        .when(validationService)
        .validateMediaForStatus(media, MediaStatus.AVAILABLE);

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> loanService.processLoanAction(loanActionRequest));
    assertEquals(ExceptionType.MEDIA_NOT_AVAILABLE, exception.getType());
    assertEquals("Invalid media status for action", exception.getMessage());

    verify(mediaService, never()).updateMediaStatus(any(), any());
    verify(patronService, never()).updatePatronAfterCheckout(any(), anyInt());
    verify(loanRepository, never()).save(any(Loan.class));
  }

  @Test
  public void testCheckoutItems_PatronNotFound() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    when(patronService.getPatronById(1))
        .thenThrow(
            new GeneralException(
                ExceptionType.PATRON_NOT_FOUND, "Patron not found", HttpStatus.NOT_FOUND));

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> loanService.processLoanAction(loanActionRequest));
    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals("Patron not found", exception.getMessage());

    verify(mediaService, never()).getMediaById(anyInt());
    verify(loanRepository, never()).save(any(Loan.class));
    verify(validationService, never()).validateMediaForStatus(any(), any());
    verify(validationService, never()).validatePatronForCheckout(any(), any());
  }

  @Test
  public void testCheckoutItems_PatronSuspended() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    patron.setStatus(PatronStatus.SUSPENDED);
    when(mediaService.getMediaById(2)).thenReturn(media);
    when(patronService.getPatronById(1)).thenReturn(patron);

    doThrow(
            new GeneralException(
                ExceptionType.PATRON_INELIGIBLE,
                "Patron is suspended and not eligible for checkout",
                HttpStatus.FORBIDDEN))
        .when(validationService)
        .validatePatronForCheckout(patron, media);

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> loanService.processLoanAction(loanActionRequest));
    assertEquals(ExceptionType.PATRON_INELIGIBLE, exception.getType());
    assertEquals("Patron is suspended and not eligible for checkout", exception.getMessage());

    verify(mediaService, never()).updateMediaStatus(any(), any());
    verify(patronService, never()).updatePatronAfterCheckout(any(), anyInt());
    verify(loanRepository, never()).save(any(Loan.class));
  }

  @Test
  public void testCheckoutItems_MinorCheckingOutSensitiveMedia() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    media.setSensitive(true);
    patron.setDateOfBirth(LocalDate.now().minusYears(15));
    when(mediaService.getMediaById(2)).thenReturn(media);
    when(patronService.getPatronById(1)).thenReturn(patron);

    doThrow(
            new GeneralException(
                ExceptionType.PATRON_INELIGIBLE,
                "Minor patrons cannot checkout sensitive media",
                HttpStatus.FORBIDDEN))
        .when(validationService)
        .validatePatronForCheckout(patron, media);

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> loanService.processLoanAction(loanActionRequest));
    assertEquals(ExceptionType.PATRON_INELIGIBLE, exception.getType());
    assertEquals("Minor patrons cannot checkout sensitive media", exception.getMessage());

    verify(mediaService, never()).updateMediaStatus(any(), any());
    verify(patronService, never()).updatePatronAfterCheckout(any(), anyInt());
    verify(loanRepository, never()).save(any(Loan.class));
  }

  @Test
  public void testCheckoutItems_MediaNotFound() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    when(patronService.getPatronById(1)).thenReturn(patron);
    when(mediaService.getMediaById(2))
        .thenThrow(
            new GeneralException(
                ExceptionType.MEDIA_NOT_FOUND, "Media not found", HttpStatus.NOT_FOUND));

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> loanService.processLoanAction(loanActionRequest));
    assertEquals("Media not found", exception.getMessage());
    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());

    verify(mediaService, never()).updateMediaStatus(any(), any());
    verify(patronService, never()).updatePatronAfterCheckout(any(), anyInt());
    verify(loanRepository, never()).save(any(Loan.class));
    verify(validationService, never()).validateMediaForStatus(any(), any());
  }

  @Test
  public void testCheckoutItems_MediaAlreadyCheckedOutBySamePatron() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    media.setStatus(MediaStatus.CHECKED_OUT);
    when(mediaService.getMediaById(2)).thenReturn(media);
    when(patronService.getPatronById(1)).thenReturn(patron);

    // Assuming that the patron has already checked out the media
    patron.getCheckedOutItems().add(2);

    doThrow(
            new GeneralException(
                ExceptionType.MEDIA_NOT_AVAILABLE,
                "Item is not available for checkout",
                HttpStatus.FORBIDDEN))
        .when(validationService)
        .validateMediaForStatus(media, MediaStatus.AVAILABLE);

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> loanService.processLoanAction(loanActionRequest));
    assertEquals("Item is not available for checkout", exception.getMessage());
    assertEquals(ExceptionType.MEDIA_NOT_AVAILABLE, exception.getType());

    verify(mediaService, never()).updateMediaStatus(any(), any());
    verify(patronService, never()).updatePatronAfterCheckout(any(), anyInt());
    verify(loanRepository, never()).save(any(Loan.class));
  }

  @Test
  public void testCheckoutItems_CreatesNewLoanIfNoneExists() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    when(mediaService.getMediaById(2)).thenReturn(media);
    when(patronService.getPatronById(1)).thenReturn(patron);
    when(loanRepository.findFirstByPatronIdAndStatus(1, LoanStatus.ACTIVE))
        .thenReturn(Optional.empty());
    when(sequenceGenerator.getNextSequenceValueForLoan()).thenReturn(1001);
    when(outputFormatter.formatBarcodeId(anyString())).thenReturn("3-9900-10000002-2");

    doNothing().when(validationService).validateMediaForStatus(media, MediaStatus.AVAILABLE);
    doNothing().when(validationService).validatePatronForCheckout(patron, media);

    doAnswer(
            invocation -> {
              Media mediaToUpdate = invocation.getArgument(0);
              MediaStatus newStatus = invocation.getArgument(1);
              mediaToUpdate.setStatus(newStatus);
              return null;
            })
        .when(mediaService)
        .updateMediaStatus(any(Media.class), eq(MediaStatus.CHECKED_OUT));

    TransactionResponse response = loanService.processLoanAction(loanActionRequest);

    assertNotNull(response);
    assertEquals(1001, response.getLoanId());
    verify(loanRepository, times(1)).save(any(Loan.class));
  }

  @Test
  public void testCheckoutItems_UsesExistingActiveLoan() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    when(mediaService.getMediaById(2)).thenReturn(media);
    when(patronService.getPatronById(1)).thenReturn(patron);
    when(loanRepository.findFirstByPatronIdAndStatus(1, LoanStatus.ACTIVE))
        .thenReturn(Optional.of(loan));
    when(outputFormatter.formatBarcodeId(anyString())).thenReturn("3-9900-10000002-2");

    doNothing().when(validationService).validateMediaForStatus(media, MediaStatus.AVAILABLE);
    doNothing().when(validationService).validatePatronForCheckout(patron, media);

    doAnswer(
            invocation -> {
              Media mediaToUpdate = invocation.getArgument(0);
              MediaStatus newStatus = invocation.getArgument(1);
              mediaToUpdate.setStatus(newStatus);
              return null;
            })
        .when(mediaService)
        .updateMediaStatus(any(Media.class), eq(MediaStatus.CHECKED_OUT));

    TransactionResponse response = loanService.processLoanAction(loanActionRequest);

    assertNotNull(response);
    assertEquals(1001, response.getLoanId());
    verify(loanRepository, times(1)).save(any(Loan.class));
    // Ensure that a new loan was not created
    verify(sequenceGenerator, times(0)).getNextSequenceValueForLoan();
  }

  @Test
  public void testCheckoutItems_MultipleItemsSuccess() {
    // Prepare LoanActionRequest
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2, 3));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    Media media1 = new Media();
    media1.setStatus(MediaStatus.AVAILABLE);
    media1.setBarCodeId("39900100000023");
    media1.setMediaId(3);
    media1.setMediaTitle("Another Sample Book");
    media1.setLastUpdateDate(now);

    when(mediaService.getMediaById(2)).thenReturn(media);
    when(mediaService.getMediaById(3)).thenReturn(media1);
    when(patronService.getPatronById(1)).thenReturn(patron);
    when(loanRepository.findFirstByPatronIdAndStatus(1, LoanStatus.ACTIVE))
        .thenReturn(Optional.empty());
    when(sequenceGenerator.getNextSequenceValueForLoan()).thenReturn(1001);
    when(outputFormatter.formatBarcodeId("39900100000022")).thenReturn("3-9900-10000002-2");
    when(outputFormatter.formatBarcodeId("39900100000023")).thenReturn("3-9900-10000003-3");

    // Mock validation service
    doNothing()
        .when(validationService)
        .validateMediaForStatus(any(Media.class), eq(MediaStatus.AVAILABLE));
    doNothing()
        .when(validationService)
        .validatePatronForCheckout(any(Patron.class), any(Media.class));

    // Simulate media status update
    doAnswer(
            invocation -> {
              Media mediaToUpdate = invocation.getArgument(0);
              MediaStatus newStatus = invocation.getArgument(1);
              mediaToUpdate.setStatus(newStatus);
              return null;
            })
        .when(mediaService)
        .updateMediaStatus(any(Media.class), any(MediaStatus.class));

    TransactionResponse response = loanService.processLoanAction(loanActionRequest);

    assertNotNull(response);
    assertEquals("Item(s) checked out successfully", response.getMessage());
    assertEquals(1001, response.getLoanId());
    assertNotNull(response.getMediaItems());
    assertEquals(2, response.getMediaItems().size());

    // Assert details for the first media item
    TransactionResponse.MediaItem mediaItem1 = response.getMediaItems().get(0);
    assertEquals("Sample Book", mediaItem1.getMediaTitle());
    assertEquals("CHECKED_OUT", mediaItem1.getMediaStatus());
    assertEquals("3-9900-10000002-2", mediaItem1.getFormattedBarcodeId());

    // Assert details for the second media item
    TransactionResponse.MediaItem mediaItem2 = response.getMediaItems().get(1);
    assertEquals("Another Sample Book", mediaItem2.getMediaTitle());
    assertEquals("CHECKED_OUT", mediaItem2.getMediaStatus());
    assertEquals("3-9900-10000003-3", mediaItem2.getFormattedBarcodeId());

    verify(mediaService, times(1)).updateMediaStatus(media, MediaStatus.CHECKED_OUT);
    verify(mediaService, times(1)).updateMediaStatus(media1, MediaStatus.CHECKED_OUT);
    verify(loanRepository, times(1)).save(any(Loan.class));
    verify(patronService, times(1)).updatePatronAfterCheckout(patron, 2);
    verify(patronService, times(1)).updatePatronAfterCheckout(patron, 3);
    verify(validationService, times(2))
        .validateMediaForStatus(any(Media.class), eq(MediaStatus.AVAILABLE));
    verify(validationService, times(2)).validatePatronForCheckout(eq(patron), any(Media.class));
  }

  @Test
  public void testUpdateInactivePatronToActiveAfterCheckout() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.CHECKOUT);

    patron.setStatus(PatronStatus.INACTIVE);

    when(patronService.getPatronById(1)).thenReturn(patron);
    when(mediaService.getMediaById(2)).thenReturn(media);
    when(loanRepository.findFirstByPatronIdAndStatus(1, LoanStatus.ACTIVE))
        .thenReturn(Optional.empty());
    when(sequenceGenerator.getNextSequenceValueForLoan()).thenReturn(1010);
    when(outputFormatter.formatBarcodeId(anyString())).thenReturn("3-9900-10000002-2");

    doNothing().when(validationService).validateMediaForStatus(media, MediaStatus.AVAILABLE);

    // Simluate media status update
    doAnswer(
            invocation -> {
              Media mediaToUpdate = invocation.getArgument(0);
              MediaStatus newStatus = invocation.getArgument(1);
              mediaToUpdate.setStatus(newStatus);
              return null;
            })
        .when(mediaService)
        .updateMediaStatus(any(Media.class), any(MediaStatus.class));

    // Simulate patron update after checkout
    doAnswer(
            invocation -> {
              Patron patronToUpdate = invocation.getArgument(0);
              int mediaId = invocation.getArgument(1);
              if (patronToUpdate.getStatus() == PatronStatus.INACTIVE) {
                patronToUpdate.setStatus(PatronStatus.ACTIVE);
              }
              patronToUpdate.getCheckedOutItems().add(mediaId);
              return null;
            })
        .when(patronService)
        .updatePatronAfterCheckout(any(Patron.class), anyInt());

    TransactionResponse response = loanService.processLoanAction(loanActionRequest);

    assertNotNull(response);
    assertEquals("Item(s) checked out successfully", response.getMessage());
    assertEquals(1010, response.getLoanId());
    assertNotNull(response.getMediaItems());
    assertEquals(1, response.getMediaItems().size());

    TransactionResponse.MediaItem mediaItem = response.getMediaItems().get(0);
    assertEquals("Sample Book", mediaItem.getMediaTitle());
    assertEquals("CHECKED_OUT", mediaItem.getMediaStatus());
    assertEquals("3-9900-10000002-2", mediaItem.getFormattedBarcodeId());
    assertEquals(PatronStatus.ACTIVE, patron.getStatus());
    assertTrue(patron.getCheckedOutItems().contains(2));

    verify(mediaService, times(1)).updateMediaStatus(media, MediaStatus.CHECKED_OUT);
    verify(loanRepository, times(1)).save(any(Loan.class));
    verify(patronService, times(1)).updatePatronAfterCheckout(patron, 2);
  }

  @Test
  public void testReturnItems_Success() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.RETURN);

    media.setStatus(MediaStatus.CHECKED_OUT);
    patron.getCheckedOutItems().add(2);

    when(mediaService.getMediaById(2)).thenReturn(media);
    when(patronService.getPatronById(1)).thenReturn(patron);
    when(loanRepository.findActiveByPatronIdAndMediaId(
            1, LoanStatus.ACTIVE, 2, ItemStatus.CHECKED_OUT))
        .thenReturn(Optional.of(loan));
    when(outputFormatter.formatBarcodeId(anyString())).thenReturn("3-9900-10000002-2");

    doNothing().when(validationService).validateMediaForStatus(media, MediaStatus.CHECKED_OUT);

    // Simluate media status update
    doAnswer(
            invocation -> {
              Media mediaToUpdate = invocation.getArgument(0);
              MediaStatus newStatus = invocation.getArgument(1);
              mediaToUpdate.setStatus(newStatus);
              return null;
            })
        .when(mediaService)
        .updateMediaStatus(any(Media.class), any(MediaStatus.class));

    TransactionResponse response = loanService.processLoanAction(loanActionRequest);

    assertNotNull(response);
    assertEquals("Item(s) returned successfully", response.getMessage());
    assertEquals(1, response.getLoanId());
    assertNotNull(response.getMediaItems());
    assertEquals(1, response.getMediaItems().size());

    TransactionResponse.MediaItem mediaItem = response.getMediaItems().get(0);
    assertEquals("Sample Book", mediaItem.getMediaTitle());
    assertEquals("AVAILABLE", mediaItem.getMediaStatus());
    assertEquals("3-9900-10000002-2", mediaItem.getFormattedBarcodeId());

    verify(validationService, times(1)).validateMediaForStatus(media, MediaStatus.CHECKED_OUT);
    verify(mediaService, times(1)).updateMediaStatus(media, MediaStatus.AVAILABLE);
    verify(patronService, times(1)).updatePatronAfterReturn(patron, 2);
    verify(loanRepository, times(1)).save(loan);
  }

  @Test
  public void testReturnItems_ItemNotCheckedOut() {
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(TransactionType.RETURN);

    media.setStatus(MediaStatus.AVAILABLE);
    when(mediaService.getMediaById(2)).thenReturn(media);
    when(patronService.getPatronById(1)).thenReturn(patron);

    doThrow(
            new GeneralException(
                ExceptionType.MEDIA_NOT_AVAILABLE,
                "Invalid media status for action",
                HttpStatus.FORBIDDEN))
        .when(validationService)
        .validateMediaForStatus(media, MediaStatus.CHECKED_OUT);

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> loanService.processLoanAction(loanActionRequest));
    assertEquals(ExceptionType.MEDIA_NOT_AVAILABLE, exception.getType());
    assertEquals("Invalid media status for action", exception.getMessage());

    verify(mediaService, never()).updateMediaStatus(any(), any());
    verify(patronService, never()).updatePatronAfterReturn(any(), anyInt());
    verify(loanRepository, never()).save(any(Loan.class));
  }

  @Test
  public void testProcessLoanAction_InvalidTransactionType() {
    // Prepare LoanActionRequest with invalid transaction type
    LoanActionRequest loanActionRequest = new LoanActionRequest();
    loanActionRequest.setPatronId(1);
    loanActionRequest.setMediaIds(List.of(2));
    loanActionRequest.setTransactionType(null); // Invalid transaction type

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> loanService.processLoanAction(loanActionRequest));
    assertEquals(ExceptionType.INVALID_INPUT, exception.getType());
    assertEquals("Invalid transaction type: null", exception.getMessage());

    verifyNoInteractions(mediaService);
    verifyNoInteractions(patronService);
    verifyNoInteractions(loanRepository);
    verifyNoInteractions(validationService);
  }

  @Test
  public void testDeleteLoanById_WithActiveItems_ShouldThrowException() {
    int loanId = 1001;

    // Prepare loan with active items
    Loan.LoanItem loanItem = new Loan.LoanItem();
    loanItem.setMediaId(2);
    loanItem.setStatus(ItemStatus.CHECKED_OUT);

    loan.setItems(List.of(loanItem));
    loan.setStatus(LoanStatus.ACTIVE);

    // Mock dependencies
    when(loanRepository.existsByLoanId(loanId)).thenReturn(true);
    when(loanRepository.findByLoanId(loanId)).thenReturn(Optional.of(loan));

    GeneralException exception =
        assertThrows(GeneralException.class, () -> loanService.deleteLoanById(loanId));
    assertEquals(ExceptionType.INVALID_OPERATION, exception.getType());
    assertEquals("Cannot delete loan with active items", exception.getMessage());

    verify(loanRepository, never()).deleteByLoanId(loanId);
  }

  @Test
  public void testDeleteLoanById_Success() {
    int loanId = 1001;

    // Prepare loan with all items returned
    Loan.LoanItem loanItem = new Loan.LoanItem();
    loanItem.setMediaId(2);
    loanItem.setStatus(ItemStatus.RETURNED);

    loan.setItems(List.of(loanItem));
    loan.setStatus(LoanStatus.COMPLETED);

    // Mock dependencies
    when(loanRepository.existsByLoanId(loanId)).thenReturn(true);
    when(loanRepository.findByLoanId(loanId)).thenReturn(Optional.of(loan));

    loanService.deleteLoanById(loanId);

    verify(loanRepository, times(1)).deleteByLoanId(loanId);
  }

  @Test
  public void testFindLoansByPatronId_Success() {
    int patronId = 1;

    when(loanRepository.findByPatronId(patronId)).thenReturn(List.of(loan));

    List<?> loans = loanService.findLoansByPatronId(patronId);

    assertNotNull(loans);
    assertEquals(1, loans.size());

    verify(loanRepository, times(1)).findByPatronId(patronId);
  }

  @Test
  public void testFindLoansByMediaId_Success() {
    int mediaId = 2;

    when(loanRepository.findByMediaId(mediaId)).thenReturn(List.of(loan));

    List<?> loans = loanService.findLoansByMediaId(mediaId);

    assertNotNull(loans);
    assertEquals(1, loans.size());

    verify(loanRepository, times(1)).findByMediaId(mediaId);
  }
}
