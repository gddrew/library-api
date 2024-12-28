package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.randomlake.library.enums.*;
import com.randomlake.library.exception.GeneralException;
import com.randomlake.library.model.Loan;
import com.randomlake.library.model.Media;
import com.randomlake.library.repository.LoanRepository;
import com.randomlake.library.repository.MediaRepository;
import com.randomlake.library.util.SequenceGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class MediaServiceTest {

  @Mock private LoanRepository loanRepository;
  @Mock private MediaRepository mediaRepository;
  @Mock private MediaUpdateService mediaUpdateService;
  @Mock private SequenceGenerator sequenceGenerator;

  @InjectMocks private MediaService mediaService;

  private Media media;
  private String mediaTitle;
  private String mediaAuthor;
  private String mediaPublisher;
  private String mediaIsbnId;

  @BeforeEach
  public void setup() {
    mediaTitle = "Chesapeake";
    mediaAuthor = "James Michener";
    mediaPublisher = "Random House";
    mediaIsbnId = "9780812970432";

    media = new Media();
    media.setMediaId(1);
    media.setMediaTitle(mediaTitle);
    media.setAuthorName(mediaAuthor);
    media.setPublisherName(mediaPublisher);
    media.setIsbnId(mediaIsbnId);
    media.setStatus(MediaStatus.AVAILABLE);
    media.setCreated_date(LocalDateTime.now());
    media.setLastUpdateDate(LocalDateTime.now());
    media.setAcquisitionDate(LocalDate.now());
  }

  @Test
  public void testGetAllMedia() {
    when(mediaRepository.findAll()).thenReturn(List.of(media));

    List<Media> result = mediaService.getAllMedia();

    assertNotNull(result);

    verify(mediaRepository, times(1)).findAll();
  }

  @Test
  public void testGetAllMedia_NoMedia() {
    when(mediaRepository.findAll()).thenReturn(Collections.emptyList());

    List<Media> media = mediaService.getAllMedia();

    assertNotNull(media);
    assertTrue(media.isEmpty());

    verify(mediaRepository, times(1)).findAll();
  }

  @Test
  public void testGetMediaById_Found() {
    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.of(media));

    Media media = mediaService.getMediaById(1);

    assertNotNull(media);
    assertEquals(1, media.getMediaId());
    assertEquals(mediaTitle, media.getMediaTitle());

    verify(mediaRepository, times(1)).findByMediaId(1);
  }

  @Test
  public void testGetMediaById_NotFound() {
    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.getMediaById(1));

    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());
    assertEquals("No item with this ID found in collection", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(mediaRepository, times(1)).findByMediaId(1);
  }

  @Test
  public void testGetMediaByTitle_Found() {
    when(mediaRepository.findByMediaTitle(mediaTitle)).thenReturn(List.of(media));

    List<Media> media = mediaService.getMediaByMediaTitle(mediaTitle);

    assertNotNull(media);
    assertEquals(1, media.size());
    assertEquals(mediaTitle, media.get(0).getMediaTitle());

    verify(mediaRepository, times(1)).findByMediaTitle(mediaTitle);
  }

  @Test
  public void testGetMediaByTitle_NotFound() {
    when(mediaRepository.findByMediaTitle(mediaTitle)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.getMediaByMediaTitle(mediaTitle));

    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());
    assertEquals("No item with this title found in collection", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(mediaRepository, times(1)).findByMediaTitle(mediaTitle);
  }

  @Test
  public void testGetMediaByAuthorName_Found() {
    when(mediaRepository.findByAuthorName(mediaAuthor)).thenReturn(List.of(media));

    List<Media> media = mediaService.getMediaByAuthorName(mediaAuthor);

    assertNotNull(media);
    assertEquals(1, media.size());
    assertEquals(mediaAuthor, media.get(0).getAuthorName());

    verify(mediaRepository, times(1)).findByAuthorName(mediaAuthor);
  }

  @Test
  public void testGetMediaByAuthorName_NotFound() {
    when(mediaRepository.findByAuthorName(mediaAuthor)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.getMediaByAuthorName(mediaAuthor));

    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());
    assertEquals("No item by this author found in collection", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(mediaRepository, times(1)).findByAuthorName(mediaAuthor);
  }

  @Test
  public void testGetMediaByPublisherName_Found() {
    when(mediaRepository.findByPublisherName(mediaPublisher)).thenReturn(List.of(media));

    List<Media> media = mediaService.getMediaByPublisherName(mediaPublisher);

    assertNotNull(media);
    assertEquals(1, media.size());
    assertEquals(mediaPublisher, media.get(0).getPublisherName());

    verify(mediaRepository, times(1)).findByPublisherName(mediaPublisher);
  }

  @Test
  public void testGetMediaByPublisherName_NotFound() {
    when(mediaRepository.findByPublisherName(mediaPublisher)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(
            GeneralException.class, () -> mediaService.getMediaByPublisherName(mediaPublisher));

    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());
    assertEquals("No item by this publisher found in collection", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(mediaRepository, times(1)).findByPublisherName(mediaPublisher);
  }

  @Test
  public void testGetMediaByIsbnId_Found() {
    when(mediaRepository.findByIsbnId(mediaIsbnId)).thenReturn(List.of(media));

    List<Media> media = mediaService.getMediaByIsbnId(mediaIsbnId);

    assertNotNull(media);
    assertEquals(1, media.size());
    assertEquals(mediaIsbnId, media.get(0).getIsbnId());

    verify(mediaRepository, times(1)).findByIsbnId(mediaIsbnId);
  }

  @Test
  public void testGetMediaByIsnbId_NotFound() {
    when(mediaRepository.findByIsbnId(mediaIsbnId)).thenReturn(Collections.emptyList());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.getMediaByIsbnId(mediaIsbnId));

    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());
    assertEquals("No item by this ISBN ID found in collection", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(mediaRepository, times(1)).findByIsbnId(mediaIsbnId);
  }

  @Test
  public void testUpdateMediaPartial_Success() {
    Map<String, Object> updates = new HashMap<>();
    updates.put("mediaTitle", "Updated Title");
    updates.put("numberPages", 350);

    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.of(media));
    doAnswer(
            invocation -> {
              Media mediaToUpdate = invocation.getArgument(0);
              Map<String, Object> updatesMap = invocation.getArgument(1);
              if (updatesMap.containsKey("mediaTitle")) {
                mediaToUpdate.setMediaTitle((String) updatesMap.get("mediaTitle"));
              }
              if (updatesMap.containsKey("numberPages")) {
                mediaToUpdate.setNumberPages((Integer) updatesMap.get("numberPages"));
              }
              return null;
            })
        .when(mediaUpdateService)
        .applyPartialUpdates(any(Media.class), anyMap());
    when(mediaRepository.save(media)).thenReturn(media);

    Media media = mediaService.updateMediaPartial(1, updates);

    assertNotNull(media);
    assertEquals("Updated Title", media.getMediaTitle());
    assertEquals(350, media.getNumberPages());

    verify(mediaRepository, times(1)).findByMediaId(1);
    verify(mediaUpdateService, times(1)).applyPartialUpdates(media, updates);
    verify(mediaRepository, times(1)).save(media);
  }

  @Test
  public void testUpdateMediaPartial_NoUpdates() {
    Map<String, Object> updates = new HashMap<>();

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.updateMediaPartial(1, updates));

    assertEquals(ExceptionType.INVALID_OPERATION, exception.getType());
    assertEquals("No updates provided", exception.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

    verify(mediaRepository, times(0)).findByMediaId(1);
    verify(mediaUpdateService, times(0)).applyPartialUpdates(media, updates);
    verify(mediaRepository, times(0)).save(media);
  }

  @Test
  public void testUpdatePartialMedia_MediaNotFound() {
    Map<String, Object> updates = Map.of("mediaTitle", "Updated Title");
    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.updateMediaPartial(1, updates));

    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());
    assertEquals("No item with this ID found in collection", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(mediaRepository, times(1)).findByMediaId(1);
    verify(mediaUpdateService, times(0)).applyPartialUpdates(media, updates);
    verify(mediaRepository, times(0)).save(media);
  }

  @Test
  public void testUpdateMediaFull_Success() {
    Media fullUpdate = new Media();
    fullUpdate.setMediaTitle("Full Updated Title");
    fullUpdate.setAuthorName("New Author");
    fullUpdate.setIsbnId("1234567890123");
    fullUpdate.setMediaType("Book");
    fullUpdate.setMediaFormat("Hardcover");
    fullUpdate.setNumberPages(400);
    fullUpdate.setClassificationCategory("Fiction");
    fullUpdate.setClassificationSubCategory("Historical");
    fullUpdate.setPublisherName("New Publisher");
    fullUpdate.setDisposalDisposition("Donate");
    fullUpdate.setAcquisitionDate(LocalDate.of(2023, 1, 1));

    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.of(media));
    doAnswer(
            invocation -> {
              Media target = invocation.getArgument(0);
              Media source = invocation.getArgument(1);
              target.setMediaTitle(source.getMediaTitle());
              target.setAuthorName(source.getAuthorName());
              target.setIsbnId(source.getIsbnId());
              target.setMediaType(source.getMediaType());
              target.setMediaFormat(source.getMediaFormat());
              target.setNumberPages(source.getNumberPages());
              target.setClassificationCategory(source.getClassificationCategory());
              target.setClassificationSubCategory(source.getClassificationSubCategory());
              target.setPublisherName(source.getPublisherName());
              target.setDisposalDisposition(source.getDisposalDisposition());
              target.setAcquisitionDate(source.getAcquisitionDate());
              return null;
            })
        .when(mediaUpdateService)
        .applyFullUpdate(any(Media.class), any(Media.class));
    when(mediaRepository.save(media)).thenReturn(media);

    Media media = mediaService.updateMediaFull(1, fullUpdate);

    assertNotNull(media);
    assertEquals("Full Updated Title", media.getMediaTitle());
    assertEquals("New Author", media.getAuthorName());
    assertEquals("1234567890123", media.getIsbnId());
    assertEquals("Book", media.getMediaType());
    assertEquals("Hardcover", media.getMediaFormat());
    assertEquals(400, media.getNumberPages());
    assertEquals("Fiction", media.getClassificationCategory());
    assertEquals("Historical", media.getClassificationSubCategory());
    assertEquals("New Publisher", media.getPublisherName());
    assertEquals("Donate", media.getDisposalDisposition());
    assertEquals(LocalDate.of(2023, 1, 1), media.getAcquisitionDate());

    verify(mediaRepository, times(1)).findByMediaId(1);
    verify(mediaUpdateService, times(1)).applyFullUpdate(media, fullUpdate);
    verify(mediaRepository, times(1)).save(media);
  }

  @Test
  public void testUpdateMediaFull_NullUpdate() {
    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.updateMediaFull(1, null));

    assertEquals(ExceptionType.INVALID_OPERATION, exception.getType());
    assertEquals("No updates provided", exception.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

    verify(mediaRepository, times(0)).findByMediaId(anyInt());
    verify(mediaUpdateService, times(0)).applyFullUpdate(any(), any());
    verify(mediaRepository, times(0)).save(any());
  }

  @Test
  public void testUpdateMediaFull_MediaNotFound() {
    Media fullUpdate = new Media();
    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.updateMediaFull(1, fullUpdate));

    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());
    assertEquals("No item with this ID found in collection", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(mediaRepository, times(1)).findByMediaId(1);
    verify(mediaUpdateService, times(0)).applyFullUpdate(any(), any());
    verify(mediaRepository, times(0)).save(any());
  }

  @Test
  public void testDeleteMedia_Success() {
    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.of(media));

    mediaService.deleteMedia(1);

    verify(mediaRepository, times(1)).findByMediaId(1);
    verify(mediaRepository, times(1)).delete(media);
  }

  @Test
  public void testDeleteMedia_NotFound() {
    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.empty());

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.deleteMedia(1));

    assertEquals(ExceptionType.MEDIA_NOT_FOUND, exception.getType());
    assertEquals("No item with this ID found in collection", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(mediaRepository, times(1)).findByMediaId(1);
    verify(loanRepository, times(0)).findByMediaId(1);
    verify(mediaRepository, times(0)).delete(any());
  }

  @Test
  public void testDeleteMediaWithNoActiveLoans_Success() {
    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.of(media));
    when(loanRepository.findByMediaId(1)).thenReturn(Collections.emptyList());

    mediaService.deleteMedia(1);

    verify(mediaRepository, times(1)).findByMediaId(1);
    verify(loanRepository, times(1)).findByMediaId(1);
    verify(mediaRepository, times(1)).delete(media);
  }

  @Test
  public void testDeleteMedia_ActiveLoansExist() {
    Loan activeLoan = new Loan();
    activeLoan.setLoanId(100);
    activeLoan.setPatronId(200);
    activeLoan.setStatus(LoanStatus.ACTIVE);

    Loan.LoanItem loanItem = new Loan.LoanItem();
    loanItem.setMediaId(1);
    loanItem.setStatus(ItemStatus.CHECKED_OUT);
    activeLoan.setItems(List.of(loanItem));

    when(mediaRepository.findByMediaId(1)).thenReturn(Optional.of(media));
    when(loanRepository.findByMediaId(1)).thenReturn(List.of(activeLoan));

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.deleteMedia(1));

    assertEquals(ExceptionType.INVALID_OPERATION, exception.getType());
    assertEquals("Cannot delete media with active loans or holds", exception.getMessage());
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());

    verify(mediaRepository, times(1)).findByMediaId(1);
    verify(loanRepository, times(1)).findByMediaId(1);
    verify(mediaRepository, times(0)).delete(any());
  }

  @Test
  public void testAddNewMedia_Success() {
    Media media = new Media();
    media.setMediaTitle("Our New Book");
    media.setAuthorName("New Author");
    media.setPublisherName("New Publisher");
    media.setIsbnId("1234567890123");

    when(sequenceGenerator.getNextSequenceValue(BarcodeType.MEDIA)).thenReturn(101);
    when(sequenceGenerator.generateBarcode(BarcodeType.MEDIA, 101)).thenReturn("M101");
    when(mediaRepository.save(any(Media.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Media result = mediaService.addNewMedia(media);

    assertNotNull(result);
    assertEquals(101, result.getMediaId());
    assertEquals("M101", result.getBarCodeId());
    assertEquals(MediaStatus.AVAILABLE, result.getStatus());
    assertNotNull(result.getCreated_date());
    assertNotNull(result.getAcquisitionDate());
    assertNotNull(result.getLastUpdateDate());

    verify(sequenceGenerator, times(1)).getNextSequenceValue(BarcodeType.MEDIA);
    verify(sequenceGenerator, times(1)).generateBarcode(BarcodeType.MEDIA, 101);
    verify(mediaRepository, times(1)).save(any(Media.class));
  }

  @Test
  public void testAddNewMedia_NullMedia() {
    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.addNewMedia(null));

    assertEquals(ExceptionType.INVALID_INPUT, exception.getType());
    assertEquals("Media object cannot be null", exception.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

    verify(sequenceGenerator, times(0)).getNextSequenceValue(BarcodeType.MEDIA);
    verify(sequenceGenerator, times(0)).generateBarcode(any(), anyInt());
    verify(mediaRepository, times(0)).save(any());
  }

  @Test
  public void testAddNewMedia_EmptyMediaTitle() {
    Media invalidMedia = new Media();
    invalidMedia.setMediaTitle("");

    GeneralException exception =
        assertThrows(GeneralException.class, () -> mediaService.addNewMedia(invalidMedia));

    assertEquals(ExceptionType.INVALID_INPUT, exception.getType());
    assertEquals("Media title cannot be empty", exception.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

    verify(sequenceGenerator, times(0)).getNextSequenceValue(BarcodeType.MEDIA);
    verify(sequenceGenerator, times(0)).generateBarcode(any(), anyInt());
    verify(mediaRepository, times(0)).save(any());
  }

  @Test
  public void testAddNewMedia_AcquisitionDateNotProvided() {
    Media media = new Media();
    media.setMediaTitle("Our New Book");
    media.setAuthorName("New Author");
    media.setPublisherName("New Publisher");
    media.setIsbnId("1234567890123");
    media.setAcquisitionDate(null);

    when(sequenceGenerator.getNextSequenceValue(BarcodeType.MEDIA)).thenReturn(101);
    when(sequenceGenerator.generateBarcode(BarcodeType.MEDIA, 101)).thenReturn("M101");
    when(mediaRepository.save(any(Media.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Media result = mediaService.addNewMedia(media);

    assertNotNull(result);
    assertEquals(101, result.getMediaId());
    assertEquals("M101", result.getBarCodeId());
    assertEquals(MediaStatus.AVAILABLE, result.getStatus());
    assertEquals(LocalDate.now(), result.getAcquisitionDate());

    verify(sequenceGenerator, times(1)).getNextSequenceValue(BarcodeType.MEDIA);
    verify(sequenceGenerator, times(1)).generateBarcode(BarcodeType.MEDIA, 101);
    verify(mediaRepository, times(1)).save(any(Media.class));
  }

  @Test
  public void testUpdateMediaStatus_Success() {
    MediaStatus newStatus = MediaStatus.OTHER;

    when(mediaRepository.save(media)).thenReturn(media);

    mediaService.updateMediaStatus(media, newStatus);

    assertEquals(newStatus, media.getStatus());
    assertEquals(LocalDate.now(), media.getLastUpdateDate().toLocalDate());

    verify(mediaRepository, times(1)).save(media);
  }
}
