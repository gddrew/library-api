package com.randomlake.library.model;

import com.randomlake.library.enums.CardStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "cards")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card extends BaseModel {

  @Id private ObjectId id;

  private int cardId;

  @CreatedDate private LocalDateTime createdDate;

  @Field("bar_code")
  private String barCodeId;

  @Field("patron_id")
  private int patronId;

  @Field("card_status")
  private CardStatus status;

  @Field("last_update_date")
  private LocalDateTime lastUpdateDate;

  @Field("last_used_date")
  private LocalDateTime lastUsedDate;

  public String getId() {
    return id != null ? id.toHexString() : null;
  }

  public void setId(String id) {
    this.id = id != null ? new ObjectId(id) : null;
  }
}
