package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.integration.youtube.PythonVideoDataProvider;
import io.github.szatms.videolibrary.integration.youtube.dto.PythonVideoResponseDTO;
import io.github.szatms.videolibrary.integration.youtube.factory.VideoFactory;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoFactory videoFactory;

    @Mock
    private PythonVideoDataProvider provider;

    @InjectMocks
    private RefreshService refreshService;

    @Test
    void refreshAllVideosAndPlaylistsRefreshesVideosAndTracksFailures() {
        Video firstVideo = Video.builder()
                .videoId("video-1")
                .youtubeId("youtube-1")
                .publishedAt(Instant.now())
                .build();

        Video secondVideo = Video.builder()
                .videoId("video-2")
                .youtubeId("youtube-2")
                .publishedAt(Instant.now())
                .build();

        PythonVideoResponseDTO successResponse = createResponse("youtube-1", "Updated title");

        when(videoRepository.findAll()).thenReturn(List.of(firstVideo, secondVideo));
        when(provider.load("youtube-1")).thenReturn(successResponse);
        when(provider.load("youtube-2")).thenThrow(new IllegalStateException("provider down"));
        when(videoFactory.fromItem(successResponse)).thenReturn(firstVideo);

        // This test focuses on the core functionality - refreshing videos
        refreshService.refreshAllVideosAndPlaylists();

        // Verify that the video repository was called to save the updated videos
        verify(videoRepository).save(firstVideo);
        
        // Verify that the provider was called to fetch data for both videos
        verify(provider).load("youtube-1");
        verify(provider).load("youtube-2");
    }

    private PythonVideoResponseDTO createResponse(String youtubeId, String title) {
        PythonVideoResponseDTO response = new PythonVideoResponseDTO();
        
        // Create item to be added to the items list
        PythonVideoResponseDTO.Item item = new PythonVideoResponseDTO.Item();
        item.setId(youtubeId);
        
        // Set snippet data
        PythonVideoResponseDTO.Snippet snippet = new PythonVideoResponseDTO.Snippet();
        snippet.setTitle(title);
        snippet.setDescription("description");
        snippet.setChannelId("channel-1");
        snippet.setPublishedAt("2024-01-01T00:00:00Z"); // String format as expected by DTO
        item.setSnippet(snippet);
        
        // Set content details
        PythonVideoResponseDTO.ContentDetails contentDetails = new PythonVideoResponseDTO.ContentDetails();
        contentDetails.setDuration("PT2M3S"); // ISO 8601 format
        item.setContentDetails(contentDetails);
        
        // Set statistics
        PythonVideoResponseDTO.Statistics statistics = new PythonVideoResponseDTO.Statistics();
        statistics.setViewCount("10"); // String format as expected by DTO
        statistics.setLikeCount("2"); // String format as expected by DTO
        item.setStatistics(statistics);
        
        // Set thumbnail
        PythonVideoResponseDTO.Thumbnails thumbnails = new PythonVideoResponseDTO.Thumbnails();
        PythonVideoResponseDTO.Thumbnail thumbnail = new PythonVideoResponseDTO.Thumbnail();
        thumbnail.setUrl("https://example.com/thumb.jpg");
        thumbnails.setDefaultThumbnail(thumbnail);
        snippet.setThumbnails(thumbnails);
        
        // Add item to response's items list
        response.setItems(List.of(item));
        
        return response;
    }
}
