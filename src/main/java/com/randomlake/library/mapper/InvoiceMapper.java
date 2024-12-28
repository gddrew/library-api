package com.randomlake.library.mapper;

import com.randomlake.library.dto.InvoiceResponse;
import com.randomlake.library.model.Invoice;

public class InvoiceMapper {

  public static InvoiceResponse toDto(Invoice invoice) {

    if (invoice == null) {
      return null;
    }

    InvoiceResponse response = new InvoiceResponse();
    response.setInvoiceId(invoice.getInvoiceId());
    response.setPatronId(invoice.getPatronId());
    response.setAmount(invoice.getAmount());
    response.setStatus(invoice.getStatus());
    response.setDate(invoice.getDate());
    response.setCampaign(invoice.getCampaign());
    return response;
  }
}
