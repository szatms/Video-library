package io.github.szatms.videolibrary.model.channelmodel;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChannelRepository extends MongoRepository<Channel, String> {
    Optional<Channel> findByChannelId(String channelId);
    boolean existsByChannelId(String channelId);
}
