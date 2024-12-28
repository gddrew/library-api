package com.randomlake.library.dto;

import com.randomlake.library.enums.MediaStatus;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaRequest {

  private String mediaTitle;
  private String authorName;
  private String isbnId;
  private String barCodeId;
  private String publicationYear;
  private String mediaType;
  private String mediaFormat;
  private Integer numberPages;
  private String classificationCategory;
  private String classificationSubCategory;
  private String publisherName;
  private String disposalDisposition;
  private LocalDate acquisitionDate;
  private MediaStatus status;
  private boolean isSensitive;
}
