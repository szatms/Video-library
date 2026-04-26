package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.UserVideoMapper;
import io.github.szatms.videolibrary.mapper.VideoMapper;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoCreateDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoResponseDTO;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.service.UserService;
import io.github.szatms.videolibrary.service.UserVideoService;
import io.github.szatms.videolibrary.service.VideoService;
import io.github.szatms.videolibrary.utils.LinkUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uservideos")
public class UserVideoController {
    private final UserVideoService userVideoService;
    private final UserVideoMapper userVideoMapper;
    private final VideoService videoService;
    private final VideoMapper videoMapper;
    private final LinkUtils linkUtils;

    @PostMapping
    public UserVideoResponseDTO addVideo(
            @RequestBody UserVideoCreateDTO dto,
            @AuthenticationPrincipal(expression = "user.userId") String userId) {

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
            @AuthenticationPrincipal(expression = "user.userId") String userId
    ) {
        UserVideo uv = userVideoService.getVideo(userId, id);

        Video video = videoService.getById(uv.getVideoId());

        return userVideoMapper.toResponseDTO(
                uv,
                videoMapper.toResponseDTO(video)
        );
    }
}
