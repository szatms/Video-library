package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.DeletedUserVideoMapper;
import io.github.szatms.videolibrary.mapper.DeletedVideoMapper;
import io.github.szatms.videolibrary.model.trashmodel.DeletedUserVideo;
import io.github.szatms.videolibrary.model.trashmodel.DeletedUserVideoRepository;
import io.github.szatms.videolibrary.model.trashmodel.DeletedVideo;
import io.github.szatms.videolibrary.model.trashmodel.DeletedVideoRepository;
import io.github.szatms.videolibrary.model.trashmodel.dto.TrashItemDisplayDTO;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashService {
    private final DeletedVideoRepository deletedVideoRepository;
    private final DeletedUserVideoRepository deletedUserVideoRepository;
    private final DeletedVideoMapper deletedVideoMapper;
    private final DeletedUserVideoMapper deletedUserVideoMapper;
    private final VideoRepository videoRepository;
    private final UserVideoRepository userVideoRepository;

    //=========================
    // MOVING ENTITIES TO TRASH
    //=========================
    public DeletedVideo moveVideoToTrash(Video video, String restoreId) {

        DeletedVideo deletedVideo = deletedVideoMapper.toDeletedVideo(video);

        deletedVideo.setRestoreId(restoreId);
        deletedVideo.setDeletedAt(Instant.now());
        deletedVideo.setPurgeAt(Instant.now().plus(30, ChronoUnit.DAYS));

        return deletedVideoRepository.save(deletedVideo);
    }

    public DeletedUserVideo moveUserVideoToTrash(UserVideo userVideo, String restoreId) {
        DeletedUserVideo deletedUserVideo = deletedUserVideoMapper.toDeletedUserVideo(userVideo);

        deletedUserVideo.setRestoreId(restoreId);
        deletedUserVideo.setDeletedAt(Instant.now());
        deletedUserVideo.setPurgeAt(Instant.now().plus(30,ChronoUnit.DAYS));

        return deletedUserVideoRepository.save(deletedUserVideo);
    }

    //=========================
    // RESTORING ENTITIES
    //=========================
    public void restore(String restoreId) {
        restoreVideo(restoreId);
        restoreUserVideo(restoreId);
    }

    public void restoreVideo(String restoreId) {
        if (deletedVideoRepository.existsByRestoreId(restoreId)) {
            DeletedVideo deletedVideo = deletedVideoRepository
                    .findByRestoreId(restoreId)
                    .orElseThrow(RuntimeException::new);
            Video restoredVideo = deletedVideoMapper.toVideo(deletedVideo);
            videoRepository.save(restoredVideo);
            deletedVideoRepository.delete(deletedVideo);
        }
    }

    public void restoreUserVideo(String restoreId) {
        DeletedUserVideo deletedUserVideo = deletedUserVideoRepository
                .findByRestoreId(restoreId)
                .orElseThrow(RuntimeException::new);
        UserVideo userVideo = deletedUserVideoMapper.toUserVideo(deletedUserVideo);
        userVideoRepository.save(userVideo);
        deletedUserVideoRepository.delete(deletedUserVideo);
    }

    public List<TrashItemDisplayDTO> getDeletedUserVideos(String userId) {

        return deletedUserVideoRepository
                .findAllByUserVideoUserId(userId)
                .stream()
                .map(deletedUserVideo -> {

                    TrashItemDisplayDTO dto = new TrashItemDisplayDTO();

                    UserVideo userVideo = deletedUserVideo.getUserVideo();

                    Video video = deletedVideoRepository
                            .findByRestoreId(deletedUserVideo.getRestoreId())
                            .map(DeletedVideo::getVideo)
                            .orElseGet(() ->
                                    videoRepository
                                            .findById(userVideo.getVideoId())
                                            .orElseThrow(RuntimeException::new)
                            );

                    dto.setId(deletedUserVideo.getRestoreId());
                    dto.setAddedAt(userVideo.getAddedAt());
                    dto.setDeletedAt(deletedUserVideo.getDeletedAt());
                    dto.setPurgeAt(deletedUserVideo.getPurgeAt());

                    dto.setTitle(video.getTitle());
                    dto.setThumbnailUrl(video.getThumbnailUrl());
                    dto.setChannelTitle(video.getChannelTitle());

                    return dto;
                }).toList();
    }
}
