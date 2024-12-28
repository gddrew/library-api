package com.randomlake.library.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportCardPatron {

  private int cardId;
  private String cardStatus;
  private int patronId;
  private String patronStatus;
  private String patronName;
  private String streetAddress;
  private String cityName;
  private String stateName;
  private String zipCode;
  private String telephoneHome;
  private String telephoneMobile;
  private String emailAddress;
}
