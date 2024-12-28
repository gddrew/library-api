package com.randomlake.library.model;

import com.randomlake.library.enums.MediaStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "media")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Media extends BaseModel {

  @Id private ObjectId id;

  private int mediaId;

  @CreatedDate private LocalDateTime created_date;

  @Field("media_title")
  private String mediaTitle;

  @Field("author_name")
  private String authorName;

  @Field("isbn_id")
  private String isbnId;

  @Field("bar_code")
  private String barCodeId;

  @Field("publication_year")
  private String publicationYear;

  @Field("media_type")
  private String mediaType;

  @Field("media_format")
  private String mediaFormat;

  @Field("number_pages")
  private Integer numberPages = 0;

  @Field("classification_category")
  private String classificationCategory;

  @Field("classification_subcategory")
  private String classificationSubCategory;

  @Field("publisher_name")
  private String publisherName;

  @Field("disposal_disposition")
  private String disposalDisposition;

  @Field("acquisition_date")
  private LocalDate acquisitionDate;

  @Field("last_update_date")
  private LocalDateTime lastUpdateDate;

  @Field("media_status")
  private MediaStatus status;

  @Field("is_sensitive")
  private boolean isSensitive;

  public String getId() {
    return id != null ? id.toHexString() : null;
  }

  public void setId(String id) {
    this.id = id != null ? new ObjectId(id) : null;
  }
}
