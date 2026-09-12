import { useState, useEffect, useRef } from "react";
import api from "../services/api";

export function useSharedDetailLogic({
  parentId,
  parentType,
  fetchNotesCallback,
  saveNoteCallback
}) {
  // State management for shared functionality
  const [noteDraft, setNoteDraft] = useState("");
  const [previewMode, setPreviewMode] = useState(false);
  const [saveState, setSaveState] = useState("idle");
  const [saveError, setSaveError] = useState("");
  const [isDirty, setIsDirty] = useState(false);
  const [notes, setNotes] = useState([]);
  const [loadingNotes, setLoadingNotes] = useState(false);
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
  const playerRef = useRef(null);

  // Fetch notes using the provided callback
  const fetchNotes = async () => {
    try {
      if (fetchNotesCallback) {
        await fetchNotesCallback();
      } else {
        // Default to fetching attached notes if no callback provided
        await fetchAttachedNotes();
      }
    } catch (err) {
      console.error("Error fetching notes:", err);
      setSaveError("Failed to load notes");
    }
  };

  // Fetch user's existing notes
  const fetchUserNotes = async () => {
    setLoadingExistingNotes(true);
    try {
      const response = await api.get('/notes');
      setExistingNotes(response.data);
    } catch (err) {
      console.error("Error fetching user notes:", err);
      setSaveError("Failed to load existing notes");
    } finally {
      setLoadingExistingNotes(false);
    }
  };

  // Fetch attached notes for this parent
  const fetchAttachedNotes = async () => {
    setLoadingAttachedNotes(true);
    try {
      if (parentType === 'video') {
        const response = await api.get(`/uservideos/${parentId}/notes`);
        setAttachedNotes(response.data);
      } else {
        // For playlist, we would need to implement
        const response = await api.get(`/notes?parentIds=${parentId}`);
        setAttachedNotes(response.data);
      }
    } catch (err) {
      console.error("Error fetching attached notes:", err);
      setSaveError("Failed to load attached notes");
    } finally {
      setLoadingAttachedNotes(false);
    }
  };

  // Handle note content changes
  const handleNoteChange = (newContent) => {
    setNoteDraft(newContent);
  };

  // Toggle preview mode
  const handleTogglePreview = (preview) => {
    setPreviewMode(preview);
  };

  // Handle dirty state changes
  const handleIsDirtyChange = (dirty) => {
    setIsDirty(dirty);
  };

  // Add existing notes
  const handleAddNotes = async () => {
    if (selectedNoteIds.size === 0) {
      setSaveError("Please select at least one note to add");
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
        // This is a simplified implementation
        console.warn("Playlist note attachment not fully implemented yet");
      }
      
      setShowAddNoteModal(false);
      setSelectedNoteIds(new Set());
      setSaveError("");
      
      // Refresh notes after adding
      if (fetchNotesCallback) {
        await fetchNotes();
      }
    } catch (err) {
      console.error("Error adding notes:", err);
      setSaveError("Failed to add notes");
    }
  };

  // Remove notes
  const handleRemoveNotes = async () => {
    if (selectedRemoveNoteIds.size === 0) {
      setSaveError("Please select at least one note to remove");
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
        // This is a simplified implementation
        console.warn("Playlist note removal not fully implemented yet");
      }
      
      setShowRemoveNoteModal(false);
      setSelectedRemoveNoteIds(new Set());
      setSaveError("");
      
      // Refresh notes after removing
      if (fetchNotesCallback) {
        await fetchNotes();
      }
    } catch (err) {
      console.error("Error removing notes:", err);
      setSaveError("Failed to remove notes");
    }
  };

  // Create a new note
  const handleCreateNote = async () => {
    if (!newNoteTitle.trim()) {
      setSaveError("Title is required");
      return;
    }

    try {
      const response = await api.post('/notes/create', {
        parentId: null,
        title: newNoteTitle,
        content: "", 
        isPdf: false,
        parentIds: [parentId]
      });
      
      setShowAddNoteModal(false);
      setNewNoteTitle("");
      setNewNoteContent("");
      setSaveError("");
      
      // Refresh notes after creating
      if (fetchNotesCallback) {
        await fetchNotes();
      }
    } catch (err) {
      console.error("Error creating note:", err);
      if (err.response) {
        setSaveError(`Failed to create note: ${err.response.data?.message || 'Unknown error'}`);
      } else {
        setSaveError("Failed to create note - network error");
      }
    }
  };

  // Save note using the provided callback
  const handleSave = async () => {
    if (saveState === "saving") return;
    
    setSaveState("saving");
    setSaveError("");
    
    try {
      if (saveNoteCallback) {
        await saveNoteCallback(noteDraft);
      } else {
        // Fallback to a simple save if no callback provided
        await api.put(`/notes/${parentId}`, {
          content: noteDraft
        });
      }
      setSaveState("saved");
      setIsDirty(false);
      
      // Reset save state after a delay
      setTimeout(() => setSaveState("idle"), 3000);
      
      // Refresh notes after saving
      if (fetchNotesCallback) {
        await fetchNotes();
      }
    } catch (err) {
      console.error("Error saving note:", err);
      setSaveError("Failed to save note");
      setSaveState("error");
    }
  };

  // Apply markdown formatting
  const applyFormat = (prefix, suffix = "", placeholder = "text") => {
    if (!textareaRef.current) return;

    const start = textareaRef.current.selectionStart;
    const end = textareaRef.current.selectionEnd;
    const selected = noteDraft.slice(start, end) || placeholder;
    const updated = noteDraft.slice(0, start) + prefix + selected + suffix + noteDraft.slice(end);

    setNoteDraft(updated);
    setIsDirty(true);
    
    // Focus back to textarea and set cursor position
    setTimeout(() => {
      if (textareaRef.current) {
        textareaRef.current.focus();
        const cursorStart = start + prefix.length;
        const cursorEnd = cursorStart + selected.length;
        textareaRef.current.setSelectionRange(cursorStart, cursorEnd);
      }
    }, 0);
  };

  // Expose all functions and state to components
  return {
    // State values
    noteDraft,
    previewMode,
    saveState,
    saveError,
    setSaveError,
    isDirty,
    notes,
    loadingNotes,
    showAddNoteModal,
    setShowAddNoteModal,
    showRemoveNoteModal,
    setShowRemoveNoteModal,
    activeTab,
    setActiveTab,
    existingNotes,
    loadingExistingNotes,
    selectedNoteIds,
    setSelectedNoteIds,
    newNoteTitle,
    setNewNoteTitle,
    newNoteContent,
    setNewNoteContent,
    attachedNotes,
    loadingAttachedNotes,
    selectedRemoveNoteIds,
    setSelectedRemoveNoteIds,
    textareaRef,
    playerRef,
    
    // Callback functions
    handleNoteChange,
    handleTogglePreview,
    handleIsDirtyChange,
    fetchNotes,
    fetchAttachedNotes,
    fetchUserNotes,
    handleAddNotes,
    handleRemoveNotes,
    handleCreateNote,
    applyFormat,
    handleSave,
    
    // State setters
    setSaveError,
    setSaveState,
    setPreviewMode,
    setLoadingNotes,
    setNoteDraft
  };
}