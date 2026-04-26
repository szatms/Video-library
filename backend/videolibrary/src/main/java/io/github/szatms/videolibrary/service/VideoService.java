package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.integration.youtube.PythonVideoDataProvider;
import io.github.szatms.videolibrary.integration.youtube.factory.VideoFactory;
import io.github.szatms.videolibrary.model.channelmodel.ChannelRepository;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoFactory videoFactory;
    private final PythonVideoDataProvider provider;

    public Video importVideo() {
        try {
            var response = provider.load();

            if (response.getItems() == null || response.getItems().isEmpty()) {
                throw new IllegalArgumentException("No video found");
            }

            var item = response.getItems().get(0);

            Video video = videoFactory.fromItem(item);

            return videoRepository.save(video);

        } catch (Exception e) {
            throw new RuntimeException("Failed to import video", e);
        }
    }

    public Video getOrCreateVideo(String youtubeId) {
        return videoRepository.findByYoutubeId(youtubeId)
                .orElseGet(() -> {
                    try {
                        var response = provider.load(); // TODO: válts át a python microservice-re
                        if (response.getItems() == null || response.getItems().isEmpty()) {
                            throw new IllegalArgumentException("No video found for youtubeId: " + youtubeId);
                        }

                        var item = response.getItems().get(0);

                        Video video = videoFactory.fromItem(item);
                        video.setYoutubeId(youtubeId);
                        return videoRepository.save(video);

                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch video for youtubeId: " + youtubeId, e);
                    }
                });
    }

    public Video getById(String videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalStateException("Video not found"));
    }
}
