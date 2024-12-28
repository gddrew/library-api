package com.randomlake.library.repository;

import com.randomlake.library.model.Invoice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {

  Boolean existsByPatronId(int patronId);

  // Find invoices by invoice ID
  Optional<Invoice> findByInvoiceId(int invoiceId);

  @Query("{ 'patronId' : ?0 }")
  List<Invoice> findByPatronId(int patronId);
}
