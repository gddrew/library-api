package com.randomlake.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportLoanPatronMedia {

  @Getter @Setter private int loanId;
  private int patronId;
  private String patronName;
  private String loanStatus;
  private List<LoanItem> items;

  @Getter
  @Setter
  public static class LoanItem {
    private int mediaId;
    private LocalDate checkoutDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;
    private MediaDetails mediaDetails;
  }

  @Getter
  @Setter
  public static class MediaDetails {

    @JsonProperty("media_title")
    private String mediaTitle;

    @JsonProperty("author_name")
    private String authorName;

    @JsonProperty("isbn_id")
    private String isbnId;

    @JsonProperty("media_type")
    private String mediaType;

    @JsonProperty("media_format")
    private String mediaFormat;

    @JsonProperty("classification_category")
    private String classificationCategory;

    @JsonProperty("classification_subcategory")
    private String classificationSubcategory;
  }

  @Getter
  @Setter
  public static class TransactionLog {
    private String transactionType;
    private LocalDate transactionDate;
    private List<Integer> mediaIds;
  }
}
