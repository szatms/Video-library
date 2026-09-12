package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.notemodel.Note;
import io.github.szatms.videolibrary.model.trashmodel.deletednote.DeletedNote;
import org.springframework.stereotype.Component;

@Component
public class DeletedNoteMapper {
    //=========================
    // ENTITY --> DELETED ENTITY
    //=========================
    public DeletedNote toDeletedNote(Note note){
        DeletedNote deletedNote = new DeletedNote();
        deletedNote.setNote(note);
        return deletedNote;
    }

    //=========================
    // DELETED ENTITY --> ENTITY
    //=========================
    public Note toNote(DeletedNote deletedNote){return deletedNote.getNote();}
}
