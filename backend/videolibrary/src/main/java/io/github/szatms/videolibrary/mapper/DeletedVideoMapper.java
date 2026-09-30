package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.trashmodel.deletedvideo.DeletedVideo;
import io.github.szatms.videolibrary.model.videomodel.Video;
import org.springframework.stereotype.Component;

@Component
public class DeletedVideoMapper {
    public DeletedVideo toDeletedVideo(Video video) {
        DeletedVideo deletedVideo = new DeletedVideo();
        deletedVideo.setVideo(video);
        return deletedVideo;
    }

    public Video toVideo(DeletedVideo deletedVideo) {return deletedVideo.getVideo();}
}
