package com.randomlake.library.model;

import com.randomlake.library.enums.FineType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fines")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fine {

  @Id private ObjectId id;

  private int fineId;
  private int patronId;
  private int mediaId;
  private FineType fineType;
  private int amount;
  private LocalDateTime dateAssessed;
  private LocalDateTime datePaid;
  private boolean isPaid;
  private boolean isWaived;

  public String getId() {
    return id != null ? id.toHexString() : null;
  }

  public void setId(String id) {
    this.id = id != null ? new ObjectId(id) : null;
  }
}
