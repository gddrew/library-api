package com.randomlake.library.mapper;

import com.randomlake.library.dto.PatronRequest;
import com.randomlake.library.dto.PatronResponse;
import com.randomlake.library.model.Patron;

public class PatronMapper {

  public static Patron toEntity(PatronRequest patronRequest) {
    if (patronRequest == null) {
      return null;
    }
    Patron patron = new Patron();
    patron.setPatronName(patronRequest.getPatronName());
    patron.setDateOfBirth(patronRequest.getDateOfBirth());
    patron.setStreetAddress(patronRequest.getStreetAddress());
    patron.setCityName(patronRequest.getCityName());
    patron.setStateName(patronRequest.getStateName());
    patron.setZipCode(patronRequest.getZipCode());
    patron.setTelephoneHome(patronRequest.getTelephoneHome());
    patron.setTelephoneMobile(patronRequest.getTelephoneMobile());
    patron.setEmailAddress(patronRequest.getEmailAddress());
    patron.setContactMethod(patronRequest.getContactMethod());
    patron.setStatus(patronRequest.getStatus());
    return patron;
  }

  public static PatronResponse toDto(Patron patron) {
    if (patron == null) {
      return null;
    }
    PatronResponse response = new PatronResponse();
    response.setPatronId(patron.getPatronId());
    response.setCreated_date(patron.getCreated_date());
    response.setPatronName(patron.getPatronName());
    response.setDateOfBirth(patron.getDateOfBirth());
    response.setStreetAddress(patron.getStreetAddress());
    response.setCityName(patron.getCityName());
    response.setStateName(patron.getStateName());
    response.setZipCode(patron.getZipCode());
    response.setTelephoneHome(patron.getTelephoneHome());
    response.setTelephoneMobile(patron.getTelephoneMobile());
    response.setEmailAddress(patron.getEmailAddress());
    response.setContactMethod(patron.getContactMethod());
    response.setStatus(patron.getStatus());
    response.setLastUpdateDate(patron.getLastUpdateDate());
    return response;
  }
}
