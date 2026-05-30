package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.integration.youtube.dto.PythonVideoResponseDTO;
import io.github.szatms.videolibrary.integration.youtube.PythonVideoDataProvider;
import io.github.szatms.videolibrary.integration.youtube.factory.VideoFactory;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import io.github.szatms.videolibrary.model.videomodel.dto.VideoRefreshJobResponseDTO;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class VideoRefreshService {
    private static final int PAGE_SIZE = 50;

    private final VideoRepository videoRepository;
    private final VideoFactory videoFactory;
    private final PythonVideoDataProvider provider;

    private final Map<String, VideoRefreshJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public VideoRefreshJobResponseDTO startRefreshAllVideos() {
        if (hasActiveJob()) {
            throw new IllegalArgumentException("Video refresh already in progress");
        }

        VideoRefreshJob job = new VideoRefreshJob(UUID.randomUUID().toString());
        jobs.put(job.getJobId(), job);
        executorService.submit(() -> runJob(job));
        return toResponseDTO(job);
    }

    public VideoRefreshJobResponseDTO getJob(String jobId) {
        VideoRefreshJob job = jobs.get(jobId);
        if (job == null) {
            throw new IllegalStateException("Video refresh job not found");
        }
        return toResponseDTO(job);
    }

    void runJob(VideoRefreshJob job) {
        try {
            job.markRunning(videoRepository.count());

            int pageNumber = 0;
            Page<Video> page;

            do {
                page = videoRepository.findAll(PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("videoId")));
                for (Video video : page.getContent()) {
                    refreshVideo(job, video);
                }
                pageNumber++;
            } while (page.hasNext());

            job.markCompleted();
        } catch (Exception e) {
            job.markFailed(e.getMessage());
        }
    }

    private void refreshVideo(VideoRefreshJob job, Video video) {
        job.setCurrentVideoId(video.getVideoId());

        try {
            PythonVideoResponseDTO response = provider.load(video.getYoutubeId());
            if (response.getItems() == null || response.getItems().isEmpty()) {
                throw new IllegalArgumentException("No video found for youtubeId: " + video.getYoutubeId());
            }

            videoFactory.updateEntityFromItem(video, response.getItems().get(0));
            videoRepository.save(video);
            job.markVideoUpdated();
        } catch (Exception e) {
            job.markVideoFailed(video.getVideoId(), e.getMessage());
        }
    }

    private boolean hasActiveJob() {
        return jobs.values().stream().anyMatch(VideoRefreshJob::isActive);
    }

    private VideoRefreshJobResponseDTO toResponseDTO(VideoRefreshJob job) {
        VideoRefreshJobResponseDTO dto = new VideoRefreshJobResponseDTO();
        dto.setJobId(job.getJobId());
        dto.setStatus(job.getStatus().name());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setStartedAt(job.getStartedAt());
        dto.setFinishedAt(job.getFinishedAt());
        dto.setTotalVideos(job.getTotalVideos());
        dto.setProcessedVideos(job.getProcessedVideos());
        dto.setUpdatedVideos(job.getUpdatedVideos());
        dto.setFailedVideos(job.getFailedVideos());
        dto.setCurrentVideoId(job.getCurrentVideoId());
        dto.setErrorMessage(job.getErrorMessage());
        return dto;
    }

    @PreDestroy
    void shutdownExecutor() {
        executorService.shutdownNow();
    }

    enum VideoRefreshJobStatus {
        QUEUED,
        RUNNING,
        COMPLETED,
        COMPLETED_WITH_ERRORS,
        FAILED
    }

    @Getter
    static class VideoRefreshJob {
        private final String jobId;
        private final Instant createdAt;
        private final AtomicInteger processedVideos = new AtomicInteger();
        private final AtomicInteger updatedVideos = new AtomicInteger();
        private final AtomicInteger failedVideos = new AtomicInteger();

        private volatile VideoRefreshJobStatus status;
        private volatile Instant startedAt;
        private volatile Instant finishedAt;
        private volatile long totalVideos;
        private volatile String currentVideoId;
        private volatile String errorMessage;

        VideoRefreshJob(String jobId) {
            this.jobId = jobId;
            this.createdAt = Instant.now();
            this.status = VideoRefreshJobStatus.QUEUED;
        }

        boolean isActive() {
            return status == VideoRefreshJobStatus.QUEUED || status == VideoRefreshJobStatus.RUNNING;
        }

        void markRunning(long totalVideos) {
            this.status = VideoRefreshJobStatus.RUNNING;
            this.startedAt = Instant.now();
            this.totalVideos = totalVideos;
        }

        void setCurrentVideoId(String currentVideoId) {
            this.currentVideoId = currentVideoId;
        }

        void markVideoUpdated() {
            updatedVideos.incrementAndGet();
            processedVideos.incrementAndGet();
        }

        void markVideoFailed(String videoId, String errorMessage) {
            currentVideoId = videoId;
            failedVideos.incrementAndGet();
            processedVideos.incrementAndGet();
            this.errorMessage = errorMessage;
        }

        void markCompleted() {
            this.status = failedVideos.get() > 0
                    ? VideoRefreshJobStatus.COMPLETED_WITH_ERRORS
                    : VideoRefreshJobStatus.COMPLETED;
            this.finishedAt = Instant.now();
            this.currentVideoId = null;
        }

        void markFailed(String errorMessage) {
            this.status = VideoRefreshJobStatus.FAILED;
            this.finishedAt = Instant.now();
            this.currentVideoId = null;
            this.errorMessage = errorMessage;
        }

        int getProcessedVideos() {
            return processedVideos.get();
        }

        int getUpdatedVideos() {
            return updatedVideos.get();
        }

        int getFailedVideos() {
            return failedVideos.get();
        }
    }
}
