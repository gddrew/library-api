package com.randomlake.library.service;

import com.randomlake.library.enums.ExceptionType;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Invoice;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.InvoiceRepository;
import com.randomlake.library.repository.PatronRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final PatronRepository patronRepository;
  private final SequenceGenerator sequenceGerator;

  private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

  public InvoiceService(
      InvoiceRepository invoiceRepository,
      PatronRepository patronRepository,
      SequenceGenerator sequenceGerator) {
    this.invoiceRepository = invoiceRepository;
    this.patronRepository = patronRepository;
    this.sequenceGerator = sequenceGerator;
  }

  @Transactional
  public Invoice createInvoice(int patronId, int amount, String status, String campaign) {
    if (amount <= 0) {
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION,
          "Invoice amount must be greater than zero",
          HttpStatus.BAD_REQUEST);
    }

    Optional<Patron> patron = patronRepository.findByPatronId(patronId);
    if (patron.isEmpty()) {
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION, "Patron not found", HttpStatus.BAD_REQUEST);
    }

    Invoice invoice = new Invoice();
    invoice.setInvoiceId(sequenceGerator.getNextSequenceValueForInvoice());
    invoice.setPatronId(patronId);
    invoice.setAmount(amount);
    invoice.setStatus(status);
    invoice.setCampaign(campaign);
    invoice.setDate(LocalDate.now());

    return invoiceRepository.save(invoice);
  }

  @Transactional
  public Invoice updateInvoice(
      int invoiceId, int patronId, int amount, String campaign, String status) {
    Invoice invoice = getInvoiceById(invoiceId);

    // Update fields if they are provided
    if (amount > 0) {
      invoice.setAmount(amount);
    } else if (amount <= 0) {
      throw new GeneralException(
          ExceptionType.INVALID_OPERATION,
          "Invoice amount must be greater than zero",
          HttpStatus.BAD_REQUEST);
    }

    Optional.of(patronId).ifPresent(invoice::setPatronId);
    Optional.of(status).ifPresent(invoice::setStatus);
    Optional.of(campaign).ifPresent(invoice::setCampaign);

    return invoiceRepository.save(invoice);
  }

  public List<Invoice> getInvoicesByPatronId(int patronId) {
    return invoiceRepository.findByPatronId(patronId);
  }

  public Invoice getInvoiceById(int invoiceId) {
    return invoiceRepository
        .findByInvoiceId(invoiceId)
        .orElseThrow(
            () ->
                new GeneralException(
                    ExceptionType.INVOICE_NOT_FOUND, "Invoice not found", HttpStatus.NOT_FOUND));
  }
}
