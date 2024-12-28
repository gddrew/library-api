package com.randomlake.library.dto;

import com.randomlake.library.enums.PatronStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatronRequest {

  @NotNull private String patronName;
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
}
