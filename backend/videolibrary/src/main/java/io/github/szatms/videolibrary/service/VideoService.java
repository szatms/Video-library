package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.exception.PythonMicroserviceUnavailableException;
import io.github.szatms.videolibrary.integration.youtube.PythonVideoDataProvider;
import io.github.szatms.videolibrary.integration.youtube.factory.VideoFactory;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoFactory videoFactory;
    private final PythonVideoDataProvider provider;

    public Video getOrCreateVideo(String youtubeId) {
        return videoRepository.findByYoutubeId(youtubeId)
                .orElseGet(() -> {
                    try {
                        var response = provider.load(youtubeId);
                        if (response.getItems() == null || response.getItems().isEmpty()) {
                            throw new IllegalArgumentException("No video found for youtubeId: " + youtubeId);
                        }

                        var item = response.getItems().get(0);

                        Video video = videoFactory.fromItem(item);
                        video.setYoutubeId(youtubeId);
                        return videoRepository.save(video);

                    } catch (ResourceAccessException e) {
                        throw new PythonMicroserviceUnavailableException(
                                "Video parsing service down",
                                e
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch video for youtubeId: " + youtubeId, e);
                    }
                });
    }

    public Video getById(String videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalStateException("Video not found"));
    }

    public List<Video> getByIds(Iterable<String> videoIds) {
        return videoRepository.findAllById(videoIds);
    }
}
