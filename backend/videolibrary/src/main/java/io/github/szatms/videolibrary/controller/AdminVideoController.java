package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.model.videomodel.dto.VideoRefreshJobResponseDTO;
import io.github.szatms.videolibrary.service.VideoRefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/videos")
public class AdminVideoController {
    private final VideoRefreshService videoRefreshService;

    @PostMapping("/refresh")
    public ResponseEntity<VideoRefreshJobResponseDTO> refreshVideos() {
        return ResponseEntity.accepted()
                .body(videoRefreshService.startRefreshAllVideos());
    }

    @GetMapping("/refresh/{jobId}")
    public VideoRefreshJobResponseDTO getRefreshJob(@PathVariable String jobId) {
        return videoRefreshService.getJob(jobId);
    }
}
