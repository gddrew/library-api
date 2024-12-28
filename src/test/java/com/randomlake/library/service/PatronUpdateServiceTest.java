package com.randomlake.library.service;

import static com.randomlake.library.enums.ContactMethodType.EMAIL;
import static org.junit.jupiter.api.Assertions.*;

import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.model.Patron;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class PatronUpdateServiceTest {

  private PatronUpdateService patronUpdateService;
  private Patron patron;

  @BeforeEach
  void setup() {
    patronUpdateService = new PatronUpdateService();
    patron = new Patron();
  }

  @Test
  public void testApplyPartialUpdate_ValidFields() {
    Map<String, Object> updates = new HashMap<>();
    updates.put("patronName", "Updated Name");
    updates.put("streetAddress", "Updated Street Address");
    updates.put("telephoneHome", "3115552368");
    updates.put("contactMethod", String.valueOf(EMAIL));
    updates.put("dateOfBirth", LocalDate.of(2021, 10, 1));

    patronUpdateService.applyPartialUpdates(patron, updates);

    assertEquals("Updated Name", patron.getPatronName());
    assertEquals("Updated Street Address", patron.getStreetAddress());
    assertEquals("3115552368", patron.getTelephoneHome());
    assertEquals(String.valueOf(EMAIL), patron.getContactMethod());
    assertEquals(LocalDate.of(2021, 10, 1), patron.getDateOfBirth());
    assertNotNull(patron.getLastUpdateDate());
  }

  @Test
  void testApplyPartialUpdates_InvalidStatus_ThrowsException() {
    Map<String, Object> updates = new HashMap<>();
    updates.put("status", "INVALID_STATUS");

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              patronUpdateService.applyPartialUpdates(patron, updates);
            });

    assertEquals("Invalid patron status: INVALID_STATUS", exception.getMessage());
  }

  @Test
  void testApplyFullUpdate_AllFields() {
    Patron fullUpdate = new Patron();
    fullUpdate.setPatronName("George Public");
    fullUpdate.setDateOfBirth(LocalDate.of(1969, 7, 20));
    fullUpdate.setStreetAddress("123 Main Street");
    fullUpdate.setCityName("Anytown");
    fullUpdate.setStateName("Ohio");
    fullUpdate.setZipCode("44444");
    fullUpdate.setTelephoneHome("3115552368");
    fullUpdate.setTelephoneMobile("3115551212");
    fullUpdate.setEmailAddress("george@public.com");
    fullUpdate.setContactMethod(String.valueOf(EMAIL));
    fullUpdate.setStatus(PatronStatus.ACTIVE);

    patronUpdateService.applyFullUpdate(patron, fullUpdate);

    assertEquals("George Public", patron.getPatronName());
    assertEquals(LocalDate.of(1969, 7, 20), patron.getDateOfBirth());
    assertEquals("123 Main Street", patron.getStreetAddress());
    assertEquals("Anytown", patron.getCityName());
    assertEquals("Ohio", patron.getStateName());
    assertEquals("44444", patron.getZipCode());
    assertEquals("3115552368", patron.getTelephoneHome());
    assertEquals("3115551212", patron.getTelephoneMobile());
    assertEquals("george@public.com", patron.getEmailAddress());
    assertEquals(String.valueOf(EMAIL), patron.getContactMethod());
    assertEquals(PatronStatus.ACTIVE, patron.getStatus());
    assertNotNull(patron.getLastUpdateDate());
  }
}
