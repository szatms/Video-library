package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.UserVideoMapper;
import io.github.szatms.videolibrary.model.usermodel.UserRepository;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoUpdateDTO;
import io.github.szatms.videolibrary.model.videomodel.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserVideoService {
    private final UserVideoRepository userVideoRepository;
    private final UserRepository userRepository;
    private final UserVideoMapper userVideoMapper;
    private final VideoService videoService;

    public UserVideo addVideo(String userId, String youtubeId){
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

        if (userVideoRepository.findByUserIdAndVideoId(userId, video.getVideoId()).isPresent()) {
            throw new IllegalArgumentException("Video already added");
        }

        UserVideo userVideo = UserVideo.builder()
                .userId(userId)
                .videoId(video.getVideoId())
                .watched(false)
                .note(null)
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

    public List<UserVideo> getVideos(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalStateException("User not found");
        }

        return userVideoRepository.findAllByUserIdOrderByAddedAtDesc(userId);
    }
}
