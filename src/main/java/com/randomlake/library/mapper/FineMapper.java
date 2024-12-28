package com.randomlake.library.mapper;

import com.randomlake.library.dto.FineResponse;
import com.randomlake.library.model.Fine;

public class FineMapper {

  public static FineResponse toDto(Fine fine) {

    if (fine == null) {
      return null;
    }
    FineResponse response = new FineResponse();
    response.setFineId(fine.getFineId());
    response.setPatronId(fine.getPatronId());
    response.setMediaId(fine.getMediaId());
    response.setFineType(fine.getFineType());
    response.setAmount(fine.getAmount());
    response.setDateAssessed(fine.getDateAssessed());
    response.setDatePaid(fine.getDatePaid());
    response.setPaid(fine.isPaid());
    response.setWaived(fine.isWaived());
    return response;
  }
}
