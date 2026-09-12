package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem.DeletedUserPlaylistItem;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItem;
import org.springframework.stereotype.Component;

@Component
public class DeletedUserPlaylistItemMapper {
    //=========================
    // ENTITY --> DELETED ENTITY
    //=========================
    public DeletedUserPlaylistItem toDeletedUserPlaylistItem(UserPlaylistItem userPlaylistItem){
        DeletedUserPlaylistItem deletedUserPlaylistItem = new DeletedUserPlaylistItem();
        deletedUserPlaylistItem.setUserPlaylistItem(userPlaylistItem);
        return deletedUserPlaylistItem;
    }

    //=========================
    // DELETED ENTITY --> ENTITY
    //=========================
    public UserPlaylistItem toUserPlaylistItem(DeletedUserPlaylistItem deletedUserPlaylistItem){return deletedUserPlaylistItem.getUserPlaylistItem();}
}
