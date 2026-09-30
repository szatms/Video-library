package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedPlaylist;
import org.springframework.stereotype.Component;

@Component
public class DeletedPlaylistMapper {
    public DeletedPlaylist toDeletedPlaylist(Playlist playlist){
        DeletedPlaylist deletedPlaylist = new DeletedPlaylist();
        deletedPlaylist.setPlaylist(playlist);
        return deletedPlaylist;
    }

    public Playlist toPlaylist(DeletedPlaylist deletedPlaylist){return deletedPlaylist.getPlaylist();}
}
