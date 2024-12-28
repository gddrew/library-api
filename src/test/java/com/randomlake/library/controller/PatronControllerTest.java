package com.randomlake.library.controller;

import static com.randomlake.library.enums.PatronStatus.ACTIVE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.randomlake.library.config.TestSecurityConfig;
import com.randomlake.library.model.Patron;
import com.randomlake.library.service.PatronService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = {TestSecurityConfig.class})
@WebMvcTest(PatronController.class)
public class PatronControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PatronService patronService;

  // Declare reusable patron instances
  private Patron patron1;
  private Patron patron2;

  @BeforeEach
  public void setup() {

    patron1 =
        new Patron(
            new ObjectId(),
            1,
            LocalDateTime.now(),
            "John Q. Public",
            LocalDate.of(1969, 7, 20),
            "123 Main Street",
            "Anytown",
            "OH",
            "44444",
            "3115552368",
            "3115551212",
            "john.public@example.com",
            "email",
            ACTIVE,
            Collections.singletonList(0));
    patron2 =
        new Patron(
            new ObjectId(),
            2,
            LocalDateTime.now(),
            "Jane M. Public",
            LocalDate.of(1975, 6, 26),
            "123 Main Street",
            "Anytown",
            "OH",
            "44444",
            "3115552368",
            "3115551213",
            "jane.public@example.com",
            "email",
            ACTIVE,
            Collections.singletonList(0));
  }

  @Test
  public void testGetAllPatrons_Success() throws Exception {

    List<Patron> patronList = Arrays.asList(patron1, patron2);

    when(patronService.getAllPatrons()).thenReturn(patronList);

    mockMvc
        .perform(get("/api/patrons"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].patronName").value("John Q. Public"))
        .andExpect(jsonPath("$[0].emailAddress").value("john.public@example.com"))
        .andExpect(jsonPath("$[1].patronName").value("Jane M. Public"))
        .andExpect(jsonPath("$[1].emailAddress").value("jane.public@example.com"));
  }

  @Test
  public void testGetPatronById_Success() throws Exception {
    when(patronService.getPatronById(1)).thenReturn(patron1);

    mockMvc
        .perform(get("/api/patrons/{patronId}", 1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.patronName", is("John Q. Public")))
        .andExpect(jsonPath("$.patronId", is(1)))
        .andExpect(jsonPath("$.streetAddress", is("123 Main Street")))
        .andExpect(jsonPath("$.cityName", is("Anytown")))
        .andExpect(jsonPath("$.stateName", is("OH")))
        .andExpect(jsonPath("$.zipCode", is("44444")))
        .andExpect(jsonPath("$.telephoneHome", is("3115552368")))
        .andExpect(jsonPath("$.telephoneMobile", is("3115551212")))
        .andExpect(jsonPath("$.emailAddress", is("john.public@example.com")))
        .andExpect(jsonPath("$.contactMethod", is("email")))
        .andExpect(jsonPath("$.status", is("ACTIVE")));
  }

  @Test
  public void testGetPatronById_NotFound() throws Exception {
    when(patronService.getPatronById(99)).thenReturn(null);

    mockMvc.perform(get("/api/patrons/{patronId}", 99)).andExpect(status().isNotFound());
  }

  @Test
  public void testGetPatronByName_Success() throws Exception {
    List<Patron> patronList = Collections.singletonList(patron2);

    when(patronService.getPatronByName("Jane M. Public")).thenReturn(patronList);

    mockMvc
        .perform(get("/api/patrons/patron/name/{patronName}", "Jane M. Public"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$.[0].patronName", is("Jane M. Public")))
        .andExpect(jsonPath("$[0].emailAddress", is("jane.public@example.com")));
  }

  @Test
  public void testGetPatronByName_NotFound() throws Exception {
    when(patronService.getPatronByName("Jane M. Public")).thenReturn(null);

    mockMvc
        .perform(get("/api/patrons/patron/name/{patronName}", "Jane M. Public"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPatronByDateOfBirth_Success() throws Exception {
    List<Patron> patronList = Collections.singletonList(patron1);

    when(patronService.getPatronByDateOfBirth(LocalDate.of(1969, 7, 20))).thenReturn(patronList);

    mockMvc
        .perform(get("/api/patrons/patron/dob/{dateOfBirth}", LocalDate.of(1969, 7, 20)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$.[0].patronName", is("John Q. Public")))
        .andExpect(jsonPath("$[0].emailAddress", is("john.public@example.com")));
  }

  @Test
  public void testGetPatronByDateOfBirth_NotFound() throws Exception {
    when(patronService.getPatronByDateOfBirth(LocalDate.of(1969, 7, 20))).thenReturn(null);

    mockMvc
        .perform(get("/api/patrons/patron/dob/{dateOfBirth}", LocalDate.of(1969, 7, 20)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPatronByTelephone_Success() throws Exception {
    List<Patron> patronList = Collections.singletonList(patron1);

    when(patronService.getPatronByTelephone("3115552368")).thenReturn(patronList);

    mockMvc
        .perform(get("/api/patrons/patron/telephone/{telephoneHome}", "3115552368"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$.[0].patronName", is("John Q. Public")))
        .andExpect(jsonPath("$[0].telephoneHome", is("3115552368")));
  }

  @Test
  public void testGetPatronByTelephone_NotFound() throws Exception {
    when(patronService.getPatronByTelephone("3115552368")).thenReturn(null);

    mockMvc
        .perform(get("/api/patrons/patron/telephone/{telephoneHome}", "3115552368"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPatronByEmail_Success() throws Exception {
    List<Patron> patronList = Collections.singletonList(patron2);

    when(patronService.getPatronByEmail("jane.public@example.com")).thenReturn(patronList);

    mockMvc
        .perform(get("/api/patrons/patron/email/{emailAddress}", "jane.public@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$.[0].patronName", is("Jane M. Public")))
        .andExpect(jsonPath("$[0].emailAddress", is("jane.public@example.com")));
  }

  @Test
  public void testGetPatronByEmail_NotFound() throws Exception {
    when(patronService.getPatronByEmail("jane.public@example.com")).thenReturn(null);

    mockMvc
        .perform(get("/api/patrons/patron/email/{emailAddress}", "jane.public@example.com"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAddNewPatron_Success() throws Exception {

    // Arrange: Mock the PatronService to return patron1 when addNewPatron is called
    when(patronService.addNewPatron(any(Patron.class))).thenReturn(patron1);

    // Act & Assert: Perform the POST request and validate the response
    mockMvc
        .perform(
            post("/api/patrons")
                .contentType("application/json")
                .content(
                    "{ \"patronName\": \"John Q. Public\", \"emailAddress\": \"john.public@example.com\", \"dateOfBirth\": \"1969-07-20\" }")) // Send a JSON body
        .andExpect(status().isCreated()) // Expect HTTP 201 Created
        .andExpect(
            jsonPath("$.patronName", is("John Q. Public"))) // Expect patronName "John Q. Public"
        .andExpect(
            jsonPath(
                "$.emailAddress",
                is("john.public@example.com"))) // Expect emailAddress "john.public@example.com"
        .andExpect(jsonPath("$.patronId", is(1))); // Expect patronId 1
  }

  @Test
  public void testUpdatePatron_Success() throws Exception {

    Patron updatedPatron =
        new Patron(
            new ObjectId(patron2.getId()),
            patron2.getPatronId(),
            patron2.getCreated_date(),
            "Jane Mendoza Public",
            patron2.getDateOfBirth(),
            patron2.getStreetAddress(),
            patron2.getCityName(),
            patron2.getStateName(),
            "44448",
            "3111012101",
            patron2.getTelephoneMobile(),
            patron2.getEmailAddress(),
            patron2.getContactMethod(),
            patron2.getStatus(),
            Collections.singletonList(0));

    when(patronService.updatePatron(eq(2), isNull(), any(Patron.class))).thenReturn(updatedPatron);

    mockMvc
        .perform(
            put("/api/patrons/{patronId}", 2)
                .contentType("application/json")
                .content(
                    "{ \"patronName\": \"Jane Mendoza Public\", \"zipCode\": \"44448\", \"telephoneHome\": \"3111012101\" }"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.patronName", is("Jane Mendoza Public")))
        .andExpect(jsonPath("$.zipCode", is("44448")))
        .andExpect(jsonPath("$.telephoneHome", is("3111012101")));
  }

  @Test
  public void testPatchUpdatePatron_Success() throws Exception {

    // Define the partial update as a map
    Map<String, Object> updates = Map.of("telephoneMobile", "9991112222");

    Patron patchUpdatedPatron =
        new Patron(
            new ObjectId(patron2.getId()),
            patron2.getPatronId(),
            patron2.getCreated_date(),
            patron2.getPatronName(),
            patron2.getDateOfBirth(),
            patron2.getStreetAddress(),
            patron2.getCityName(),
            patron2.getStateName(),
            patron2.getZipCode(),
            patron2.getTelephoneHome(),
            "9991112222",
            patron2.getEmailAddress(),
            patron2.getContactMethod(),
            patron2.getStatus(),
            Collections.singletonList(0));

    // Mock the PatronService to return the patched patron object
    when(patronService.updatePatron(eq(2), anyMap(), isNull())).thenReturn(patchUpdatedPatron);

    // Perform the PATCH request and assert the response
    mockMvc
        .perform(
            patch("/api/patrons/{patronId}", 2)
                .contentType("application/json")
                .content("{ \"telephoneMobile\": \"9991112222\" }"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.telephoneMobile", is("9991112222")));
  }
}
