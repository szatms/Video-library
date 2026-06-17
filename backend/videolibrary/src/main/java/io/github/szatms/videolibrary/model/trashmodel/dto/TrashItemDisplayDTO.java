package io.github.szatms.videolibrary.model.trashmodel.dto;

import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import lombok.Data;

import java.time.Instant;

@Data
public class TrashItemDisplayDTO {

    private String id;

    private String title;
    private String thumbnailUrl;
    private String channelTitle;

    private Instant addedAt;
    private Instant deletedAt;
    private Instant purgeAt;
}
