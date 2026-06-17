package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.exception.PythonMicroserviceUnavailableException;
import io.github.szatms.videolibrary.integration.youtube.PythonChannelDataProvider;
import io.github.szatms.videolibrary.integration.youtube.factory.ChannelFactory;
import io.github.szatms.videolibrary.model.channelmodel.Channel;
import io.github.szatms.videolibrary.model.channelmodel.ChannelRepository;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {
    private final UserVideoRepository userVideoRepository;
    private final VideoRepository videoRepository;
    private final ChannelRepository channelRepository;
    private final ChannelFactory channelFactory;
    private final PythonChannelDataProvider provider;

    public Channel getOrCreateChannel(String channelId){
        return channelRepository.findByChannelId(channelId)
                .orElseGet(() -> {
                    try{
                        var response = provider.load(channelId);
                        if(response == null || response.getItems().get(0).getId() == null || response.getItems().get(0).getId().isBlank()){
                            throw new IllegalArgumentException("No channel found with id " + channelId);
                        }

                        Channel channel = channelFactory.fromItem(response);
                        channel.setChannelId(channelId);
                        return channelRepository.save(channel);
                    } catch (ResourceAccessException e) {
                        throw new PythonMicroserviceUnavailableException(
                                "Parsing service down",
                                e
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to fetch channel for channelId " + channelId, e);
                    }
                });
    }

    public Channel getById(String channelId){
        return channelRepository.findByChannelId(channelId)
                .orElseThrow(() -> new IllegalStateException("Channel not found"));
    }

    public List<Channel> getChannelsForUser(String userId) {

        List<String> videoIds = userVideoRepository
                .findAllByUserId(userId, Sort.unsorted())
                .stream()
                .map(UserVideo::getVideoId)
                .toList();

        if (videoIds.isEmpty()) {
            return List.of();
        }

        List<Video> videos = videoRepository.findByVideoIdIn(videoIds);

        Set<String> channelIds = videos.stream()
                .map(Video::getChannelId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (channelIds.isEmpty()) {
            return List.of();
        }

        return channelRepository.findAllById(channelIds);
    }

    public List<Channel> getByIds(Iterable<String> channelIds){return channelRepository.findAllById(channelIds);}

    public void deleteById(String channelId){channelRepository.deleteById(channelId);}
}
