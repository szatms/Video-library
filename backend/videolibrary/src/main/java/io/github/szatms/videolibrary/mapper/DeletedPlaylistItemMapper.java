package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem.DeletedPlaylistItem;
import org.springframework.stereotype.Component;

@Component
public class DeletedPlaylistItemMapper {
    //=========================
    // ENTITY --> DELETED ENTITY
    //=========================
    public DeletedPlaylistItem toDeletedPlaylistItem(PlaylistItem playlistItem){
        DeletedPlaylistItem deletedPlaylistItem = new DeletedPlaylistItem();
        deletedPlaylistItem.setPlaylistItem(playlistItem);
        return deletedPlaylistItem;
    }

    //=========================
    // DELETED ENTITY --> ENTITY
    //=========================
    public PlaylistItem toPlaylistItem(DeletedPlaylistItem deletedPlaylistItem){return  deletedPlaylistItem.getPlaylistItem();}
}
