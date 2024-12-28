package com.randomlake.library.controller;

import static com.randomlake.library.enums.MediaStatus.AVAILABLE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.randomlake.library.config.TestSecurityConfig;
import com.randomlake.library.enums.MediaStatus;
import com.randomlake.library.model.Media;
import com.randomlake.library.service.MediaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = {TestSecurityConfig.class})
@WebMvcTest(MediaController.class)
public class MediaControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private MediaService mediaService;

  // Declare reusable media instances
  private Media media1;
  private Media media2;

  @BeforeEach
  public void setup() {

    media1 =
        new Media(
            new ObjectId(),
            1,
            LocalDateTime.now(),
            "Title1",
            "Author1",
            "1234567890123",
            "BarCode1",
            "2024",
            "Book",
            "Hardcover",
            300,
            "Science",
            "Physics",
            "Publisher1",
            "Sell",
            LocalDate.now(),
            LocalDateTime.now(),
            AVAILABLE,
            false);
    media2 =
        new Media(
            new ObjectId(),
            2,
            LocalDateTime.now(),
            "Title2",
            "Author2",
            "9876543210987",
            "BarCode2",
            "2023",
            "Magazine",
            "Digital",
            150,
            "Literature",
            "Fiction",
            "Publisher2",
            "Sell",
            LocalDate.now(),
            LocalDateTime.now(),
            MediaStatus.CHECKED_OUT,
            true);
  }

  @Test
  public void testGetAllMedia_Success() throws Exception {

    List<Media> mediaList = Arrays.asList(media1, media2);
    when(mediaService.getAllMedia()).thenReturn(mediaList);

    mockMvc
        .perform(get("/api/collection/media"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].mediaTitle").value("Title1"))
        .andExpect(jsonPath("$[0].authorName").value("Author1"))
        .andExpect(jsonPath("$[1].mediaTitle").value("Title2"))
        .andExpect(jsonPath("$[1].authorName").value("Author2"));
  }

  @Test
  public void testGetMediaById_Success() throws Exception {

    when(mediaService.getMediaById(1)).thenReturn(media1);

    mockMvc
        .perform(get("/api/collection/media/{mediaId}", 1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mediaTitle", is("Title1")))
        .andExpect(jsonPath("$.authorName", is("Author1")))
        .andExpect(jsonPath("$.mediaId", is(1)))
        .andExpect(jsonPath("$.isbnId", is("1234567890123")))
        .andExpect(jsonPath("$.barCodeId", is("BarCode1")))
        .andExpect(jsonPath("$.publicationYear", is("2024")))
        .andExpect(jsonPath("$.mediaType", is("Book")))
        .andExpect(jsonPath("$.mediaFormat", is("Hardcover")))
        .andExpect(jsonPath("$.numberPages", is(300)))
        .andExpect(jsonPath("$.classificationCategory", is("Science")))
        .andExpect(jsonPath("$.classificationSubCategory", is("Physics")))
        .andExpect(jsonPath("$.publisherName", is("Publisher1")))
        .andExpect(jsonPath("$.disposalDisposition", is("Sell")))
        .andExpect(jsonPath("$.status", is("AVAILABLE")));
  }

  @Test
  public void testGetMediaById_NotFound() throws Exception {
    when(mediaService.getMediaById(1)).thenReturn(null);

    mockMvc.perform(get("/api/collection/media/{mediaId}", 1)).andExpect(status().isNotFound());
  }

  @Test
  public void testGetMediaByAuthor_Success() throws Exception {
    List<Media> mediaList = Collections.singletonList(media2);

    when(mediaService.getMediaByAuthorName("Author2")).thenReturn(mediaList);

    mockMvc
        .perform(get("/api/collection/media/author/{authorName}", "Author2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].mediaTitle", is("Title2")))
        .andExpect(jsonPath("$[0].authorName", is("Author2")));
  }

  @Test
  public void testGetMediaByAuthor_NotFound() throws Exception {
    when(mediaService.getMediaByAuthorName("Author2")).thenReturn(null);

    mockMvc
        .perform(get("/api/collection/media/author/{authorName}", "Author2"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetMediaByTitle_Success() throws Exception {

    List<Media> mediaList = Collections.singletonList(media1);

    when(mediaService.getMediaByMediaTitle("Title1")).thenReturn(mediaList);

    mockMvc
        .perform(get("/api/collection/media/title/{mediaTitle}", "Title1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].mediaTitle", is("Title1")));
  }

  @Test
  public void testGetMediaByTitle_NotFound() throws Exception {
    when(mediaService.getMediaByMediaTitle("Title1")).thenReturn(null);

    mockMvc
        .perform(get("/api/collection/media/title/{mediaTitle}", "Title1"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetMediaByPublisher_Success() throws Exception {

    List<Media> mediaList = Collections.singletonList(media1);

    when(mediaService.getMediaByPublisherName("Publisher1")).thenReturn(mediaList);

    mockMvc
        .perform(get("/api/collection/media/publisher/{publisherName}", "Publisher1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].publisherName", is("Publisher1")));
  }

  @Test
  public void testGetMediaByPublisher_NotFound() throws Exception {
    when(mediaService.getMediaByPublisherName("Publisher1")).thenReturn(null);

    mockMvc
        .perform(get("/api/collection/media/publisher/{publisherName}", "Publisher1"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetMediaByIsbnId_Success() throws Exception {

    List<Media> mediaList = Collections.singletonList(media1);

    when(mediaService.getMediaByIsbnId("1234567890123")).thenReturn(mediaList);

    mockMvc
        .perform(get("/api/collection/media/isbn/{isbnId}", "1234567890123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].isbnId", is("1234567890123")));
  }

  @Test
  public void testGetMediaByIsbnId_NotFound() throws Exception {
    when(mediaService.getMediaByIsbnId("1234567890123")).thenReturn(null);

    mockMvc
        .perform(get("/api/collection/media/isbn/{isbnId}", "1234567890123"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAddMedia_Success() throws Exception {

    when(mediaService.addNewMedia(any(Media.class))).thenReturn(media1);

    mockMvc
        .perform(
            post("/api/collection/media")
                .contentType("application/json")
                .content("{ \"mediaTitle\": \"Title1\", \"authorName\": \"Author1\"}"))
        .andExpect(status().isCreated()) // Expect HTTP 201 Created
        .andExpect(jsonPath("$.mediaTitle", is("Title1"))) // Expect title "Title1"
        .andExpect(jsonPath("$.authorName", is("Author1"))) // Expect author "Author1"
        .andExpect(jsonPath("$.mediaId", is(1))); // Expect mediaId 1
  }

  @Test
  public void testUpdateMedia_Success() throws Exception {

    Media updatedMedia =
        new Media(
            new ObjectId(media2.getId()),
            media2.getMediaId(),
            media2.getCreated_date(),
            "Updated Title",
            "Updated Author",
            media2.getIsbnId(),
            media2.getBarCodeId(),
            media2.getPublicationYear(),
            media2.getMediaType(),
            media2.getMediaFormat(),
            media2.getNumberPages(),
            media2.getClassificationCategory(),
            media2.getClassificationSubCategory(),
            media2.getPublisherName(),
            media2.getDisposalDisposition(),
            media2.getAcquisitionDate(),
            media2.getLastUpdateDate(),
            media2.getStatus(),
            media2.isSensitive());

    when(mediaService.updateMediaFull(eq(2), any(Media.class))).thenReturn(updatedMedia);

    mockMvc
        .perform(
            put("/api/collection/media/{mediaId}", 2)
                .contentType("application/json")
                .content(
                    "{ \"mediaTitle\": \"Updated Title\", \"authorName\": \"Updated Author\" }"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mediaTitle", is("Updated Title")))
        .andExpect(jsonPath("$.authorName", is("Updated Author")));
  }

  @Test
  public void testPatchUpdateMedia_Success() throws Exception {

    // Define the partial updates as a map
    Map<String, Object> updates = Map.of("publisherName", "Random Lake Communications");

    // Create a mock Media object with the updated fields
    Media patchUpdatedMedia =
        new Media(
            new ObjectId(media2.getId()),
            media2.getMediaId(),
            media2.getCreated_date(),
            media2.getMediaTitle(),
            media2.getAuthorName(),
            media2.getIsbnId(),
            media2.getBarCodeId(),
            media2.getPublicationYear(),
            media2.getMediaType(),
            media2.getMediaFormat(),
            media2.getNumberPages(),
            media2.getClassificationCategory(),
            media2.getClassificationSubCategory(),
            "Random Lake Communications",
            media2.getDisposalDisposition(),
            media2.getAcquisitionDate(),
            media2.getLastUpdateDate(),
            media2.getStatus(),
            media2.isSensitive());

    // Mock the MediaService to return the patched media object
    when(mediaService.updateMediaPartial(eq(2), anyMap())).thenReturn(patchUpdatedMedia);

    // Perform the PATCH request and assert the response
    mockMvc
        .perform(
            patch("/api/collection/media/{mediaId}", 2)
                .contentType("application/json")
                .content("{ \"publisherName\": \"Random Lake Communications\" }"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publisherName", is("Random Lake Communications")));
  }

  @Test
  public void testDeleteMedia_Success() throws Exception {

    // No need to mock the response as deleteMedia doesn't return any value
    doNothing().when(mediaService).deleteMedia(1);

    // Perform the DELETE request and assert the response
    mockMvc
        .perform(delete("/api/collection/media/{mediaId}", 1))
        .andExpect(status().isNoContent()); // Expect HTTP 204 No Content

    // Verify that the service's deleteMedia method was called once with the correct mediaId
    verify(mediaService, times(1)).deleteMedia(1);
  }
}
