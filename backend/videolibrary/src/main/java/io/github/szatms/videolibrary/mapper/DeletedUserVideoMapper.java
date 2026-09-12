package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.trashmodel.deletedvideo.DeletedUserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import org.springframework.stereotype.Component;

@Component
public class DeletedUserVideoMapper {
    //=========================
    // ENTITY --> DELETED ENTITY
    //=========================
    public DeletedUserVideo toDeletedUserVideo(UserVideo userVideo) {
        DeletedUserVideo deletedUserVideo = new DeletedUserVideo();
        deletedUserVideo.setUserVideo(userVideo);
        return deletedUserVideo;
    }

    //=========================
    // DELETED ENTITY --> ENTITY
    //=========================
    public UserVideo toUserVideo(DeletedUserVideo deletedUserVideo) {return deletedUserVideo.getUserVideo();}
}
