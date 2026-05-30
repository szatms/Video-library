package io.github.szatms.videolibrary.model.videomodel.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class VideoRefreshJobResponseDTO {
    private String jobId;
    private String status;
    private Instant createdAt;
    private Instant startedAt;
    private Instant finishedAt;
    private long totalVideos;
    private int processedVideos;
    private int updatedVideos;
    private int failedVideos;
    private String currentVideoId;
    private String errorMessage;
}
