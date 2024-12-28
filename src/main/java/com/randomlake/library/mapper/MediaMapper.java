package com.randomlake.library.mapper;

import com.randomlake.library.dto.MediaRequest;
import com.randomlake.library.dto.MediaResponse;
import com.randomlake.library.model.Media;

public class MediaMapper {

  public static Media toEntity(MediaRequest mediaRequest) {
    if (mediaRequest == null) {
      return null;
    }
    Media media = new Media();
    media.setMediaTitle(mediaRequest.getMediaTitle());
    media.setAuthorName(mediaRequest.getAuthorName());
    media.setIsbnId(mediaRequest.getIsbnId());
    media.setBarCodeId(mediaRequest.getBarCodeId());
    media.setPublicationYear(mediaRequest.getPublicationYear());
    media.setMediaType(mediaRequest.getMediaType());
    media.setMediaFormat(mediaRequest.getMediaFormat());
    media.setNumberPages(mediaRequest.getNumberPages());
    media.setClassificationCategory(mediaRequest.getClassificationCategory());
    media.setClassificationSubCategory(mediaRequest.getClassificationSubCategory());
    media.setPublisherName(mediaRequest.getPublisherName());
    media.setDisposalDisposition(mediaRequest.getDisposalDisposition());
    media.setAcquisitionDate(mediaRequest.getAcquisitionDate());
    media.setStatus(mediaRequest.getStatus());
    media.setSensitive(mediaRequest.isSensitive());
    return media;
  }

  public static MediaResponse toDto(Media media) {
    if (media == null) {
      return null;
    }
    MediaResponse response = new MediaResponse();
    response.setMediaId(media.getMediaId());
    response.setCreated_date(media.getCreated_date());
    response.setMediaTitle(media.getMediaTitle());
    response.setAuthorName(media.getAuthorName());
    response.setIsbnId(media.getIsbnId());
    response.setBarCodeId(media.getBarCodeId());
    response.setPublicationYear(media.getPublicationYear());
    response.setMediaType(media.getMediaType());
    response.setMediaFormat(media.getMediaFormat());
    response.setNumberPages(media.getNumberPages());
    response.setClassificationCategory(media.getClassificationCategory());
    response.setClassificationSubCategory(media.getClassificationSubCategory());
    response.setPublisherName(media.getPublisherName());
    response.setDisposalDisposition(media.getDisposalDisposition());
    response.setAcquisitionDate(media.getAcquisitionDate());
    response.setStatus(media.getStatus());
    response.setSensitive(media.isSensitive());
    response.setLastUpdateDate(media.getLastUpdateDate());
    return response;
  }
}
