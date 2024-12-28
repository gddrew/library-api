package com.randomlake.library.controller;

import static com.randomlake.library.mapper.InvoiceMapper.toDto;

import com.randomlake.library.dto.InvoiceRequest;
import com.randomlake.library.dto.InvoiceResponse;
import com.randomlake.library.mapper.InvoiceMapper;
import com.randomlake.library.model.Invoice;
import com.randomlake.library.service.InvoiceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class InvoiceController {

  private final InvoiceService invoiceService;

  public InvoiceController(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

  @PostMapping("/invoice/create")
  public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest request) {
    Invoice createdInvoice =
        invoiceService.createInvoice(
            request.getPatronId(), request.getAmount(), request.getStatus(), request.getCampaign());
    InvoiceResponse response = toDto(createdInvoice);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/invoice/{invoiceId}")
  public ResponseEntity<InvoiceResponse> updateInvoice(
      @PathVariable int invoiceId, @Valid @RequestBody InvoiceRequest request) {
    Invoice updatedInvoice =
        invoiceService.updateInvoice(
            invoiceId,
            request.getPatronId(),
            request.getAmount(),
            request.getCampaign(),
            request.getStatus());
    InvoiceResponse response = toDto(updatedInvoice);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/invoice/patron/{patronId}")
  public ResponseEntity<List<InvoiceResponse>> getInvoicesByPatronId(
      @PathVariable int patronId,
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "10") int limit) {
    List<Invoice> invoices = invoiceService.getInvoicesByPatronId(patronId);
    List<InvoiceResponse> responses =
        invoices.stream().map(InvoiceMapper::toDto).collect(Collectors.toList());
    if (invoices.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responses);
    }
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/invoice/{invoiceId}")
  public ResponseEntity<InvoiceResponse> getInvoiceById(
      @PathVariable int invoiceId,
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "10") int limit) {
    Invoice invoice = invoiceService.getInvoiceById(invoiceId);
    InvoiceResponse response = toDto(invoice);
    return invoice != null
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
  }
}
