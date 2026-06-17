package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.ChannelMapper;
import io.github.szatms.videolibrary.model.channelmodel.Channel;
import io.github.szatms.videolibrary.model.channelmodel.dto.ChannelResponseDTO;
import io.github.szatms.videolibrary.model.channelmodel.dto.ChannelSummaryDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoSummaryResponseDTO;
import io.github.szatms.videolibrary.service.ChannelService;
import io.github.szatms.videolibrary.service.UserService;
import io.github.szatms.videolibrary.service.UserVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {
    private final ChannelMapper channelMapper;
    private final ChannelService channelService;
    private final UserService userService;
    private final UserVideoService userVideoService;

    @GetMapping
    public List<ChannelSummaryDTO> getChannels() {

        String userId = userService
                .getCurrentUser()
                .getUserId();

        return channelService.getChannelsForUser(userId)
                .stream()
                .map(channelMapper::toChannelSummaryDTO)
                .toList();
    }

    @GetMapping("/{channelId}")
    public ChannelResponseDTO getChannel(
            @PathVariable String channelId
    ) {
        Channel channel = channelService.getById(channelId);

        return channelMapper.toChannelResponseDTO(channel);
    }

    @GetMapping("/{channelId}/videos")
    public List<UserVideoSummaryResponseDTO> getChannelVideos(
            @PathVariable String channelId
    ) {

        String userId = userService
                .getCurrentUser()
                .getUserId();

        return userVideoService.getVideosForChannel(
                userId,
                channelId
        );
    }
}
