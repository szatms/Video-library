import React from "react";
import NotesEditor from "../notes/NotesEditor";

function CodexContentDisplay({ selectedItem, codexId, onBack }) {
  // Handle Note type items
  if (selectedItem && selectedItem.contentType === "NOTE") {
    return (
      <div className="h-100 overflow-auto" style={{ background: "transparent" }}>
        <NotesEditor
          note={selectedItem.content || ""}
          onNoteChange={(newNote) => {
            // For notes in codex, we could update the note in the backend
            // but for now we'll just update the local state
          }}
          onSave={() => {}}
          loading={false}
          saveState="idle"
          saveError=""
          isDirty={false}
          onTogglePreview={() => {}}
          previewMode={false}
          onIsDirtyChange={() => {}}
          parentId={selectedItem.id}
          parentType="note"
          notes={[]}
          onFetchNotes={() => {}}
        />
      </div>
    );
  }

  // Return empty state if not a note
  return (
    <div className="d-flex flex-column align-items-center justify-content-center h-100 text-muted">
      <i className="bi bi-file-earmark" style={{ fontSize: "3rem", marginBottom: "1rem" }}></i>
      <p className="text-center">Select a note to view details</p>
    </div>
  );
}

export default CodexContentDisplay;