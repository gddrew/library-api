package com.randomlake.library.service;

import com.randomlake.library.dto.LoanActionRequest;
import com.randomlake.library.dto.LoanResponse;
import com.randomlake.library.dto.ReportLoanPatronMedia;
import com.randomlake.library.dto.TransactionResponse;
import com.randomlake.library.enums.*;
import com.randomlake.library.enums.TransactionType;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.mapper.LoanMapper;
import com.randomlake.library.model.Loan;
import com.randomlake.library.model.Media;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.LoanRepository;
import com.randomlake.library.util.OutputFormatter;
import com.randomlake.library.util.SequenceGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {

  private final MediaService mediaService;
  private final PatronService patronService;
  private final LoanRepository loanRepository;
  private final SequenceGenerator sequenceGenerator;
  private final OutputFormatter outputFormatter;
  private final ValidationService validationService;

  private static final Logger log = LoggerFactory.getLogger(LoanService.class);

  @Value("${loan.period.days}")
  private int loanPeriodDays;

  public LoanService(
      MediaService mediaService,
      PatronService patronService,
      LoanRepository loanRepository,
      SequenceGenerator sequenceGenerator,
      OutputFormatter outputFormatter,
      ValidationService validationService) {
    this.mediaService = mediaService;
    this.patronService = patronService;
    this.loanRepository = loanRepository;
    this.sequenceGenerator = sequenceGenerator;
    this.outputFormatter = outputFormatter;
    this.validationService = validationService;
  }

  public TransactionResponse processLoanAction(LoanActionRequest loanActionRequest) {
    TransactionType transactionType = loanActionRequest.getTransactionType();
    if (transactionType == null) {
      log.error("Invalid transation type: null");
      throw new GeneralException(
          ExceptionType.INVALID_INPUT, "Invalid transaction type: null", HttpStatus.BAD_REQUEST);
    }
    return switch (transactionType) {
      case CHECKOUT -> checkoutItems(loanActionRequest);
      case RETURN -> returnItems(loanActionRequest);
      default -> {
        log.error("Invalid transaction type: {}", transactionType);
        throw new GeneralException(
            ExceptionType.INVALID_INPUT,
            "Invalid transaction type: " + transactionType,
            HttpStatus.BAD_REQUEST);
      }
    };
  }

  @Transactional
  public TransactionResponse checkoutItems(LoanActionRequest loanActionRequest) {
    int patronId = loanActionRequest.getPatronId();
    List<Integer> mediaIds = loanActionRequest.getMediaIds();

    if (mediaIds == null || mediaIds.isEmpty()) {
      throw new GeneralException(
          ExceptionType.INVALID_INPUT,
          "No media IDs provided for checkout",
          HttpStatus.BAD_REQUEST);
    }
    log.info("Attempting to checkout mediaIds {} for patronId {}", mediaIds, patronId);

    Patron patron = patronService.getPatronById(patronId);
    Loan loan = findOrCreateActiveLoanForPatron(patronId);
    List<Media> checkedOutMedia = new ArrayList<>();

    for (int mediaId : mediaIds) {
      Media media = mediaService.getMediaById(mediaId);

      validationService.validateMediaForStatus(media, MediaStatus.AVAILABLE);
      validationService.validatePatronForCheckout(patron, media);

      LocalDate dueDate = LocalDate.now().plusDays(loanPeriodDays);

      mediaService.updateMediaStatus(media, MediaStatus.CHECKED_OUT);
      patronService.updatePatronAfterCheckout(patron, mediaId);

      addLoanItemToLoan(loan, mediaId, dueDate);
      checkedOutMedia.add(media);
    }

    loanRepository.save(loan);
    log.info("Checkout successful for mediaIds {} to patronId {}", mediaIds, patronId);

    return createTransactionResponse(checkedOutMedia, loan.getLoanId(), true);
  }

  @Transactional
  public TransactionResponse returnItems(LoanActionRequest loanActionRequest) {
    int patronId = loanActionRequest.getPatronId();
    List<Integer> mediaIds = loanActionRequest.getMediaIds();

    if (mediaIds == null || mediaIds.isEmpty()) {
      throw new GeneralException(
          ExceptionType.INVALID_INPUT, "No media IDs provided for return", HttpStatus.BAD_REQUEST);
    }
    log.info("Attempting to return mediaIds {} from patronId {}", mediaIds, patronId);

    Patron patron = patronService.getPatronById(patronId);
    List<Media> returnedMedia = new ArrayList<>();

    for (int mediaId : mediaIds) {
      Media media = mediaService.getMediaById(mediaId);

      validationService.validateMediaForStatus(media, MediaStatus.CHECKED_OUT);

      Loan loan = findActiveLoanWithMediaItem(patronId, mediaId);
      updateLoanWithReturn(loan, mediaId);
      loanRepository.save(loan);

      mediaService.updateMediaStatus(media, MediaStatus.AVAILABLE);
      patronService.updatePatronAfterReturn(patron, mediaId);

      returnedMedia.add(media);
    }

    log.info("Return successful for mediaIds {} from patronId {}", mediaIds, patronId);

    return createTransactionResponse(returnedMedia, patronId, false);
  }

  // Helper methods
  private Loan findOrCreateActiveLoanForPatron(int patronId) {
    return loanRepository
        .findFirstByPatronIdAndStatus(patronId, LoanStatus.ACTIVE)
        .orElseGet(() -> createNewLoan(patronId));
  }

  private Loan createNewLoan(int patronId) {
    Loan loan = new Loan();
    loan.setLoanId(sequenceGenerator.getNextSequenceValueForLoan());
    loan.setPatronId(patronId);
    loan.setStatus(LoanStatus.ACTIVE);
    loan.setItems(new ArrayList<>());
    loan.setTransactionLog(new ArrayList<>());
    return loan;
  }

  private void addLoanItemToLoan(Loan loan, int mediaId, LocalDate dueDate) {
    Loan.LoanItem loanItem = new Loan.LoanItem();
    loanItem.setMediaId(mediaId);
    loanItem.setCheckoutDate(LocalDate.now());
    loanItem.setDueDate(dueDate);
    loanItem.setStatus(ItemStatus.CHECKED_OUT);
    loan.getItems().add(loanItem);
    loan.getTransactionLog()
        .add(createTransactionLogEntry(TransactionType.CHECKOUT, List.of(mediaId)));
    log.info("Added loan item for mediaId {} to loanNumber {}", mediaId, loan.getLoanId());
  }

  private Loan findActiveLoanWithMediaItem(int patronId, int mediaId) {
    return loanRepository
        .findActiveByPatronIdAndMediaId(
            patronId, LoanStatus.ACTIVE, mediaId, ItemStatus.CHECKED_OUT)
        .orElseThrow(
            () -> {
              log.error("No active loan found for patronId {} with mediaId {}", patronId, mediaId);
              return new GeneralException(
                  ExceptionType.INVALID_LOAN,
                  "No active loan found with the given media item",
                  HttpStatus.NOT_FOUND);
            });
  }

  private void updateLoanWithReturn(Loan loan, int mediaId) {
    loan.getItems().stream()
        .filter(item -> item.getMediaId() == mediaId && item.getStatus() == ItemStatus.CHECKED_OUT)
        .findFirst()
        .ifPresentOrElse(
            item -> {
              item.setReturnDate(LocalDate.now());
              item.setStatus(ItemStatus.RETURNED);
            },
            () -> {
              log.error("Loan item not found or already returned for mediaId {}", mediaId);
              throw new GeneralException(
                  ExceptionType.INVALID_LOAN,
                  "Loan item not found or already returned",
                  HttpStatus.NOT_FOUND);
            });
    loan.getTransactionLog()
        .add(createTransactionLogEntry(TransactionType.RETURN, List.of(mediaId)));
    if (loan.getItems().stream().allMatch(item -> item.getStatus() == ItemStatus.RETURNED)) {
      loan.setStatus(LoanStatus.COMPLETED);
    }
    log.info("Updated loanID {} after return of mediaId {}", loan.getLoanId(), mediaId);
  }

  private TransactionResponse createTransactionResponse(
      List<Media> mediaList, int loanId, boolean isCheckout) {
    TransactionResponse response = new TransactionResponse();
    response.setLoanId(loanId);
    response.setMessage(
        isCheckout ? "Item(s) checked out successfully" : "Item(s) returned successfully");
    response.setMediaItems(new ArrayList<>());

    for (Media media : mediaList) {
      TransactionResponse.MediaItem mediaItem = new TransactionResponse.MediaItem();
      mediaItem.setMediaTitle(media.getMediaTitle());
      mediaItem.setMediaStatus(media.getStatus().name());
      mediaItem.setFormattedBarcodeId(outputFormatter.formatBarcodeId(media.getBarCodeId()));
      response.getMediaItems().add(mediaItem);
    }

    return response;
  }

  private Loan.TransactionLog createTransactionLogEntry(
      TransactionType transactionType, List<Integer> mediaIds) {
    Loan.TransactionLog log = new Loan.TransactionLog();
    log.setTransactionType(transactionType);
    log.setTransactionDate(LocalDateTime.now());
    log.setMediaIds(mediaIds);
    return log;
  }

  public List<LoanResponse> findLoansByPatronId(int patronId) {
    List<Loan> loans = loanRepository.findByPatronId(patronId);
    return loans.stream().map(LoanMapper::toLoanResponse).collect(Collectors.toList());
  }

  public List<LoanResponse> findLoansByMediaId(int mediaId) {
    List<Loan> loans = loanRepository.findByMediaId(mediaId);
    return loans.stream().map(LoanMapper::toLoanResponse).collect(Collectors.toList());
  }

  public void deleteLoanById(int loanId) {
    boolean exists = loanRepository.existsByLoanId(loanId);
    if (!exists) {
      log.error("Loan not found with ID: {}", loanId);
      throw new GeneralException(
          ExceptionType.INVALID_INPUT, "Loan not found with ID: " + loanId, HttpStatus.NOT_FOUND);
    }

    Loan loan =
        loanRepository
            .findByLoanId(loanId)
            .orElseThrow(
                () ->
                    new GeneralException(
                        ExceptionType.INVALID_INPUT,
                        "Loan not found with ID: " + loanId,
                        HttpStatus.NOT_FOUND));

    boolean hasActiveItems =
        loan.getItems().stream().anyMatch(item -> item.getStatus() == ItemStatus.CHECKED_OUT);
    if (hasActiveItems) {
      log.error("loan with ID {} has active items", loanId);
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION,
          "Cannot delete loan with active items",
          HttpStatus.FORBIDDEN);
    }

    loanRepository.deleteByLoanId(loanId);
    log.info("Deleted loan with ID: {}", loanId);
  }

  public List<ReportLoanPatronMedia> getReportLoanPatronMedia(Optional<Integer> patronId) {
    if (patronId.isEmpty()) {
      throw new IllegalArgumentException("Patron ID is required");
    }
    return loanRepository.getReportLoanPatronMedia(patronId);
  }
}
