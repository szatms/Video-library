import { useEffect, useRef, useState } from "react";
import api from "../../services/api";

function getSafeExternalUrl(rawUrl) {
  try {
    const parsed = new URL(rawUrl);
    return parsed.protocol === "http:" || parsed.protocol === "https:" ? parsed.href : null;
  } catch {
    return null;
  }
}

function renderInlineMarkdown(text) {
  const nodes = [];
  const pattern = /(\*\*[^*]+\*\*|\*[^*]+\*|`[^`]+`|\[[^\]]+\]\([^)]+\))/g;
  let lastIndex = 0;
  let key = 0;

  for (const match of text.matchAll(pattern)) {
    const [token] = match;
    const index = match.index ?? 0;

    if (index > lastIndex) {
      nodes.push(text.slice(lastIndex, index));
    }

    if (token.startsWith("**") && token.endsWith("**")) {
      nodes.push(<strong key={key++}>{token.slice(2, -2)}</strong>);
    } else if (token.startsWith("*") && token.endsWith("*")) {
      nodes.push(<em key={key++}>{token.slice(1, -1)}</em>);
    } else if (token.startsWith("`") && token.endsWith("`")) {
      nodes.push(<code key={key++}>{token.slice(1, -1)}</code>);
    } else {
      const linkMatch = token.match(/^\[([^\]]+)\]\(([^)]+)\)$/);
      if (linkMatch) {
        const safeUrl = getSafeExternalUrl(linkMatch[2]);

        if (safeUrl) {
          nodes.push(
            <a
              key={key++}
              href={safeUrl}
              target="_blank"
              rel="noreferrer"
            >
              {linkMatch[1]}
            </a>
          );
        } else {
          nodes.push(linkMatch[1]);
        }
      } else {
        nodes.push(token);
      }
    }

    lastIndex = index + token.length;
  }

  if (lastIndex < text.length) {
    nodes.push(text.slice(lastIndex));
  }

  return nodes.length > 0 ? nodes : text;
}

function renderMarkdown(text) {
  if (!text.trim()) {
    return <p className="mb-0 text-muted">No notes yet.</p>;
  }

  const lines = text.split("\n");
  const elements = [];
  let listItems = [];
  let listType = null;

  const flushList = () => {
    if (listItems.length === 0) {
      return;
    }

    const ListTag = listType;
    elements.push(
      <ListTag key={`list-${elements.length}`} className="mb-3 ps-3">
        {listItems.map((item, index) => (
          <li key={index}>{renderInlineMarkdown(item)}</li>
        ))}
      </ListTag>
    );
    listItems = [];
    listType = null;
  };

  lines.forEach((line, index) => {
    const trimmed = line.trim();

    if (!trimmed) {
      flushList();
      return;
    }

    if (trimmed.startsWith("### ")) {
      flushList();
      elements.push(<h6 key={index}>{renderInlineMarkdown(trimmed.slice(4))}</h6>);
      return;
    }

    if (trimmed.startsWith("## ")) {
      flushList();
      elements.push(<h5 key={index}>{renderInlineMarkdown(trimmed.slice(3))}</h5>);
      return;
    }

    if (trimmed.startsWith("# ")) {
      flushList();
      elements.push(<h4 key={index}>{renderInlineMarkdown(trimmed.slice(2))}</h4>);
      return;
    }

    if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
      if (listType !== "ul") {
        flushList();
        listType = "ul";
      }
      listItems.push(trimmed.slice(2));
      return;
    }

    if (/^\d+\.\s/.test(trimmed)) {
      if (listType !== "ol") {
        flushList();
        listType = "ol";
      }
      listItems.push(trimmed.replace(/^\d+\.\s/, ""));
      return;
    }

    flushList();
    elements.push(
      <p key={index} className="mb-3">
        {renderInlineMarkdown(trimmed)}
      </p>
    );
  });

  flushList();

  return elements;
}

function formatTimestamp(seconds) {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
  }

  return `${minutes}:${String(secs).padStart(2, "0")}`;
}

function NotesEditor({
  note,
  onNoteChange,
  onSave,
  loading,
  saveState,
  saveError,
  isDirty,
  onTogglePreview,
  previewMode,
  onSaveError,
  onIsDirtyChange,
  parentId, // For note management
  parentType, // 'video' or 'playlist'
  currentUser,
  notes, // Pass in notes for display
  onAddNotes,
  onRemoveNotes,
  onFetchNotes
}) {
  const [showAddNoteModal, setShowAddNoteModal] = useState(false);
  const [showRemoveNoteModal, setShowRemoveNoteModal] = useState(false);
  const [activeTab, setActiveTab] = useState("add-existing");
  const [existingNotes, setExistingNotes] = useState([]);
  const [loadingExistingNotes, setLoadingExistingNotes] = useState(false);
  const [selectedNoteIds, setSelectedNoteIds] = useState(new Set());
  const [newNoteTitle, setNewNoteTitle] = useState("");
  const [newNoteContent, setNewNoteContent] = useState("");
  const [attachedNotes, setAttachedNotes] = useState([]);
  const [loadingAttachedNotes, setLoadingAttachedNotes] = useState(false);
  const [selectedRemoveNoteIds, setSelectedRemoveNoteIds] = useState(new Set());
  const textareaRef = useRef(null);

  const applyFormat = (prefix, suffix = "", placeholder = "text") => {
    const textarea = textareaRef.current;

    if (!textarea) {
      return;
    }

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selected = note.slice(start, end) || placeholder;
    const updated =
      note.slice(0, start) +
      prefix +
      selected +
      suffix +
      note.slice(end);

    onNoteChange(updated);
    onIsDirtyChange(true);
    
    requestAnimationFrame(() => {
      textarea.focus();
      const cursorStart = start + prefix.length;
      const cursorEnd = cursorStart + selected.length;
      textarea.setSelectionRange(cursorStart, cursorEnd);
    });
  };

  const fetchUserNotes = async () => {
    setLoadingExistingNotes(true);
    try {
      const response = await api.get('/notes');
      setExistingNotes(response.data);
    } catch (err) {
      console.error("Error fetching user notes:", err);
      if (onSaveError) {
        onSaveError("Failed to load existing notes");
      }
    } finally {
      setLoadingExistingNotes(false);
    }
  };

  const fetchAttachedNotes = async () => {
    setLoadingAttachedNotes(true);
    try {
      if (parentType === 'video') {
        const response = await api.get(`/uservideos/${parentId}/notes`);
        setAttachedNotes(response.data);
      } else {
        // For playlist, we would need to implement
        console.warn("Fetching attached notes for playlist not implemented");
      }
    } catch (err) {
      console.error("Error fetching attached notes:", err);
      if (onSaveError) {
        onSaveError("Failed to load attached notes");
      }
    } finally {
      setLoadingAttachedNotes(false);
    }
  };

  const handleAddNotes = async () => {
    if (selectedNoteIds.size === 0) {
      if (onSaveError) {
        onSaveError("Please select at least one note to add");
      }
      return;
    }

    try {
      const noteIds = Array.from(selectedNoteIds);
      if (parentType === 'video') {
        await api.post(`/uservideos/${parentId}/notes/add`, {
          parentId: parentId,
          noteIds: noteIds
        });
      } else {
        // For playlist, we would need to update the note to include the playlist ID
        throw new Error("Playlist note attachment not implemented yet");
      }
      
      setShowAddNoteModal(false);
      setSelectedNoteIds(new Set());
      if (onSaveError) {
        onSaveError("");
      }
      
      if (onFetchNotes) {
        onFetchNotes();
      }
    } catch (err) {
      console.error("Error adding notes:", err);
      if (onSaveError) {
        onSaveError("Failed to add notes");
      }
    }
  };

  const handleRemoveNotes = async () => {
    if (selectedRemoveNoteIds.size === 0) {
      if (onSaveError) {
        onSaveError("Please select at least one note to remove");
      }
      return;
    }

    try {
      const noteIds = Array.from(selectedRemoveNoteIds);
      if (parentType === 'video') {
        await api.post(`/uservideos/${parentId}/notes/remove`, {
          parentId: parentId,
          noteIds: noteIds
        });
      } else {
        // For playlist, we would need to update the note to remove the playlist ID
        throw new Error("Playlist note removal not implemented yet");
      }
      
      setShowRemoveNoteModal(false);
      setSelectedRemoveNoteIds(new Set());
      if (onSaveError) {
        onSaveError("");
      }
      
      if (onFetchNotes) {
        onFetchNotes();
      }
    } catch (err) {
      console.error("Error removing notes:", err);
      if (onSaveError) {
        onSaveError("Failed to remove notes");
      }
    }
  };

  const handleCreateNote = async () => {
    if (!newNoteTitle.trim()) {
      if (onSaveError) {
        onSaveError("Title is required");
      }
      return;
    }

    try {
      const response = await api.post('/notes/create', {
        parentId: null,
        title: newNoteTitle,
        content: "", 
        isPdf: false,
        parentIds: [parentId] // Send parent ID in parentIds array
      });
      
      setShowAddNoteModal(false);
      setNewNoteTitle("");
      setNewNoteContent("");
      if (onSaveError) {
        onSaveError("");
      }
      
      if (onFetchNotes) {
        onFetchNotes();
      }
    } catch (err) {
      console.error("Error creating note:", err);
      if (onSaveError) {
        if (err.response) {
          onSaveError(`Failed to create note: ${err.response.data?.message || 'Unknown error'}`);
        } else {
          onSaveError("Failed to create note - network error");
        }
      }
    }
  };

  if (loading) {
    return <div className="p-3">Loading notes...</div>;
  }

  return (
    <div
      style={{ backgroundColor: "transparent" }}
      className="p-3 d-flex flex-column h-100 overflow-auto"
    >
      <div className="d-flex justify-content-between align-items-start mb-3">
        <h5 className="mb-0">Notes</h5>
        <div className="d-flex gap-1">
          <button
            type="button"
            className="btn btn-sm btn-success"
            onClick={async () => {
              await fetchUserNotes();
              setShowAddNoteModal(true);
            }}
          >
            +
          </button>
          <button
            type="button"
            className="btn btn-sm btn-danger"
            onClick={async () => {
              await fetchAttachedNotes();
              setShowRemoveNoteModal(true);
            }}
          >
            -
          </button>
          <div className="btn-group btn-group-sm" role="group" aria-label="Note mode">
            <button
              type="button"
              className={`btn ${!previewMode ? "btn-light" : "btn-outline-light"}`}
              onClick={() => onTogglePreview(false)}
            >
              Edit
            </button>
            <button
              type="button"
              className={`btn ${previewMode ? "btn-light" : "btn-outline-light"}`}
              onClick={() => onTogglePreview(true)}
            >
              Preview
            </button>
          </div>
        </div>
      </div>

      <div className="d-flex flex-column gap-2 mb-3" style={{ maxHeight: "150px", overflowY: "auto" }}>
        {notes && notes.length > 0 ? (
          notes.map((note) => (
            <div
              key={note.id}
              className="card bg-dark text-white border-secondary"
              style={{ cursor: "pointer" }}
              onClick={() => {
                onNoteChange(note.content ?? "");
                onTogglePreview(false);
                }}
              >
                <div className="card-body p-2">
                  <h6 className="card-title mb-1" style={{ fontSize: "0.8rem" }}>{note.title}</h6>
                    <p className="card-text small mb-0" style={{ fontSize: "0.7rem", overflow: "hidden", textOverflow: "ellipsis", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" }}>
                      {note.content || "No content"}
                    </p>
                </div>
            </div>
            ))
          ) : (
            <div className="text-center text-muted">No notes yet</div>
              )}
            </div>

      <div className="btn-group btn-group-sm mb-2" role="group" aria-label="Markdown toolbar">
        <button type="button" className="btn btn-outline-light" onClick={() => applyFormat("**", "**", "bold")}>
          B
        </button>
        <button type="button" className="btn btn-outline-light" onClick={() => applyFormat("*", "*", "italic")}>
          I
        </button>
        <button type="button" className="btn btn-outline-light" onClick={() => applyFormat("# ", "", "Heading")}>
          H
        </button>
        <button type="button" className="btn btn-outline-light" onClick={() => applyFormat("- ", "", "List item")}>
          List
        </button>
        <button
          type="button"
          className="btn btn-outline-light"
          onClick={() => applyFormat("[", "](https://example.com)", "link text")}
        >
          Link
        </button>
      </div>



      {previewMode ? (
        <div
          className="border rounded p-3 flex-grow-1 overflow-auto"
          style={{ minHeight: "220px", background: "rgba(255,255,255,0.04)" }}
        >
          {renderMarkdown(note)}
        </div>
      ) : (
        <textarea
          ref={textareaRef}
          className="form-control flex-grow-1 mb-3"
          style={{
            minHeight: "220px",
            resize: "vertical",
            background: "rgba(255,255,255,0.04)",
            color: "inherit",
          }}
          value={note}
          onChange={(e) => {
            onNoteChange(e.target.value);
            onIsDirtyChange(true);
          }}
          placeholder="Write notes in Markdown..."
        />
      )}

      <div className="d-flex justify-content-between align-items-center mt-3">
        <small
          className={
            saveError
              ? "text-danger"
              : saveState === "saved"
                ? "text-success"
                : "text-white fw-bold"
          }
        >
          {saveError || (saveState === "saved" ? "Saved" : isDirty ? "Unsaved changes" : "Up to date")}
        </small>
        <button
          type="button"
          className="btn btn-success btn-sm"
          onClick={onSave}
          disabled={!isDirty || saveState === "saving"}
        >
          {saveState === "saving" ? "Saving..." : "Save"}
        </button>
      </div>

      {/* Add Notes Modal */}
      {showAddNoteModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: "rgba(0,0,0,0.5)" }}>
          <div className="modal-dialog">
            <div className="modal-content bg-dark text-white border-secondary">
              <div className="modal-header border-secondary">
                <ul className="nav nav-tabs border-bottom-0">
                  <li className="nav-item">
                    <button
                      className={`nav-link ${activeTab === "add-existing" ? "active" : ""}`}
                      onClick={() => setActiveTab("add-existing")}
                    >
                      Add existing
                    </button>
                  </li>
                  <li className="nav-item">
                    <button
                      className={`nav-link ${activeTab === "add-new" ? "active" : ""}`}
                      onClick={() => setActiveTab("add-new")}
                    >
                      Add new
                    </button>
                  </li>
                </ul>
                <button 
                  type="button" 
                  className="btn-close btn-close-white" 
                  onClick={() => setShowAddNoteModal(false)}
                ></button>
              </div>
              <div className="modal-body">
                {activeTab === "add-existing" ? (
                  <>
                    <p>Select notes to add to this {parentType}:</p>
                    {loadingExistingNotes ? (
                      <div className="text-center">Loading notes...</div>
                    ) : existingNotes.length === 0 ? (
                      <div className="text-center text-muted">No existing notes found</div>
                    ) : (
                      <div className="d-flex flex-column gap-2 max-height-300 overflow-auto">
                        {existingNotes.map((note) => (
                          <div 
                            key={note.id} 
                            className="card bg-secondary border-secondary"
                            style={{ cursor: "pointer" }}
                            onClick={() => {
                              const newSelected = new Set(selectedNoteIds);
                              if (newSelected.has(note.id)) {
                                newSelected.delete(note.id);
                              } else {
                                newSelected.add(note.id);
                              }
                              setSelectedNoteIds(newSelected);
                            }}
                          >
                            <div className="card-body p-2">
                              <div className="form-check">
                                <input
                                  className="form-check-input"
                                  type="checkbox"
                                  checked={selectedNoteIds.has(note.id)}
                                  onChange={() => {}}
                                />
                                <label className="form-check-label">
                                  <h6 className="card-title mb-1">{note.title}</h6>
                                  <p className="card-text small mb-0" style={{ fontSize: "0.7rem", overflow: "hidden", textOverflow: "ellipsis", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" }}>
                                    {note.content || "No content"}
                                  </p>
                                </label>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </>
                ) : (
                  <>
                    <div className="mb-3">
                      <label htmlFor="newNoteTitle" className="form-label">Title</label>
                      <input
                        type="text"
                        className="form-control bg-secondary text-white border-secondary"
                        id="newNoteTitle"
                        value={newNoteTitle}
                        onChange={(e) => setNewNoteTitle(e.target.value)}
                        placeholder="Enter note title"
                      />
                    </div>
                  </>
                )}
                {saveError && <div className="alert alert-danger mt-3">{saveError}</div>}
              </div>
              <div className="modal-footer border-secondary">
                <button 
                  type="button" 
                  className="btn btn-secondary" 
                  onClick={() => setShowAddNoteModal(false)}
                >
                  Cancel
                </button>
                <button 
                  type="button" 
                  className="btn btn-success" 
                  onClick={activeTab === "add-existing" ? handleAddNotes : handleCreateNote}
                  disabled={(activeTab === "add-existing" && selectedNoteIds.size === 0) || (activeTab === "add-new" && !newNoteTitle.trim())}
                >
                  {activeTab === "add-existing" ? "Add Selected Notes" : "Create"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Remove Notes Modal */}
      {showRemoveNoteModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: "rgba(0,0,0,0.5)" }}>
          <div className="modal-dialog">
            <div className="modal-content bg-dark text-white border-secondary">
              <div className="modal-header border-secondary">
                <h5 className="modal-title">Remove Notes</h5>
                <button 
                  type="button" 
                  className="btn-close btn-close-white" 
                  onClick={() => setShowRemoveNoteModal(false)}
                ></button>
              </div>
              <div className="modal-body">
                <p>Select notes to remove from this {parentType}:</p>
                {loadingAttachedNotes ? (
                  <div className="text-center">Loading notes...</div>
                ) : attachedNotes.length === 0 ? (
                  <div className="text-center text-muted">No notes attached to this {parentType}</div>
                ) : (
                  <div className="d-flex flex-column gap-2 max-height-300 overflow-auto">
                    {attachedNotes.map((note) => (
                      <div 
                        key={note.id} 
                        className="card bg-secondary border-secondary"
                        style={{ cursor: "pointer" }}
                        onClick={() => {
                          const newSelected = new Set(selectedRemoveNoteIds);
                          if (newSelected.has(note.id)) {
                            newSelected.delete(note.id);
                          } else {
                            newSelected.add(note.id);
                          }
                          setSelectedRemoveNoteIds(newSelected);
                        }}
                      >
                        <div className="card-body p-2">
                          <div className="form-check">
                            <input
                              className="form-check-input"
                              type="checkbox"
                              checked={selectedRemoveNoteIds.has(note.id)}
                              onChange={() => {}}
                            />
                            <label className="form-check-label">
                              <h6 className="card-title mb-1">{note.title}</h6>
                              <p className="card-text small mb-0" style={{ fontSize: "0.7rem", overflow: "hidden", textOverflow: "ellipsis", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" }}>
                                {note.content || "No content"}
                              </p>
                            </label>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
                {saveError && <div className="alert alert-danger mt-3">{saveError}</div>}
              </div>
              <div className="modal-footer border-secondary">
                <button 
                  type="button" 
                  className="btn btn-secondary" 
                  onClick={() => setShowRemoveNoteModal(false)}
                >
                  Cancel
                </button>
                <button 
                  type="button" 
                  className="btn btn-danger" 
                  onClick={handleRemoveNotes}
                  disabled={selectedRemoveNoteIds.size === 0}
                >
                  Remove Selected Notes
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default NotesEditor;