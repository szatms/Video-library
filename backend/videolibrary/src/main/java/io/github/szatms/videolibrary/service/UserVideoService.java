package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.UserVideoMapper;
import io.github.szatms.videolibrary.mapper.VideoMapper;
import io.github.szatms.videolibrary.model.notemodel.Note;
import io.github.szatms.videolibrary.model.notemodel.NoteRepository;
import io.github.szatms.videolibrary.model.usermodel.UserRepository;
import io.github.szatms.videolibrary.model.uservideomodel.SortDirection;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoSortBy;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoSummaryResponseDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoUpdateDTO;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserVideoService {
    private final UserVideoRepository userVideoRepository;
    private final UserRepository userRepository;
    private final UserVideoMapper userVideoMapper;
    private final VideoService videoService;
    private final TrashService trashService;
    private final VideoMapper videoMapper;
    private final VideoRepository videoRepository;
    private final NoteRepository noteRepository;

    public UserVideo addVideo(String userId, String youtubeId){
        return addVideo(userId, youtubeId, false);
    }

    public UserVideo addVideo(String userId, String youtubeId, boolean inPlaylist){
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (youtubeId == null) {
            throw new IllegalArgumentException("Invalid YouTube video id");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalStateException("User not found");
        }

        Video video = videoService.getOrCreateVideo(youtubeId);

        Optional<UserVideo> existingUserVideo = userVideoRepository.findByUserIdAndVideoId(userId, video.getVideoId());
        if (existingUserVideo.isPresent()) {
            UserVideo existing = existingUserVideo.get();
            // Only refuse if trying to add the same inPlaylist state twice
            if (existing.isInPlaylist() == inPlaylist) {
                throw new IllegalArgumentException("Video already added");
            }
            // If existing is in playlist, but we want to add as standalone, or vice versa,
            // we should actually update the existing one (this is handled by the fact that
            // the save below will fail due to unique constraint)
        }

        UserVideo userVideo = UserVideo.builder()
                .userId(userId)
                .videoId(video.getVideoId())
                .watched(false)
                .noteIds(new ArrayList<>())
                .timestamps(new ArrayList<>())
                .inPlaylist(inPlaylist)  // Set based on parameter
                .addedAt(Instant.now())
                .build();

        return userVideoRepository.save(userVideo);
    }

    public UserVideo getVideo(String userId, String userVideoId){
        return userVideoRepository.findById(userVideoId)
                .filter(uv -> uv.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalStateException("Video not found"));
    }

    public UserVideo updateVideo(String userId, String userVideoId, UserVideoUpdateDTO dto) {
        UserVideo userVideo = getVideo(userId, userVideoId);
        userVideoMapper.updateEntityFromDTO(dto, userVideo);
        return userVideoRepository.save(userVideo);
    }

    public void deleteVideo(String userVideoId) {
        UserVideo userVideo = userVideoRepository.getById(userVideoId);
        long assignmentCount = userVideoRepository.countByVideoId(userVideo.getVideoId());

        String restoreId = UUID.randomUUID().toString();
        trashService.moveUserVideoToTrash(userVideo, restoreId);
        userVideoRepository.deleteById(userVideo.getId());

        if (assignmentCount == 1) {
            Video video = videoService.getById(userVideo.getVideoId());
            trashService.moveVideoToTrash(video, restoreId);
            videoService.deleteById(userVideo.getVideoId());
        }
    }

    public void deleteVideo(String userVideoId, String restoreId) {
        UserVideo userVideo = userVideoRepository.getById(userVideoId);
        long assignmentCount = userVideoRepository.countByVideoId(userVideo.getVideoId());

        trashService.moveUserVideoToTrash(userVideo, restoreId);
        userVideoRepository.deleteById(userVideo.getId());

        if (assignmentCount == 1) {
            Video video = videoService.getById(userVideo.getVideoId());
            trashService.moveVideoToTrash(video, restoreId);
            videoService.deleteById(userVideo.getVideoId());
        }
    }

    public void deleteVideos(List<String> userVideoIds){
        for (String id : userVideoIds){deleteVideo(id);}
    }

    public void deleteVideos(List<String> userVideoIds, String restoreId){
        for (String id : userVideoIds){deleteVideo(id, restoreId);}
    }

    public List<UserVideo> getVideos(
            String userId,
            UserVideoSortBy sortBy,
            SortDirection direction,
            boolean unwatchedFirst
    ) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalStateException("User not found");
        }

        UserVideoSortBy effectiveSortBy = sortBy == null ? UserVideoSortBy.ADDED_AT : sortBy;
        SortDirection effectiveDirection = direction == null ? SortDirection.DESC : direction;

        List<UserVideo> userVideos;
        if (effectiveSortBy == UserVideoSortBy.VIEWS) {
            userVideos = getVideosSortedByViews(userId, effectiveDirection, unwatchedFirst);
        } else {
            userVideos = userVideoRepository.findAllByUserId(
                    userId,
                    buildDatabaseSort(effectiveSortBy, effectiveDirection, unwatchedFirst)
            );
        }
        
        return userVideos;
    }

    private List<UserVideo> getVideosSortedByViews(String userId, SortDirection direction, boolean unwatchedFirst) {
        List<UserVideo> userVideos = userVideoRepository.findAllByUserId(
                userId,
                Sort.by(Sort.Direction.DESC, "addedAt")
        );

        Map<String, Video> videosById = videoService.getByIds(userVideos.stream()
                        .map(UserVideo::getVideoId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(Video::getVideoId, Function.identity()));

        Comparator<UserVideo> comparator = Comparator.comparingLong(
                (UserVideo userVideo) -> getViewCount(videosById.get(userVideo.getVideoId()))
        );

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        if (unwatchedFirst) {
            comparator = Comparator.comparing(UserVideo::isWatched).thenComparing(comparator);
        }

        comparator = comparator.thenComparing(UserVideo::getAddedAt, Comparator.nullsLast(Comparator.reverseOrder()));

        return userVideos.stream()
                .sorted(comparator)
                .toList();
    }

    private Sort buildDatabaseSort(UserVideoSortBy sortBy, SortDirection direction, boolean unwatchedFirst) {
        Sort.Direction sortDirection = direction == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.unsorted();

        if (unwatchedFirst && sortBy != UserVideoSortBy.WATCHED) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "watched"));
        }

        if (sortBy == UserVideoSortBy.WATCHED) {
            return sort.and(Sort.by(
                    new Sort.Order(sortDirection, "watched"),
                    new Sort.Order(Sort.Direction.DESC, "addedAt")
            ));
        }

        return sort.and(Sort.by(
                new Sort.Order(sortDirection, "addedAt")
        ));
    }

    private long getViewCount(Video video) {
        if (video == null || video.getStats() == null) {
            return 0L;
        }
        return video.getStats().getViewCount();
    }

    public List<UserVideoSummaryResponseDTO> getVideosForChannel(
            String userId,
            String channelId
    ) {
        List<UserVideo> userVideos = userVideoRepository.findAllByUserId(
                userId,
                Sort.unsorted()
        );

        if (userVideos.isEmpty()) {
            return List.of();
        }

        Map<String, Video> videosById = videoService
                .getByIds(
                        userVideos.stream()
                                .map(UserVideo::getVideoId)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        Video::getVideoId,
                        Function.identity()
                ));

        return userVideos.stream()
                .filter(userVideo -> {
                    Video video = videosById.get(
                            userVideo.getVideoId()
                    );

                    return video != null
                            && channelId.equals(
                            video.getChannelId()
                    );
                })
                .map(userVideo -> {
                    Video video = videosById.get(
                            userVideo.getVideoId()
                    );

                    return userVideoMapper.toSummaryResponseDTO(
                            userVideo,
                            videoMapper.toSummaryDTO(video)
                    );
                })
                .toList();
    }
}
