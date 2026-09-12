package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedUserPlaylist;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import org.springframework.stereotype.Component;

@Component
public class DeletedUserPlaylistMapper {
    //=========================
    // ENTITY --> DELETED ENTITY
    //=========================
    public DeletedUserPlaylist toDeletedUserPlaylist(UserPlaylist userPlaylist) {
        DeletedUserPlaylist deletedUserPlaylist = new DeletedUserPlaylist();
        deletedUserPlaylist.setUserPlaylist(userPlaylist);
        return deletedUserPlaylist;
    }

    //=========================
    // DELETED ENTITY --> ENTITY
    //=========================
    public UserPlaylist toUserPlaylist(DeletedUserPlaylist deletedUserPlaylist) {return deletedUserPlaylist.getUserPlaylist();}
}
