package com.randomlake.library.dto;

import com.randomlake.library.enums.PatronStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatronResponse {
  private int patronId;
  private LocalDateTime created_date;
  private String patronName;
  private LocalDate dateOfBirth;
  private String streetAddress;
  private String cityName;
  private String stateName;
  private String zipCode;
  private String telephoneHome;
  private String telephoneMobile;
  private String emailAddress;
  private String contactMethod;
  private PatronStatus status;
  private LocalDateTime lastUpdateDate;
}
