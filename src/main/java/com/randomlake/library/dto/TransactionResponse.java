package com.randomlake.library.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransactionResponse {

  private int loanId;
  private String message;
  private List<MediaItem> mediaItems;

  @Setter
  @Getter
  public static class MediaItem {

    private String mediaTitle;
    private String mediaStatus;
    private String formattedBarcodeId;
  }
}
