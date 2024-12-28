package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Invoice;
import com.randomlake.library.model.Patron;
import com.randomlake.library.repository.InvoiceRepository;
import com.randomlake.library.repository.PatronRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InvoiceServiceTest {

  @Mock private InvoiceRepository invoiceRepository;

  @Mock private PatronRepository patronRepository;

  @Mock private SequenceGenerator sequenceGenerator;

  @InjectMocks private InvoiceService invoiceService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testCreateInvoiceSuccess() {
    int patronId = 1;
    int amount = 1500;
    String status = "pending";
    String campaign = "Friends of the Library";

    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.of(new Patron()));
    when(invoiceRepository.existsByPatronId(patronId)).thenReturn(false);
    when(sequenceGenerator.getNextSequenceValueForInvoice()).thenReturn(100);
    when(invoiceRepository.save(any(Invoice.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Invoice invoice = invoiceService.createInvoice(patronId, amount, status, campaign);

    assertNotNull(invoice);
    assertEquals(patronId, invoice.getPatronId());
    assertEquals(amount, invoice.getAmount());

    assertEquals(campaign, invoice.getCampaign());
    assertTrue(invoice.getAmount() > 0);
    verify(invoiceRepository).save(invoice);
  }

  @Test
  public void testCreateInvoice_InvalidAmount() {
    int patronId = 1;
    int amount = 0; // Invalid amount
    String status = "pending";
    String campaign = "Friends of the Library";

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> invoiceService.createInvoice(patronId, amount, status, campaign));

    assertEquals("Invoice amount must be greater than zero", exception.getMessage());
    verify(invoiceRepository, never()).save(any(Invoice.class)); // Save should not be called
  }

  @Test
  public void testCreateInvoice_PatronNotFound() {
    int patronId = 1;
    int amount = 1500;
    String status = "pending";
    String campaign = "Friends of the Library";

    when(patronRepository.findByPatronId(patronId)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> invoiceService.createInvoice(patronId, amount, status, campaign));

    assertEquals("Patron not found", exception.getMessage());
    verify(invoiceRepository, never()).save(any(Invoice.class)); // Save should not be called
  }

  @Test
  public void testUpdateInvoice_Success() {
    int invoiceId = 100;
    int patronId = 1;
    int amount = 1500;
    String campaign = "Annual Book Sale 2024";
    String status = "paid";

    Invoice invoice = new Invoice();
    invoice.setInvoiceId(invoiceId);
    invoice.setAmount(amount);

    when(invoiceRepository.findByInvoiceId(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceRepository.save(any(Invoice.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Invoice updatedInvoice =
        invoiceService.updateInvoice(invoiceId, patronId, amount, campaign, status);

    assertNotNull(updatedInvoice);
    assertEquals(campaign, updatedInvoice.getCampaign());
    assertEquals(status, updatedInvoice.getStatus());

    verify(invoiceRepository).save(invoice);
  }

  @Test
  public void testUpdateInvoice_InvalidAmount() {
    int invoiceId = 100;
    int patronId = 1;
    int amount = -1500; // Invalid negative amount
    String campaign = "Annual Book Sale 2024";
    String status = "paid";

    Invoice invoice = new Invoice();
    invoice.setInvoiceId(invoiceId);
    invoice.setAmount(1000); // Initial amount

    when(invoiceRepository.findByInvoiceId(invoiceId)).thenReturn(Optional.of(invoice));

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> invoiceService.updateInvoice(invoiceId, patronId, amount, campaign, status));

    assertEquals("Invoice amount must be greater than zero", exception.getMessage());
    verify(invoiceRepository, never()).save(any(Invoice.class)); // Save should not be called
  }

  @Test
  public void testUpdateInvoice_InvoiceNotFound() {
    int invoiceId = 100;
    int patronId = 1;
    int amount = 1500;
    String campaign = "Annual Book Sale 2024";
    String status = "paid";

    when(invoiceRepository.findByInvoiceId(invoiceId)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(
            GeneralException.class,
            () -> invoiceService.updateInvoice(invoiceId, patronId, amount, campaign, status));

    assertEquals("Invoice not found", exception.getMessage());
    verify(invoiceRepository, never()).save(any(Invoice.class)); // Save should not be called
  }

  @Test
  public void testGetInvoicesByPatronId_Success() {
    int patronId = 1;

    Invoice invoice1 = new Invoice();
    invoice1.setInvoiceId(100);
    invoice1.setPatronId(patronId);
    invoice1.setAmount(1500);

    Invoice invoice2 = new Invoice();
    invoice2.setInvoiceId(101);
    invoice2.setPatronId(patronId);
    invoice2.setAmount(5000);

    List<Invoice> mockInvoices = List.of(invoice1, invoice2);

    when(invoiceRepository.findByPatronId(patronId)).thenReturn(mockInvoices);

    List<Invoice> invoices = invoiceService.getInvoicesByPatronId(patronId);

    assertNotNull(invoices);
    assertEquals(2, invoices.size());
    assertEquals(patronId, invoices.get(0).getPatronId());
    assertEquals(1500, invoices.get(0).getAmount());
    assertEquals(5000, invoices.get(1).getAmount());
    assertEquals(101, invoices.get(1).getInvoiceId());

    verify(invoiceRepository, times(1)).findByPatronId(patronId);
  }

  @Test
  public void testGetInvoiceById_InvoiceNotFound() {
    int invoiceId = 100;

    when(invoiceRepository.findByInvoiceId(invoiceId)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> invoiceService.getInvoiceById(invoiceId));

    assertEquals("Invoice not found", exception.getMessage());
  }

  @Test
  public void testGetInvoicesByPatronId_NoInvoicesFound() {
    int patronId = 1;

    when(invoiceRepository.findByPatronId(patronId)).thenReturn(List.of()); // No invoices

    List<Invoice> invoices = invoiceService.getInvoicesByPatronId(patronId);

    assertNotNull(invoices);
    assertTrue(invoices.isEmpty()); // No invoices should be returned
    verify(invoiceRepository, times(1)).findByPatronId(patronId);
  }
}
