package com.randomlake.library.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.model.Patron;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class PatronRepositoryTest {

  @Mock private PatronRepository patronRepository;

  private Patron patron;
  private String patronName;
  private LocalDate dateOfBirth;
  private PatronStatus patronStatus;
  private String telephoneHome;
  private String emailAddress;

  @BeforeEach
  public void setup() {

    // Initialize mocks
    MockitoAnnotations.openMocks(this);

    // Define the mock data once
    patronName = "John Q. Public";
    dateOfBirth = LocalDate.of(1990, 1, 1);
    patronStatus = PatronStatus.ACTIVE;
    telephoneHome = "311-555-2368";
    emailAddress = "jqp@example.com";

    // Set up the mock Patron object using these variables
    patron = new Patron();
    patron.setPatronId(1);
    patron.setPatronName(patronName);
    patron.setDateOfBirth(dateOfBirth);
    patron.setStatus(patronStatus);
    patron.setTelephoneHome(telephoneHome);
    patron.setEmailAddress(emailAddress);
  }

  @Test
  public void testFindByPatronIdSuccess() {
    // Arrange
    when(patronRepository.findByPatronId(1)).thenReturn(Optional.of(patron));

    // Act
    Optional<Patron> foundPatron = patronRepository.findByPatronId(1);

    // Assert
    assertTrue(foundPatron.isPresent());
    assertEquals(patron.getPatronName(), foundPatron.get().getPatronName());
  }

  @Test
  public void testFindByPatronNameSuccess() {
    List<Patron> patrons = List.of(patron);
    when(patronRepository.findByPatronName(patronName)).thenReturn(patrons);

    List<Patron> foundPatrons = patronRepository.findByPatronName(patronName);

    assertEquals(1, foundPatrons.size());
    assertEquals(patronName, foundPatrons.get(0).getPatronName());
  }

  @Test
  public void testFindByDateOfBirthSuccess() {
    List<Patron> patrons = List.of(patron);
    when(patronRepository.findByDateOfBirth(dateOfBirth)).thenReturn(patrons);

    List<Patron> foundPatrons = patronRepository.findByDateOfBirth(dateOfBirth);

    assertEquals(1, foundPatrons.size());
    assertEquals(dateOfBirth, foundPatrons.get(0).getDateOfBirth());
  }

  @Test
  public void testFindByEmailAddressSuccess() {
    List<Patron> patrons = List.of(patron);
    when(patronRepository.findByEmailAddress(emailAddress)).thenReturn(patrons);

    List<Patron> foundPatrons = patronRepository.findByEmailAddress(emailAddress);

    assertEquals(1, foundPatrons.size());
    assertEquals(emailAddress, foundPatrons.get(0).getEmailAddress());
  }

  @Test
  public void testFindByStatusSuccess() {
    List<Patron> patrons = List.of(patron);
    when(patronRepository.findByStatus(PatronStatus.ACTIVE)).thenReturn(patrons);

    List<Patron> foundPatrons = patronRepository.findByStatus(PatronStatus.ACTIVE);

    assertEquals(1, foundPatrons.size());
    assertEquals(PatronStatus.ACTIVE, foundPatrons.get(0).getStatus());
  }

  @Test
  public void testDeleteByIdSuccess() {
    doNothing().when(patronRepository).deleteByPatronId(1);

    patronRepository.deleteByPatronId(1);

    verify(patronRepository, times(1)).deleteByPatronId(1);
  }
}
