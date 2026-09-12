package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedPlaylist;
import org.springframework.stereotype.Component;

@Component
public class DeletedPlaylistMapper {
    //=========================
    // ENTITY --> DELETED ENTITY
    //=========================
    public DeletedPlaylist toDeletedPlaylist(Playlist playlist){
        DeletedPlaylist deletedPlaylist = new DeletedPlaylist();
        deletedPlaylist.setPlaylist(playlist);
        return deletedPlaylist;
    }

    //=========================
    // DELETED ENTITY --> ENTITY
    //=========================
    public Playlist toPlaylist(DeletedPlaylist deletedPlaylist){return deletedPlaylist.getPlaylist();}
}
