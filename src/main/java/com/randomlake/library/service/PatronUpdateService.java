package com.randomlake.library.service;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Patron;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatronUpdateService {

  private static final Logger log = LoggerFactory.getLogger(PatronUpdateService.class);

  @Transactional
  public void applyPartialUpdates(Patron patron, Map<String, Object> updates) {
    updates.forEach(
        (key, value) -> {
          switch (key) {
            case "patronName":
              patron.setPatronName((String) value);
              break;
            case "dateOfBirth":
              patron.setDateOfBirth((LocalDate) value);
              break;
            case "streetAddress":
              patron.setStreetAddress((String) value);
              break;
            case "cityName":
              patron.setCityName((String) value);
              break;
            case "stateName":
              patron.setStateName((String) value);
              break;
            case "zipCode":
              patron.setZipCode((String) value);
              break;
            case "telephoneHome":
              patron.setTelephoneHome((String) value);
              break;
            case "telephoneMobile":
              patron.setTelephoneMobile((String) value);
              break;
            case "emailAddress":
              patron.setEmailAddress((String) value);
              break;
            case "contactMethod":
              patron.setContactMethod((String) value);
              break;
            case "status":
              try {
                patron.setStatus(PatronStatus.valueOf((String) value));
              } catch (IllegalArgumentException e) {
                throw new GeneralException(
                    ExceptionType.INVALID_INPUT,
                    "Invalid patron status: " + value,
                    HttpStatus.BAD_REQUEST);
              }
              break;
            default:
              log.error("Attempted to update unknown property '{}' with value '{}'", key, value);
              throw new IllegalArgumentException("Unknown property: " + key);
          }
          patron.setLastUpdateDate(LocalDateTime.now());
        });
  }

  @Transactional
  public void applyFullUpdate(Patron patron, Patron fullUpdate) {
    patron.setPatronName(fullUpdate.getPatronName());
    patron.setDateOfBirth(fullUpdate.getDateOfBirth());
    patron.setStreetAddress(fullUpdate.getStreetAddress());
    patron.setCityName(fullUpdate.getCityName());
    patron.setStateName(fullUpdate.getStateName());
    patron.setZipCode(fullUpdate.getZipCode());
    patron.setTelephoneHome(fullUpdate.getTelephoneHome());
    patron.setTelephoneMobile(fullUpdate.getTelephoneMobile());
    patron.setEmailAddress(fullUpdate.getEmailAddress());
    patron.setContactMethod(fullUpdate.getContactMethod());
    patron.setStatus(fullUpdate.getStatus());
    patron.setLastUpdateDate(LocalDateTime.now());
  }
}
