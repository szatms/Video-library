package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.codexmodel.Codex;
import io.github.szatms.videolibrary.model.trashmodel.deletedcodex.DeletedCodex;
import org.springframework.stereotype.Component;

@Component
public class DeletedCodexMapper {
    public DeletedCodex toDeletedCodex(Codex codex){
        DeletedCodex deletedCodex = new DeletedCodex();
        deletedCodex.setCodex(codex);
        return deletedCodex;
    }

    public Codex toCodex(DeletedCodex deletedCodex){return deletedCodex.getCodex();}
}
