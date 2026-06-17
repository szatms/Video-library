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
class VideoRefreshServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoFactory videoFactory;

    @Mock
    private PythonVideoDataProvider provider;

    @InjectMocks
    private VideoRefreshService videoRefreshService;

    @Test
    void runJobRefreshesVideosAndTracksFailures() {
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

        when(videoRepository.count()).thenReturn(2L);
        when(videoRepository.findAll(PageRequest.of(0, 50, Sort.by("videoId"))))
                .thenReturn(new PageImpl<>(List.of(firstVideo, secondVideo), PageRequest.of(0, 50), 2));
        when(provider.load("youtube-1")).thenReturn(successResponse);
        when(provider.load("youtube-2")).thenThrow(new IllegalStateException("provider down"));

        VideoRefreshService.VideoRefreshJob job = new VideoRefreshService.VideoRefreshJob("job-1");
        videoRefreshService.runJob(job);

        ArgumentCaptor<Video> savedVideoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(savedVideoCaptor.capture());
        verify(videoFactory).updateEntityFromItem(firstVideo, successResponse);

        assertEquals(VideoRefreshService.VideoRefreshJobStatus.COMPLETED_WITH_ERRORS, job.getStatus());
        assertEquals(2, job.getProcessedVideos());
        assertEquals(1, job.getUpdatedVideos());
        assertEquals(1, job.getFailedVideos());
        assertEquals(2L, job.getTotalVideos());
        assertEquals("provider down", job.getErrorMessage());
        assertNull(job.getCurrentVideoId());
        assertEquals("video-1", savedVideoCaptor.getValue().getVideoId());
    }

    private PythonVideoResponseDTO createResponse(String youtubeId, String title) {
        PythonVideoResponseDTO response = new PythonVideoResponseDTO();
        response.setId(youtubeId);
        response.setTitle(title);
        response.setDescription("description");
        response.setChannelId("channel-1");
        response.setThumbnail("https://example.com/thumb.jpg");
        response.setDuration(123L);
        response.setViewCount(10L);
        response.setLikeCount(2L);
        response.setTimestamp(1704067200L);
        return response;
    }
}
