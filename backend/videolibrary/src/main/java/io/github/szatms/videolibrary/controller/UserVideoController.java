package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.UserVideoMapper;
import io.github.szatms.videolibrary.mapper.VideoMapper;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoCreateDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoResponseDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoSummaryResponseDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoUpdateDTO;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.service.UserVideoService;
import io.github.szatms.videolibrary.service.VideoService;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.utils.LinkUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uservideos")
public class UserVideoController {
    private final UserVideoService userVideoService;
    private final UserVideoMapper userVideoMapper;
    private final VideoService videoService;
    private final VideoMapper videoMapper;
    private final LinkUtils linkUtils;

    @GetMapping
    public List<UserVideoSummaryResponseDTO> getVideos(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        List<UserVideo> userVideos = userVideoService.getVideos(userId);
        Map<String, Video> videosById = getVideosById(userVideos);

        return userVideos.stream()
                .map(userVideo -> userVideoMapper.toSummaryResponseDTO(
                        userVideo,
                        videoMapper.toSummaryDTO(requireVideo(videosById, userVideo.getVideoId()))
                ))
                .toList();
    }

    @GetMapping("/detailed")
    public List<UserVideoResponseDTO> getDetailedVideos(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        List<UserVideo> userVideos = userVideoService.getVideos(userId);
        Map<String, Video> videosById = getVideosById(userVideos);

        return userVideos.stream()
                .map(userVideo -> userVideoMapper.toResponseDTO(
                        userVideo,
                        videoMapper.toResponseDTO(requireVideo(videosById, userVideo.getVideoId()))
                ))
                .toList();
    }

    @PostMapping
    public UserVideoResponseDTO addVideo(
            @RequestBody UserVideoCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getUser().getUserId();
        String youtubeId = linkUtils.getYTVideoId(dto.getUrl());

        if (youtubeId == null) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }

        var userVideo = userVideoService.addVideo(userId, youtubeId);

        Video video = videoService.getById(userVideo.getVideoId());

        return userVideoMapper.toResponseDTO(
                userVideo,
                videoMapper.toResponseDTO(video)
        );
    }

    @GetMapping("/{id}")
    public UserVideoResponseDTO getVideo(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        UserVideo uv = userVideoService.getVideo(userId, id);

        Video video = videoService.getById(uv.getVideoId());

        return userVideoMapper.toResponseDTO(
                uv,
                videoMapper.toResponseDTO(video)
        );
    }

    @PatchMapping("/{id}")
    public UserVideoResponseDTO updateVideo(
            @PathVariable String id,
            @RequestBody UserVideoUpdateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        UserVideo userVideo = userVideoService.updateVideo(userId, id, dto);
        Video video = videoService.getById(userVideo.getVideoId());

        return userVideoMapper.toResponseDTO(
                userVideo,
                videoMapper.toResponseDTO(video)
        );
    }

    private Map<String, Video> getVideosById(List<UserVideo> userVideos) {
        return videoService.getByIds(userVideos.stream()
                        .map(UserVideo::getVideoId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(Video::getVideoId, Function.identity()));
    }

    private Video requireVideo(Map<String, Video> videosById, String videoId) {
        Video video = videosById.get(videoId);
        if (video == null) {
            throw new IllegalStateException("Video not found");
        }
        return video;
    }
}
