package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.UserVideoMapper;
import io.github.szatms.videolibrary.model.statsmodel.Stats;
import io.github.szatms.videolibrary.model.usermodel.UserRepository;
import io.github.szatms.videolibrary.model.uservideomodel.SortDirection;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoSortBy;
import io.github.szatms.videolibrary.model.videomodel.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserVideoServiceTest {

    @Mock
    private UserVideoRepository userVideoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserVideoMapper userVideoMapper;

    @Mock
    private VideoService videoService;

    @InjectMocks
    private UserVideoService userVideoService;

    @Test
    void getVideosDefaultsToAddedAtDescending() {
        when(userRepository.existsById("user-1")).thenReturn(true);
        Sort expectedSort = Sort.by(
                new Sort.Order(Sort.Direction.ASC, "watched"),
                new Sort.Order(Sort.Direction.DESC, "addedAt")
        );
        when(userVideoRepository.findAllByUserId("user-1", expectedSort))
                .thenReturn(List.of());

        userVideoService.getVideos("user-1", null, null, true);

        verify(userVideoRepository).findAllByUserId("user-1", expectedSort);
    }

    @Test
    void getVideosSortsByWatchedWithAddedAtTieBreaker() {
        Sort expectedSort = Sort.by(
                new Sort.Order(Sort.Direction.ASC, "watched"),
                new Sort.Order(Sort.Direction.DESC, "addedAt")
        );

        when(userRepository.existsById("user-1")).thenReturn(true);
        when(userVideoRepository.findAllByUserId("user-1", expectedSort)).thenReturn(List.of());

        userVideoService.getVideos("user-1", UserVideoSortBy.WATCHED, SortDirection.ASC, true);

        verify(userVideoRepository).findAllByUserId("user-1", expectedSort);
    }

    @Test
    void getVideosSortsByViewsInMemoryAndPrioritizesUnwatched() {
        Instant oldest = Instant.parse("2024-01-01T00:00:00Z");
        Instant newest = Instant.parse("2024-01-03T00:00:00Z");
        Instant middle = Instant.parse("2024-01-02T00:00:00Z");

        UserVideo lowViews = userVideo("uv-1", "video-1", oldest);
        UserVideo highViewsOlder = userVideo("uv-2", "video-2", middle);
        UserVideo highViewsNewer = userVideo("uv-3", "video-3", newest);
        highViewsNewer.setWatched(true);

        when(userRepository.existsById("user-1")).thenReturn(true);
        when(userVideoRepository.findAllByUserId("user-1", Sort.by(Sort.Direction.DESC, "addedAt")))
                .thenReturn(List.of(highViewsNewer, highViewsOlder, lowViews));
        when(videoService.getByIds(List.of("video-3", "video-2", "video-1"))).thenReturn(List.of(
                video("video-1", 10),
                video("video-2", 100),
                video("video-3", 100)
        ));

        List<UserVideo> result = userVideoService.getVideos("user-1", UserVideoSortBy.VIEWS, SortDirection.DESC, true);

        assertEquals(List.of("uv-2", "uv-1", "uv-3"), result.stream().map(UserVideo::getId).toList());
    }

    @Test
    void getVideosCanSkipUnwatchedPriority() {
        when(userRepository.existsById("user-1")).thenReturn(true);
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "addedAt");
        when(userVideoRepository.findAllByUserId("user-1", expectedSort)).thenReturn(List.of());

        userVideoService.getVideos("user-1", UserVideoSortBy.ADDED_AT, SortDirection.DESC, false);

        verify(userVideoRepository).findAllByUserId("user-1", expectedSort);
    }

    @Test
    void deleteVideoDeletesSharedRecordWhenOnlyOneAssignmentExists() {
        UserVideo userVideo = UserVideo.builder()
                .id("uv-1")
                .userId("user-1")
                .videoId("video-1")
                .addedAt(Instant.now())
                .build();

        when(userVideoRepository.findById("uv-1")).thenReturn(Optional.of(userVideo));
        when(userVideoRepository.countByVideoId("video-1")).thenReturn(1L);

        userVideoService.deleteVideo("uv-1");

        verify(userVideoRepository).deleteById("uv-1");
        verify(videoService).deleteById("video-1");
    }

    @Test
    void deleteVideoKeepsSharedVideoWhenMultipleAssignmentsExist() {
        UserVideo userVideo = UserVideo.builder()
                .id("uv-1")
                .userId("user-1")
                .videoId("video-1")
                .addedAt(Instant.now())
                .build();

        when(userVideoRepository.findById("uv-1")).thenReturn(Optional.of(userVideo));
        when(userVideoRepository.countByVideoId("video-1")).thenReturn(2L);

        userVideoService.deleteVideo("uv-1");

        verify(userVideoRepository).deleteById("uv-1");
        verify(videoService, never()).deleteById("video-1");
    }

    private UserVideo userVideo(String id, String videoId, Instant addedAt) {
        return UserVideo.builder()
                .id(id)
                .userId("user-1")
                .videoId(videoId)
                .addedAt(addedAt)
                .build();
    }

    private Video video(String videoId, long viewCount) {
        return Video.builder()
                .videoId(videoId)
                .stats(Stats.builder().viewCount(viewCount).build())
                .build();
    }
}
