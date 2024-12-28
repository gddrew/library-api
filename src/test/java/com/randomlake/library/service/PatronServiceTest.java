package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.LoanStatus;
import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.LoanRepository;
import com.randomlake.library.repository.PatronRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

public class PatronServiceTest {

  @Mock private PatronRepository patronRepository;
  @Mock private PatronUpdateService patronUpdateService;
  @Mock private SequenceGenerator sequenceGenerator;
  @Mock private NotificationService notificationService;
  @Mock private LoanRepository loanRepository;

  @InjectMocks private PatronService patronService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGetAllPatrons_Success() {
    Patron patron = new Patron();
    when(patronRepository.findAll()).thenReturn(List.of(patron));

    List<Patron> result = patronService.getAllPatrons();

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(patronRepository, times(1)).findAll();
  }

  @Test
  public void testGetAllPatrons_NotFound() {
    when(patronRepository.findAll()).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.getAllPatrons());

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals("No patrons found", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    verify(patronRepository, times(1)).findAll();
  }

  @Test
  public void testGetPatronById() {
    int patronId = 1;
    Patron patron = new Patron();
    patron.setPatronId(patronId);

    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.of(patron));

    Optional<Patron> result = Optional.ofNullable(patronService.getPatronById(patronId));

    assertTrue(result.isPresent());
    assertEquals(patronId, result.get().getPatronId());
    verify(patronRepository, times(1)).findByPatronId(patronId);
  }

  @Test
  public void testGetPatronById_NotFound() {
    int patronId = 1;

    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.getPatronById(patronId));

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals("Patron not found with ID: " + patronId, exception.getMessage());
    verify(patronRepository, times(1)).findByPatronId(patronId);
  }

  @Test
  public void testGetPatronByName() {
    Patron patron = new Patron();
    when(patronRepository.findByPatronName("John Doe")).thenReturn(List.of(patron));

    List<Patron> result = patronService.getPatronByName("John Doe");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(patron, result.get(0));
    verify(patronRepository, times(1)).findByPatronName("John Doe");
  }

  @Test
  public void testGetPatronByName_NotFound() {
    String patronName = "John Doe";
    when(patronRepository.findByPatronName(patronName)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.getPatronByName(patronName));

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals("No patrons with name provided found: " + patronName, exception.getMessage());
    verify(patronRepository, times(1)).findByPatronName(patronName);
  }

  @Test
  public void testGetPatronByDateOfBirth() {
    Patron patron = new Patron();
    when(patronRepository.findByDateOfBirth(LocalDate.of(2000, 1, 15))).thenReturn(List.of(patron));

    List<Patron> result = patronService.getPatronByDateOfBirth(LocalDate.of(2000, 1, 15));

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(patron, result.get(0));
    verify(patronRepository, times(1)).findByDateOfBirth(LocalDate.of(2000, 1, 15));
  }

  @Test
  public void testGetPatronByDateOfBirth_NotFound() {
    LocalDate dateOfBirth = LocalDate.of(2000, 1, 15);
    when(patronRepository.findByDateOfBirth(dateOfBirth)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> patronService.getPatronByDateOfBirth(dateOfBirth));

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals(
        "No patrons with date of birth provided found: " + dateOfBirth, exception.getMessage());
    verify(patronRepository, times(1)).findByDateOfBirth(dateOfBirth);
  }

  @Test
  public void testGetPatronByTelephone() {
    Patron patron = new Patron();
    when(patronRepository.findByTelephone("1234567890")).thenReturn(List.of(patron));

    List<Patron> result = patronService.getPatronByTelephone("1234567890");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(patron, result.get(0));
    verify(patronRepository, times(1)).findByTelephone("1234567890");
  }

  @Test
  public void testGetPatronByTelephone_NotFound() {
    String telephone = "1234567890";
    when(patronRepository.findByTelephone(telephone)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.getPatronByTelephone(telephone));

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals(
        "No patrons with telephone number provided found: " + telephone, exception.getMessage());
    verify(patronRepository, times(1)).findByTelephone(telephone);
  }

  @Test
  public void testGetPatronByEmail() {
    Patron patron = new Patron();
    when(patronRepository.findByEmailAddress("john.doe@example.com")).thenReturn(List.of(patron));

    List<Patron> result = patronService.getPatronByEmail("john.doe@example.com");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(patron, result.get(0));
    verify(patronRepository, times(1)).findByEmailAddress("john.doe@example.com");
  }

  @Test
  public void testGetPatronByEmail_NotFound() {
    String emailAddress = "xyz@example.com";
    when(patronRepository.findByEmailAddress(emailAddress)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.getPatronByEmail(emailAddress));

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals(
        "No patrons with email address provided found: " + emailAddress, exception.getMessage());
    verify(patronRepository, times(1)).findByEmailAddress(emailAddress);
  }

  @Test
  public void testUpdatePatron_Success() {
    Patron patron = new Patron();
    patron.setPatronId(1);
    patron.setPatronName("John Doe");
    patron.setDateOfBirth(LocalDate.of(2000, 1, 15));
    patron.setEmailAddress("john.doe@example.com");
    patron.setTelephoneHome("1234567890");
    patron.setTelephoneMobile("0987654321");
    patron.setStatus(PatronStatus.ACTIVE);

    Patron updatedPatron = new Patron();
    updatedPatron.setPatronId(1);
    updatedPatron.setPatronName("John Q. Doe");
    updatedPatron.setDateOfBirth(LocalDate.of(2000, 1, 15));
    updatedPatron.setEmailAddress("john.doe@example.com");
    updatedPatron.setTelephoneHome("1234567890");
    updatedPatron.setTelephoneMobile("0987654321");
    updatedPatron.setStatus(PatronStatus.ACTIVE);

    when(patronRepository.findByPatronId(anyInt())).thenReturn(Optional.of(patron));
    when(patronRepository.save(patron)).thenReturn(updatedPatron);

    Patron result = patronService.updatePatron(1, Map.of("patronName", "John Q. Doe"), null);

    assertNotNull(result);
    assertEquals("John Q. Doe", result.getPatronName());
    assertEquals(LocalDate.of(2000, 1, 15), result.getDateOfBirth());
    assertEquals("john.doe@example.com", result.getEmailAddress());
    assertEquals("1234567890", result.getTelephoneHome());
    assertEquals("0987654321", result.getTelephoneMobile());
    assertEquals(PatronStatus.ACTIVE, result.getStatus());

    verify(patronRepository, times(1)).findByPatronId(1);
    verify(patronRepository, times(1)).save(any(Patron.class));
  }

  @Test
  public void testUpdatePatron_NotFound() {
    when(patronRepository.findByPatronId(1)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> patronService.updatePatron(1, Map.of("patronName", "John Q. Doe"), null));

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals("Patron with ID provided not found: 1", exception.getMessage());
    verify(patronRepository, times(1)).findByPatronId(1);
  }

  @Test
  public void testDeletePatron_Success() {
    Patron patron = new Patron();
    patron.setPatronId(1);
    patron.setPatronName("John Doe");
    patron.setDateOfBirth(LocalDate.of(2000, 1, 15));
    patron.setEmailAddress("john.doe@example.com");
    patron.setTelephoneHome("1234567890");
    patron.setTelephoneMobile("0987654321");
    patron.setStatus(PatronStatus.ACTIVE);

    when(patronRepository.findByPatronId(1)).thenReturn(Optional.of(patron));

    patronService.deletePatron(1);

    verify(patronRepository, times(1)).findByPatronId(1);
    verify(patronRepository, times(1)).delete(patron);
  }

  @Test
  public void testDeletePatron_HasActiveLoan() {
    int patronId = 1;
    Patron patron = new Patron();
    patron.setPatronId(patronId);
    patron.setPatronName("John Doe");
    patron.setDateOfBirth(LocalDate.of(2000, 1, 15));
    patron.setEmailAddress("john.doe@example.com");
    patron.setTelephoneHome("1234567890");
    patron.setTelephoneMobile("0987654321");
    patron.setStatus(PatronStatus.ACTIVE);

    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.of(patron));
    when(loanRepository.existsByPatronIdAndStatus(patronId, LoanStatus.ACTIVE)).thenReturn(true);

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.deletePatron(patronId));

    assertEquals(ExceptionType.INVALID_OPERATION, exception.getType());
    assertEquals("Cannot delete patron with active loans", exception.getMessage());
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());

    verify(patronRepository, times(1)).findByPatronId(patronId);
    verify(loanRepository, times(1)).existsByPatronIdAndStatus(patronId, LoanStatus.ACTIVE);
    verify(patronRepository, never()).delete(patron);
  }

  @Test
  public void testDeletePatron_NotFound() {
    when(patronRepository.findByPatronId(1)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.deletePatron(1));

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals("Patron not found with ID: 1", exception.getMessage());
    verify(patronRepository, times(1)).findByPatronId(1);
    verify(patronRepository, times(0)).delete(any());
  }

  @Test
  public void testSuspendPatron_Success() {
    Patron patron = new Patron();
    patron.setPatronId(1);
    patron.setStatus(PatronStatus.ACTIVE);

    when(patronRepository.findByPatronId(1)).thenReturn(Optional.of(patron));

    patronService.suspendPatron(1);

    assertEquals(PatronStatus.SUSPENDED, patron.getStatus());
    verify(patronRepository, times(1)).save(patron);
    verify(notificationService, times(1)).notifyPatronStatusChange(1, PatronStatus.SUSPENDED);
  }

  @Test
  public void testSuspendPatron_AlreadySuspended() {
    Patron patron = new Patron();
    patron.setPatronId(1);
    patron.setStatus(PatronStatus.SUSPENDED);

    when(patronRepository.findByPatronId(1)).thenReturn(Optional.of(patron));

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.suspendPatron(1));

    assertEquals(ExceptionType.INVALID_OPERATION, exception.getType());
    assertEquals("Patron with ID provided is already suspended: 1", exception.getMessage());
    verify(patronRepository, times(0)).save(patron);
    verify(notificationService, times(0)).notifyPatronStatusChange(anyInt(), any());
  }

  @Test
  public void testSuspendPatron_NotFound() {
    when(patronRepository.findByPatronId(1)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.suspendPatron(1));

    assertEquals(ExceptionType.PATRON_NOT_FOUND, exception.getType());
    assertEquals("Patron with ID provided not found: 1", exception.getMessage());
    verify(patronRepository, times(0)).save(any());
    verify(notificationService, times(0)).notifyPatronStatusChange(anyInt(), any());
  }

  @Test
  public void testAddNewPatron_Success() {
    Patron patron = new Patron();
    patron.setPatronName("John Doe");
    patron.setDateOfBirth(LocalDate.of(2000, 1, 15));
    when(patronRepository.findByPatronNameAndDateOfBirth("John Doe", LocalDate.of(2000, 1, 15)))
        .thenReturn(List.of());
    when(sequenceGenerator.getNextSequenceValueForPatron()).thenReturn(1);
    when(patronRepository.save(patron)).thenReturn(patron);

    Patron result = patronService.addNewPatron(patron);

    assertNotNull(result);
    assertEquals(1, result.getPatronId());
    verify(patronRepository, times(1))
        .findByPatronNameAndDateOfBirth("John Doe", LocalDate.of(2000, 1, 15));
    verify(sequenceGenerator, times(1)).getNextSequenceValueForPatron();
    verify(patronRepository, times(1)).save(patron);
  }

  @Test
  public void testAddNewPatron_AlreadyExists() {
    Patron patron = new Patron();
    patron.setPatronName("John Doe");
    patron.setDateOfBirth(LocalDate.of(2000, 1, 15));
    when(patronRepository.findByPatronNameAndDateOfBirth("John Doe", LocalDate.of(2000, 1, 15)))
        .thenReturn(List.of(patron));

    GeneralException exception =
        assertThrows(GeneralException.class, () -> patronService.addNewPatron(patron));

    assertEquals(ExceptionType.PATRON_ALREADY_EXISTS, exception.getType());
    assertEquals(
        "A patron with the name John Doe and date of birth 2000-01-15 already exists",
        exception.getMessage());
    verify(patronRepository, times(1))
        .findByPatronNameAndDateOfBirth("John Doe", LocalDate.of(2000, 1, 15));
    verify(sequenceGenerator, times(0)).getNextSequenceValueForPatron();
    verify(patronRepository, times(0)).save(patron);
  }

  @Test
  public void testIsMinor_True() {
    LocalDate dateOfBirth = LocalDate.now().minusYears(17);
    assertTrue(patronService.isMinor(dateOfBirth));
  }

  @Test
  public void testIsMinor_False() {
    LocalDate dateOfBirth = LocalDate.now().minusYears(18);
    assertFalse(patronService.isMinor(dateOfBirth));
  }

  @Test
  public void testIsPatronSuspended() {
    Patron patron = new Patron();
    patron.setPatronId(1);
    patron.setStatus(PatronStatus.SUSPENDED);

    when(patronRepository.findByPatronId(1)).thenReturn(Optional.of(patron));

    boolean isSuspended = patronService.isPatronSuspended(1);

    assertTrue(isSuspended);
    verify(patronRepository, times(1)).findByPatronId(1);
  }

  @Test
  public void testIsPatronSuspended_NotSuspended() {
    Patron patron = new Patron();
    patron.setPatronId(1);
    patron.setStatus(PatronStatus.ACTIVE);

    when(patronRepository.findByPatronId(1)).thenReturn(Optional.of(patron));

    boolean isSuspended = patronService.isPatronSuspended(1);

    assertFalse(isSuspended);
    verify(patronRepository, times(1)).findByPatronId(1);
  }

  @Test
  public void testIsPatronSuspended_NotFound() {
    when(patronRepository.findByPatronId(1)).thenReturn(Optional.empty());

    boolean isSuspended = patronService.isPatronSuspended(1);

    assertFalse(isSuspended);
    verify(patronRepository, times(1)).findByPatronId(1);
  }
}
