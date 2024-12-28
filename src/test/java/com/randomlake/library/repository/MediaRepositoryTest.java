package com.randomlake.library.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.randomlake.library.model.Media;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class MediaRepositoryTest {

  @Mock private MediaRepository mediaRepository;

  private Media media;
  private int mediaId;
  private String mediaTitle;
  private String authorName;
  private String publisherName;
  private String isbnId;

  @BeforeEach
  public void setup() {

    MockitoAnnotations.openMocks(this);

    mediaId = 1641272933;
    mediaTitle = "some book";
    authorName = "some author";
    publisherName = "Random Lake";
    isbnId = "12345";

    media = new Media();
    media.setMediaId(mediaId);
    media.setMediaTitle(mediaTitle);
    media.setAuthorName(authorName);
    media.setPublisherName(publisherName);
    media.setIsbnId(isbnId);
  }

  @Test
  public void findByMediaIdSuccess() {
    when(mediaRepository.findByMediaId(mediaId)).thenReturn(Optional.of(media));

    Optional<Media> foundMedia = mediaRepository.findByMediaId(mediaId);

    assertTrue(foundMedia.isPresent());
    assertEquals(media.getMediaTitle(), foundMedia.get().getMediaTitle());
  }

  @Test
  public void findByMediaTitleSuccess() {
    List<Media> medias = List.of(media);
    when(mediaRepository.findByMediaTitle(mediaTitle)).thenReturn(medias);

    List<Media> foundMedias = mediaRepository.findByMediaTitle(mediaTitle);

    assertEquals(1, foundMedias.size());
    assertEquals(mediaTitle, foundMedias.get(0).getMediaTitle());
  }

  @Test
  public void findByMediaAuthorSuccess() {
    List<Media> medias = List.of(media);
    when(mediaRepository.findByAuthorName(authorName)).thenReturn(medias);

    List<Media> foundMedias = mediaRepository.findByAuthorName(authorName);

    assertEquals(1, foundMedias.size());
    assertEquals(authorName, foundMedias.get(0).getAuthorName());
  }

  @Test
  public void findByPublisherNameSuccess() {
    List<Media> medias = List.of(media);
    when(mediaRepository.findByPublisherName(publisherName)).thenReturn(medias);

    List<Media> foundMedias = mediaRepository.findByPublisherName(publisherName);

    assertEquals(1, foundMedias.size());
    assertEquals(publisherName, foundMedias.get(0).getPublisherName());
  }

  @Test
  public void findByIsbnIdSuccess() {
    List<Media> medias = List.of(media);
    when(mediaRepository.findByIsbnId(isbnId)).thenReturn(medias);

    List<Media> foundMedias = mediaRepository.findByIsbnId(isbnId);

    assertEquals(1, foundMedias.size());
    assertEquals(isbnId, foundMedias.get(0).getIsbnId());
  }

  @Test
  public void deleteByIdSuccess() {
    doNothing().when(mediaRepository).deleteById(mediaId);

    mediaRepository.deleteById(mediaId);

    verify(mediaRepository, times(1)).deleteById(mediaId);
  }
}
