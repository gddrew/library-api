package com.randomlake.library.controller;

import com.randomlake.library.dto.MediaRequest;
import com.randomlake.library.dto.MediaResponse;
import com.randomlake.library.mapper.MediaMapper;
import com.randomlake.library.model.Media;
import com.randomlake.library.service.MediaService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collection/media")
public class MediaController {

  @Autowired private MediaService mediaService;

  @GetMapping
  public ResponseEntity<List<MediaResponse>> getAllMedia() {
    List<Media> media = mediaService.getAllMedia();
    List<MediaResponse> mediaResponses = media.stream().map(MediaMapper::toDto).toList();
    return new ResponseEntity<>(mediaResponses, HttpStatus.OK);
  }

  @GetMapping("/{mediaId}")
  public ResponseEntity<MediaResponse> getMediaByID(@PathVariable int mediaId) {
    Media media = mediaService.getMediaById(mediaId);
    if (media == null) {
      return ResponseEntity.notFound().build();
    }
    MediaResponse mediaResponse = MediaMapper.toDto(media);
    return ResponseEntity.ok(mediaResponse);
  }

  @GetMapping("/author/{authorName}")
  public ResponseEntity<List<MediaResponse>> getMediaByAuthorName(@PathVariable String authorName) {
    List<Media> media = mediaService.getMediaByAuthorName(authorName);
    if (media == null) {
      return ResponseEntity.notFound().build();
    }
    List<MediaResponse> mediaResponses = media.stream().map(MediaMapper::toDto).toList();
    return ResponseEntity.ok(mediaResponses);
  }

  @GetMapping("/title/{mediaTitle}")
  public ResponseEntity<List<MediaResponse>> getMediaByMediaTitle(@PathVariable String mediaTitle) {
    List<Media> media = mediaService.getMediaByMediaTitle(mediaTitle);
    if (media == null) {
      return ResponseEntity.notFound().build();
    }
    List<MediaResponse> mediaResponses = media.stream().map(MediaMapper::toDto).toList();
    return ResponseEntity.ok(mediaResponses);
  }

  @GetMapping("/publisher/{publisherName}")
  public ResponseEntity<List<MediaResponse>> getMediaByPublisherName(
      @PathVariable String publisherName) {
    List<Media> media = mediaService.getMediaByPublisherName(publisherName);
    if (media == null) {
      return ResponseEntity.notFound().build();
    }
    List<MediaResponse> mediaResponses = media.stream().map(MediaMapper::toDto).toList();
    return ResponseEntity.ok(mediaResponses);
  }

  @GetMapping("/isbn/{isbnId}")
  public ResponseEntity<List<MediaResponse>> getMediaByIsbnId(@PathVariable String isbnId) {
    List<Media> media = mediaService.getMediaByIsbnId(isbnId);
    if (media == null) {
      return ResponseEntity.notFound().build();
    }
    List<MediaResponse> mediaResponses = media.stream().map(MediaMapper::toDto).toList();
    return ResponseEntity.ok(mediaResponses);
  }

  @PostMapping
  public ResponseEntity<MediaResponse> addMedia(@Valid @RequestBody MediaRequest mediaRequest) {
    Media media = MediaMapper.toEntity(mediaRequest);
    Media addedMedia = mediaService.addNewMedia(media);
    MediaResponse mediaResponse = MediaMapper.toDto(addedMedia);
    return new ResponseEntity<>(mediaResponse, HttpStatus.CREATED);
  }

  @PutMapping("/{mediaId}")
  public ResponseEntity<MediaResponse> updateMedia(
      @PathVariable("mediaId") int mediaId, @Valid @RequestBody MediaRequest mediaRequest) {
    Media mediaDetails = MediaMapper.toEntity(mediaRequest);
    Media updatedMedia = mediaService.updateMediaFull(mediaId, mediaDetails);
    MediaResponse mediaResponse = MediaMapper.toDto(updatedMedia);
    return ResponseEntity.ok(mediaResponse);
  }

  @PatchMapping("/{mediaId}")
  public ResponseEntity<MediaResponse> patchUpdateMedia(
      @PathVariable("mediaId") int mediaId, @RequestBody Map<String, Object> updates) {
    Media patchUpdatedMedia = mediaService.updateMediaPartial(mediaId, updates);
    MediaResponse mediaResponse = MediaMapper.toDto(patchUpdatedMedia);
    return ResponseEntity.ok(mediaResponse);
  }

  @DeleteMapping("/{mediaId}")
  public ResponseEntity<Void> deleteMedia(@PathVariable("mediaId") int mediaId) {
    mediaService.deleteMedia(mediaId);
    return ResponseEntity.noContent().build();
  }
}
