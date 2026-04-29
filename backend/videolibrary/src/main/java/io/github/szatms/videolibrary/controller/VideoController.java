package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.VideoMapper;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.dto.VideoResponseDTO;
import io.github.szatms.videolibrary.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/videos")
public class VideoController {
    private final VideoService videoService;
    private final VideoMapper videoMapper;

    @PostMapping("/import")
    public VideoResponseDTO importVideo(@RequestParam String youtubeId) {
        Video video = videoService.getOrCreateVideo(youtubeId);
        return videoMapper.toResponseDTO(video);
    }
}
