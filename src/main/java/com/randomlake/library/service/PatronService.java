package com.randomlake.library.service;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.LoanStatus;
import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.LoanRepository;
import com.randomlake.library.repository.PatronRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatronService {

  private final PatronRepository patronRepository;
  private final PatronUpdateService patronUpdateService;
  private final SequenceGenerator sequenceGenerator;
  private final NotificationService notificationService;
  private final LoanRepository loanRepository;
  private static final Logger log = LoggerFactory.getLogger(PatronService.class);

  public PatronService(
      PatronRepository patronRepository,
      PatronUpdateService patronUpdateService,
      SequenceGenerator sequenceGenerator,
      NotificationService notificationService,
      LoanRepository loanRepository) {
    this.patronRepository = patronRepository;
    this.patronUpdateService = patronUpdateService;
    this.sequenceGenerator = sequenceGenerator;
    this.notificationService = notificationService;
    this.loanRepository = loanRepository;
  }

  public List<Patron> getAllPatrons() {
    List<Patron> patrons = patronRepository.findAll();
    if (patrons.isEmpty()) {
      log.error("No patrons found");
      throw new GeneralException(
          ExceptionType.PATRON_NOT_FOUND, "No patrons found", HttpStatus.NOT_FOUND);
    }
    return patrons;
  }

  public Patron getPatronById(int patronId) {
    return patronRepository
        .findByPatronId(patronId)
        .orElseThrow(
            () -> {
              String message = "Patron not found with ID: " + patronId;
              log.error(message);
              return new GeneralException(
                  ExceptionType.PATRON_NOT_FOUND, message, HttpStatus.NOT_FOUND);
            });
  }

  public List<Patron> getPatronByName(String patronName) {
    return handleNotFound(
        patronRepository.findByPatronName(patronName),
        "No patrons with name provided found: " + patronName);
  }

  public List<Patron> getPatronByDateOfBirth(LocalDate dateOfBirth) {
    return handleNotFound(
        patronRepository.findByDateOfBirth(dateOfBirth),
        "No patrons with date of birth provided found: " + dateOfBirth);
  }

  public List<Patron> getPatronByTelephone(String telephone) {
    return handleNotFound(
        patronRepository.findByTelephone(telephone),
        "No patrons with telephone number provided found: " + telephone);
  }

  public List<Patron> getPatronByEmail(String emailAddress) {
    return handleNotFound(
        patronRepository.findByEmailAddress(emailAddress),
        "No patrons with email address provided found: " + emailAddress);
  }

  /* This is the entry point for updating a patron record. It retrieves the record from the repository, applies the updates
   * using the PatronUpdateService. It sets the lastUpdate date, saves it back to the database and returns the updated record
   */
  @Transactional
  public Patron updatePatron(int patronId, Map<String, Object> updates, Patron fullUpdate) {
    return patronRepository
        .findByPatronId(patronId)
        .map(
            patron -> {
              if (updates != null) {
                patronUpdateService.applyPartialUpdates(patron, updates);
              } else if (fullUpdate != null) {
                patronUpdateService.applyFullUpdate(patron, fullUpdate);
              }
              patron.setLastUpdateDate(LocalDateTime.now());
              return patronRepository.save(patron);
            })
        .orElseThrow(
            () -> {
              log.error("Patron with ID provided not found: {}", patronId);
              return new GeneralException(
                  ExceptionType.PATRON_NOT_FOUND,
                  "Patron with ID provided not found: " + patronId,
                  HttpStatus.NOT_FOUND);
            });
  }

  @Transactional
  public void deletePatron(int patronId) {
    Patron patron = getPatronById(patronId);

    if (hasActiveLoans(patronId)) {
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION,
          "Cannot delete patron with active loans",
          HttpStatus.CONFLICT);
    }

    patronRepository.delete(patron);
    log.info("Deleted patron with ID {}", patronId);
  }

  protected boolean hasActiveLoans(int patronId) {
    return loanRepository.existsByPatronIdAndStatus(patronId, LoanStatus.ACTIVE);
  }

  @Transactional
  public void suspendPatron(int patronId) {
    Optional<Patron> patron = patronRepository.findByPatronId(patronId);
    if (patron.isPresent()) {
      if (patron.get().getStatus() == PatronStatus.SUSPENDED) {
        log.error("Patron with ID provided is already suspended: {}", patronId);
        throw new GeneralException(
            ExceptionType.INVALID_OPERATION,
            "Patron with ID provided is already suspended: " + patronId,
            HttpStatus.FORBIDDEN);
      }
      patron.get().setStatus(PatronStatus.SUSPENDED);
      patronRepository.save(patron.get());
      notificationService.notifyPatronStatusChange(patronId, PatronStatus.SUSPENDED);
    } else {
      log.warn("Patron with ID provided not found: {}", patronId);
      throw new GeneralException(
          ExceptionType.PATRON_NOT_FOUND,
          "Patron with ID provided not found: " + patronId,
          HttpStatus.NOT_FOUND);
    }
  }

  @Transactional
  public Patron addNewPatron(Patron patron) {

    // Check if patron with the same name and date of birth
    if (!patronRepository
        .findByPatronNameAndDateOfBirth(patron.getPatronName(), patron.getDateOfBirth())
        .isEmpty()) {
      log.error(
          "A patron with the name {} and date of birth {} already exists",
          patron.getPatronName(),
          patron.getDateOfBirth());
      throw new GeneralException(
          ExceptionType.PATRON_ALREADY_EXISTS,
          "A patron with the name "
              + patron.getPatronName()
              + " and date of birth "
              + patron.getDateOfBirth()
              + " already exists",
          HttpStatus.FORBIDDEN);
    }

    // Get the next sequence value for the patron ID
    int nextPatronNumber = sequenceGenerator.getNextSequenceValueForPatron();
    patron.setPatronId(nextPatronNumber);

    // Set both the created_date and last_update_date to the current timestamp
    LocalDateTime now = LocalDateTime.now();
    patron.setCreated_date(now);
    patron.setLastUpdateDate(now);
    patron.setStatus(PatronStatus.ACTIVE);

    // Save the patron to the database
    Patron savedPatron = patronRepository.save(patron);

    // Return a ReponseEntity with the status CREATE and the saved patron
    return savedPatron;
  }

  // Helper Methods

  // Determine if the patron is a minor
  protected boolean isMinor(LocalDate dateOfBirth) {
    return Period.between(dateOfBirth, LocalDate.now()).getYears() < 18;
  }

  public boolean isPatronSuspended(int patronId) {
    return patronRepository
        .findByPatronId(patronId)
        .map(patron -> patron.getStatus() == PatronStatus.SUSPENDED)
        .orElse(false);
  }

  // Eliminate repetitive exception handling
  private List<Patron> handleNotFound(List<Patron> patrons, String message) {
    if (patrons.isEmpty()) {
      log.error(message);
      throw new GeneralException(ExceptionType.PATRON_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
    return patrons;
  }

  // Helper method to update the patron record after a media item is checked out
  @Transactional
  public void updatePatronAfterCheckout(Patron patron, int mediaId) {
    patron.getCheckedOutItems().add(mediaId);
    if (patron.getStatus() == PatronStatus.INACTIVE) {
      patron.setStatus(PatronStatus.ACTIVE);
    }
    patron.setLastUpdateDate(LocalDateTime.now());
    patronRepository.save(patron);
    log.info("Updated patron {} after checkout of mediaId {}", patron.getPatronId(), mediaId);
  }

  // Helper method to update the patron record after a media item is returned
  @Transactional
  public void updatePatronAfterReturn(Patron patron, int mediaId) {
    patron.getCheckedOutItems().remove(Integer.valueOf(mediaId));
    patron.setLastUpdateDate(LocalDateTime.now());
    patronRepository.save(patron);
    log.info("Updated patron {} after return of mediaId {}", patron.getPatronId(), mediaId);
  }
}
