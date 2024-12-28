package com.randomlake.library.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "invoices")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {

  @Id private ObjectId id;

  private int invoiceId;
  private int patronId;
  private int amount;
  private String status;
  private LocalDate date;
  private String campaign;

  public String getId() {
    return id != null ? id.toHexString() : null;
  }

  public void setId(String id) {
    this.id = id != null ? new ObjectId(id) : null;
  }
}
