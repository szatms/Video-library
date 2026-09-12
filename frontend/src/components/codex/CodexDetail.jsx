import { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../services/api";
import CodexContentDisplay from "./CodexContentDisplay";
import PlaylistDetail from "../playlist/PlaylistDetail";
import VideoDetail from "../video/VideoDetail";

function CodexDetail() {
  const { codexId } = useParams();
  const navigate = useNavigate();
  const [codex, setCodex] = useState(null);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [notes, setNotes] = useState([]);
  const [selectedNote, setSelectedNote] = useState(null);
  const [noteContent, setNoteContent] = useState('');
  const [noteTitle, setNoteTitle] = useState('');
  const [loadingNotes, setLoadingNotes] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);
  const [saveState, setSaveState] = useState("idle");
  const [saveError, setSaveError] = useState("");
  const [isDirty, setIsDirty] = useState(false);
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [editTitleValue, setEditTitleValue] = useState("");
  // Add state for modals
  const [showAddModal, setShowAddModal] = useState(false);
  const [showRemoveModal, setShowRemoveModal] = useState(false);
  const [addSelectedItems, setAddSelectedItems] = useState([]);
  const [removeSelectedItems, setRemoveSelectedItems] = useState([]);
  const [addContent, setAddContent] = useState([]);
  const [removeContent, setRemoveContent] = useState([]);
  // State for handling navigation to specific content types
  const [viewingContent, setViewingContent] = useState(null);
  
  // Add CSS for card styling
  useEffect(() => {
    const style = document.createElement('style');
    style.textContent = `
      .clickable-card {
        transition: all 0.2s ease;
        border: 1px solid #dee2e6;
        background-color: #ffffff;
      }
      .clickable-card:hover {
        box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        border-color: #0d6efd;
      }
      .clickable-card.selected {
        border: 2px solid #0d6efd;
        background-color: #e7f1ff;
      }
      .modal-content {
        background-color: #ffffff;
        color: #333;
      }
      .modal-header {
        background-color: #ffffff;
        border-bottom: 1px solid #dee2e6;
      }
      .modal-footer {
        background-color: #ffffff;
        border-top: 1px solid #dee2e6;
      }
      .btn-close {
        filter: none;
      }
      .bg-light {
        background-color: #ffffff !important;
      }
      .text-dark {
        color: #333 !important;
      }
      .border-secondary {
        border-color: #dee2e6 !important;
      }
      .btn-outline-primary {
        color: #0d6efd;
        border-color: #0d6efd;
      }
      .btn-outline-primary:hover {
        background-color: #0d6efd;
        border-color: #0d6efd;
      }
    `;
    document.head.appendChild(style);
    return () => {
      document.head.removeChild(style);
    };
  }, []);
  const [addLoading, setAddLoading] = useState(false);
  const [removeLoading, setRemoveLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('videos');
  const [creatingNewNote, setCreatingNewNote] = useState(false);
  const [newNoteTitle, setNewNoteTitle] = useState('');

  // Function to handle title update
  const handleUpdateTitle = async () => {
    if (!codex || !editTitleValue.trim()) return;
    
    try {
      const payload = {
        codexId: codex.id,
        title: editTitleValue.trim()
      };
      
      await api.put('/codices/update', payload);
      
      // Update the local codex state
      setCodex(prev => ({ ...prev, title: editTitleValue.trim() }));
      setIsEditingTitle(false);
    } catch (err) {
      console.error("Error updating title:", err);
      // Optionally show an error message to the user
    }
  };

  useEffect(() => {
    const fetchCodexDetails = async () => {
      try {
        setLoading(true);
        setError(null);
        
        // Fetch codex details
        const codexResponse = await api.get(`/codices/${codexId}`);
        setCodex(codexResponse.data);
        setEditTitleValue(codexResponse.data.title);
        
        // Fetch items for this specific codex using the new endpoint
        const itemsResponse = await api.get(`/codices/${codexId}/content`);
        setItems(itemsResponse.data);

        // Fetch notes associated with this codex
        try {
          const noteResponse = await api.get(`/notes?parentIds=${codexId}`);
          setNotes(noteResponse.data);
        } catch (noteErr) {
          console.error("Error fetching notes:", noteErr);
        }
      } catch (err) {
        console.error("Error fetching codex details:", err);
        if (err.response?.status === 401) {
          // Redirect to login if unauthorized
          navigate("/");
        } else {
          setError("Could not load codex details.");
        }
      } finally {
        setLoading(false);
      }
    };

    if (codexId) {
      fetchCodexDetails();
    }
  }, [codexId, navigate]);

  // Fetch modal content when modals are opened
  useEffect(() => {
    if (showAddModal) {
      fetchAddContent(activeTab);
    }
  }, [showAddModal, activeTab]);

  // Reset note creation state when modal is closed
  useEffect(() => {
    if (!showAddModal) {
      setCreatingNewNote(false);
      setNewNoteTitle('');
    }
  }, [showAddModal]);

  useEffect(() => {
    if (showRemoveModal) {
      fetchRemoveContent();
    }
  }, [showRemoveModal]);

  const formatDuration = (durationSeconds) => {
    if (durationSeconds == null || Number.isNaN(durationSeconds)) {
      return null;
    }

    const totalSeconds = Math.max(0, Math.floor(durationSeconds));
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if (hours > 0) {
      return `${hours}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
    }

    return `${minutes}:${String(seconds).padStart(2, "0")}`;
  };

  const formatAddedAt = (addedAt) => {
    if (!addedAt) {
      return null;
    }

    const addedAtMs = new Date(addedAt).getTime();
    if (Number.isNaN(addedAtMs)) {
      return null;
    }

    const elapsedMs = Date.now() - addedAtMs;
    const elapsedMinutes = Math.max(0, Math.floor(elapsedMs / 60000));

    if (elapsedMinutes < 60) {
      return elapsedMinutes <= 1 ? "1 minute ago" : `${elapsedMinutes} minutes ago`;
    }

    const elapsedHours = Math.floor(elapsedMinutes / 60);
    if (elapsedHours < 24) {
      return elapsedHours === 1 ? "1 hour ago" : `${elapsedHours} hours ago`;
    }

    const elapsedDays = Math.floor(elapsedHours / 24);
    if (elapsedDays < 7) {
      return elapsedDays === 1 ? "1 day ago" : `${elapsedDays} days ago`;
    }

    const elapsedWeeks = Math.floor(elapsedDays / 7);
    return elapsedWeeks === 1 ? "1 week ago" : `${elapsedWeeks} weeks ago`;
  };

  const handleItemClick = (itemId, contentType) => {
    // Check if this is a NOTE item
    const item = items.find(i => i.id === itemId);
    if (item && item.contentType === 'NOTE') {
      // For NOTE items, treat them as notes and load them into the editor
      // Clear any existing selectedItem to make sure we're in note mode
      setSelectedItem(null);
      handleNoteSelect(item);
    } else {
      // For non-note items, navigate to the appropriate detail page
      if (item) {
        if (item.contentType === 'VIDEO') {
          // Navigate to the correct path for video detail (matching existing route)
          navigate(`/home/codices/${codexId}/video/${item.id}`);
        } else if (item.contentType === 'PLAYLIST') {
          navigate(`/home/codices/${codexId}/playlist/${item.id}`);
        } else {
          // For other content types, use the existing behavior
          setSelectedNote(null);
          setSelectedItem(item);
        }
      }
    }
  };

  const toggleWatchedStatus = async (itemId, currentWatchedStatus, contentType) => {
    // Only allow toggling watched status for videos and playlists
    if (contentType === 'NOTE') {
      return;
    }
    
    try {
      const res = await api.patch(`/uservideos/${itemId}`, {
        watched: !currentWatchedStatus
      });

      // Update the local items array to reflect the change
      setItems(prevItems => 
        prevItems.map(item => 
          item.id === itemId ? { ...item, watched: !currentWatchedStatus } : item
        )
      );
    } catch (err) {
      console.error("Error toggling watched status:", err);
    }
  };

  const handleNoteSelect = (note) => {
    // If clicking the same note again, clear the selection
    if (selectedNote && selectedNote.id === note.id) {
      setSelectedNote(null);
      setNoteTitle('');
      setNoteContent('');
    } else {
      setSelectedNote(note);
      setNoteTitle(note.title);
      // Handle the note content field - it's in noteContent for items from /content endpoint
      setNoteContent(note.noteContent || note.content || '');
    }
  };

  const handleSaveNote = async () => {
    if (!selectedNote) return;

    try {
      // Send only the fields needed for update
      const noteData = {
        title: noteTitle,
        content: noteContent,
        isPdf: false
      };

      const response = await api.put(`/notes/${selectedNote.id}`, noteData);

      // Update the note in the list
      setNotes(prev => prev.map(note =>
        note.id === selectedNote.id ? response.data : note
      ));
      setSelectedNote(response.data);
      setSaveState("saved");
      setIsDirty(false);
    } catch (err) {
      console.error("NOTE SAVE ERROR:", err);
      setSaveError("Could not save note.");
      setSaveState("error");
    }
  };

  const handleCreateNote = async () => {
    try {
      const noteData = {
        title: 'New Note',
        content: '',
        parentIds: [codexId],
        isPdf: false
      };

      const response = await api.post(`/notes/create`, noteData);

      // Add the new note to the list
      setNotes(prev => [response.data, ...prev]);
      setSelectedNote(response.data);
      setNoteTitle(response.data.title);
      setNoteContent('');
      setSaveState("saved");
      setIsDirty(false);
    } catch (err) {
      console.error("CREATE NOTE ERROR:", err);
      setSaveError("Could not create note.");
      setSaveState("error");
    }
  };

  const handleDeleteNote = async (noteId) => {
    try {
      await api.delete(`/notes/delete/${noteId}`);
      // Remove the note from the list
      setNotes(prev => prev.filter(note => note.id !== noteId));
      // If we're deleting the currently selected note, clear the selection
      if (selectedNote && selectedNote.id === noteId) {
        setSelectedNote(null);
        setNoteTitle('');
        setNoteContent('');
      }
      setSaveState("saved");
      setIsDirty(false);
    } catch (err) {
      console.error("DELETE NOTE ERROR:", err);
      setSaveError("Could not delete note.");
      setSaveState("error");
    }
  };

  const handleCreateNoteFromModal = async () => {
    if (!newNoteTitle.trim()) return;
    
    try {
      const noteData = {
        title: newNoteTitle.trim(),
        content: '',
        parentIds: [codexId],
        isPdf: false
      };

      const response = await api.post(`/notes/create`, noteData);

      // Add the new note to the list
      setNotes(prev => [response.data, ...prev]);
      
      // Add the new note to addContent so it appears in the modal
      setAddContent(prev => [...prev, {
        id: response.data.id,
        title: response.data.title,
        thumbnailUrl: '',
        channelTitle: '',
        viewCount: null,
        videoCount: null,
        contentType: 'NOTE'
      }]);
      
      // Reset the new note creation state
      setCreatingNewNote(false);
      setNewNoteTitle('');
      
      // Select the newly created note if needed
      setAddSelectedItems(prev => [...prev, response.data.id]);
    } catch (err) {
      console.error("CREATE NOTE ERROR:", err);
      setSaveError("Could not create note.");
      setSaveState("error");
    }
  };

  // Function to fetch content for add/remove modals
  const fetchAddContent = async (contentType) => {
    try {
      setAddLoading(true);
      let response;
      if (contentType === 'videos') {
        response = await api.get('/uservideos');
      } else if (contentType === 'playlists') {
        response = await api.get('/userplaylists');
      } else if (contentType === 'notes') {
        response = await api.get('/notes');
      }
      
      // Make sure we're getting the right data structure
      if (response && response.data) {
        let data = response.data;
        
        // Handle nested data structures for videos and playlists
        if (Array.isArray(data)) {
          // For videos, we need to extract the video info from the nested structure
          if (contentType === 'videos') {
            data = data.map(item => {
              // Try multiple possible structures
              const videoData = item.video || item;
              const processedItem = {
                id: item.id || videoData.id,
                title: videoData.title || item.title || 'Untitled',
                thumbnailUrl: videoData.thumbnailUrl || item.thumbnailUrl,
                channelTitle: videoData.channelTitle || item.channelTitle,
                viewCount: videoData.stats?.viewCount || item.viewCount,
                videoCount: videoData.stats?.videoCount || item.videoCount,
                contentType: 'VIDEO'
              };
              // Add fallbacks for missing data
              if (!processedItem.title) processedItem.title = 'Untitled Video';
              if (!processedItem.thumbnailUrl) processedItem.thumbnailUrl = '';
              return processedItem;
            });
          }
          // For playlists, we need to extract the playlist info from the nested structure
          else if (contentType === 'playlists') {
            data = data.map(item => {
              // Try multiple possible structures
              const playlistData = item.playlist || item;
              const processedItem = {
                id: item.id || playlistData.id,
                title: playlistData.title || item.title || 'Untitled Playlist',
                thumbnailUrl: playlistData.thumbnailUrl || item.thumbnailUrl,
                channelTitle: playlistData.channelTitle || item.channelTitle,
                videoCount: playlistData.videoCount || item.videoCount,
                contentType: 'PLAYLIST'
              };
              // Add fallbacks for missing data
              if (!processedItem.title) processedItem.title = 'Untitled Playlist';
              if (!processedItem.thumbnailUrl) processedItem.thumbnailUrl = '';
              return processedItem;
            });
          }
          // For notes, the structure should be simpler
          else if (contentType === 'notes') {
            data = data.map(item => ({
              id: item.id,
              title: item.title || 'Untitled Note',
              thumbnailUrl: '', // Notes don't have thumbnails
              channelTitle: '', // Notes don't have channel titles
              viewCount: null, // Notes don't have view counts
              videoCount: null, // Notes don't have video counts
              contentType: 'NOTE'
            }));
          }
        }
        
        setAddContent(data);
      }
    } catch (err) {
      console.error("Error fetching add content:", err);
    } finally {
      setAddLoading(false);
    }
  };

  const fetchRemoveContent = async () => {
    try {
      setRemoveLoading(true);
      const response = await api.get(`/codices/${codexId}/content`);
      
      // Make sure we're getting the right data structure
      if (response && response.data) {
        // For content from codex, it should be already in the proper format
        setRemoveContent(response.data);
      }
    } catch (err) {
      console.error("Error fetching remove content:", err);
    } finally {
      setRemoveLoading(false);
    }
  };

  // Function to add content to codex
  const handleAddContent = async () => {
    try {
      if (addSelectedItems.length === 0) return;
      
      const payload = {
        contentIds: addSelectedItems,
        codexId: codexId
      };
      
      await api.post('/codices/add', payload);
      
      // Refresh the items list
      const itemsResponse = await api.get(`/codices/${codexId}/content`);
      setItems(itemsResponse.data);
      
      // Close modal and reset
      setShowAddModal(false);
      setAddSelectedItems([]);
    } catch (err) {
      console.error("Error adding content:", err);
    }
  };

  // Function to remove content from codex
  const handleRemoveContent = async () => {
    try {
      if (removeSelectedItems.length === 0) return;
      
      const payload = {
        contentIds: removeSelectedItems,
        codexId: codexId
      };
      
      await api.put('/codices/remove', payload);
      
      // Refresh the items list
      const itemsResponse = await api.get(`/codices/${codexId}/content`);
      setItems(itemsResponse.data);
      
      // Close modal and reset
      setShowRemoveModal(false);
      setRemoveSelectedItems([]);
    } catch (err) {
      console.error("Error removing content:", err);
    }
  };

  const handleNoteChange = (newNote) => {
    setNoteContent(newNote);
    setIsDirty(true);
    setSaveState("idle");
    setSaveError("");
  };

  if (loading) {
    return <div className="p-3">Loading codex...</div>;
  }

  if (error) {
    return <div className="p-3 text-danger">{error}</div>;
  }

  if (!codex) {
    return <div className="p-3">Codex not found.</div>;
  }

  // Handle direct navigation to content items
  if (viewingContent) {
    if (viewingContent.type === 'video') {
      return (
        <div className="h-100 overflow-hidden">
          <VideoDetail 
            userVideoId={viewingContent.id} 
            onBack={() => setViewingContent(null)} 
          />
        </div>
      );
    } else if (viewingContent.type === 'playlist') {
      return (
        <div className="h-100 overflow-hidden">
          <PlaylistDetail 
            playlistId={viewingContent.id} 
            onBack={() => setViewingContent(null)} 
          />
        </div>
      );
    }
  }

  return (
    <div className="d-flex h-100 overflow-hidden">
      {/* LEFT PANEL - CONTENT LIST */}
      <div className="flex-grow-1 d-flex flex-column min-w-0">
        {/* BACK BUTTON */}
        <div className="mb-3">
          <button 
            className="btn btn-outline-primary" 
            onClick={() => navigate(-1)}
          >
            ← Back
          </button>
        </div>

        {/* CODEX HEADER */}
        <div className="d-flex gap-3 mb-4">
          <div className="d-flex flex-column align-items-center justify-content-center bg-secondary rounded" style={{ width: '120px', height: '120px' }}>
            <i className="bi bi-book" style={{ fontSize: '2.5rem' }}></i>
          </div>
          <div className="flex-grow-1">
            {isEditingTitle ? (
              <div className="d-flex align-items-center gap-2 mb-2">
                <input
                  type="text"
                  className="form-control"
                  value={editTitleValue}
                  onChange={(e) => setEditTitleValue(e.target.value)}
                  onBlur={handleUpdateTitle}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      handleUpdateTitle();
                    } else if (e.key === 'Escape') {
                      setIsEditingTitle(false);
                      setEditTitleValue(codex.title);
                    }
                  }}
                  autoFocus
                />
                <button 
                  className="btn btn-success btn-sm"
                  onClick={handleUpdateTitle}
                >
                  Save
                </button>
                <button 
                  className="btn btn-outline-secondary btn-sm"
                  onClick={() => {
                    setIsEditingTitle(false);
                    setEditTitleValue(codex.title);
                  }}
                >
                  Cancel
                </button>
              </div>
            ) : (
              <div className="d-flex align-items-center gap-2 mb-2">
                <h2 className="fw-bold">{codex.title}</h2>
                <button 
                  className="btn btn-outline-primary btn-sm"
                  onClick={() => {
                    setIsEditingTitle(true);
                    setEditTitleValue(codex.title);
                  }}
                >
                  <i className="bi bi-pencil"></i>
                </button>
              </div>
            )}
            <div className="d-flex flex-wrap gap-3 mt-2 text-muted small">
              <span>Added: {formatAddedAt(codex.addedAt)}</span>
              <span>{items.length} items</span>
              <div className="d-flex gap-2">
                <button 
                  className="btn btn-outline-success btn-sm"
                  onClick={() => {
                    setShowAddModal(true);
                    setAddSelectedItems([]);
                  }}
                >
                  +
                </button>
                <button 
                  className="btn btn-outline-danger btn-sm"
                  onClick={() => {
                    setShowRemoveModal(true);
                    setRemoveSelectedItems([]);
                  }}
                >
                  -
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* ITEM LIST */}
        <div className="flex-grow-1 overflow-auto" style={{ maxHeight: 'calc(100vh - 250px)' }}>
          {items.length === 0 ? (
            <div className="text-muted">No items in this codex.</div>
          ) : (
            items.map((item) => (
                <div
                  key={item.id}
                  className={`video-list-card d-flex gap-3 align-items-start justify-content-between mb-3 ${selectedItem?.id === item.id ? 'selected' : ''}`}
                  onClick={() => handleItemClick(item.id, item.contentType)}
                >
                <div className="d-flex gap-3 align-items-start min-w-0 flex-grow-1">
                  {item.thumbnailUrl ? (
                    <img
                      src={item.thumbnailUrl}
                      alt={item.title}
                      className="video-list-thumbnail"
                      style={{ width: '80px', height: '80px', objectFit: 'cover' }}
                    />
                  ) : (
                    <div className="d-flex flex-column align-items-center justify-content-center bg-secondary rounded" style={{ width: '80px', height: '80px' }}>
                      {item.contentType === 'NOTE' ? (
                        <i className="bi bi-file-earmark" style={{ fontSize: '2rem' }}></i>
                      ) : item.contentType === 'PLAYLIST' ? (
                        <i className="bi bi-list" style={{ fontSize: '2rem' }}></i>
                      ) : (
                        <i className="bi bi-file-earmark" style={{ fontSize: '2rem' }}></i>
                      )}
                    </div>
                  )}
                  <div className="min-w-0">
                    <div className="fw-semibold">{item.title}</div>
                    {item.channelTitle && (
                      <div className="video-list-channel text-truncate">{item.channelTitle}</div>
                    )}
                    <div className="d-flex flex-wrap gap-3 mt-2 text-muted small">
                      {item.videoCount !== null && item.videoCount !== undefined && (
                        <span>{item.videoCount} videos</span>
                      )}
                      {item.viewCount !== null && item.viewCount !== undefined && (
                        <span>{item.viewCount} views</span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* RIGHT PANEL - NOTES OR CONTENT DISPLAY */}
      <div style={{ width: "50%", background: "transparent", position: "relative" }} className="p-3 d-flex flex-column flex-shrink-0 h-100 overflow-auto">
        {selectedItem ? (
          // Display content when an item is selected (but not for videos/playlists)
          <CodexContentDisplay 
            selectedItem={selectedItem} 
            codexId={codexId} 
            onBack={() => setSelectedItem(null)} 
          />
        ) : selectedNote ? (
          // Display note editor when a note is selected
          <div className="h-100 d-flex flex-column">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <button 
                className="btn btn-outline-primary btn-sm" 
                onClick={() => setSelectedNote(null)}
              >
                ← Back
              </button>
              <h5 className="mb-0">Edit Note</h5>
              <div></div> {/* Spacer for alignment */}
            </div>
            <input
              type="text"
              className="form-control form-control-lg mb-3 bg-dark text-white border-secondary"
              value={noteTitle}
              onChange={(e) => {
                setNoteTitle(e.target.value);
                setIsDirty(true);
                setSaveState("idle");
                setSaveError("");
              }}
              placeholder="Note title"
            />
            <textarea
              className="form-control flex-grow-1 bg-dark text-white border-secondary"
              rows="15"
              value={noteContent}
              onChange={(e) => handleNoteChange(e.target.value)}
              placeholder="Write your note content here..."
            />
            <div className="mt-3 d-flex justify-content-between align-items-center">
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
                onClick={handleSaveNote}
                disabled={!selectedNote || !noteTitle.trim() || !isDirty}
              >
                Save
              </button>
            </div>
          </div>
        ) : (
          // Empty state - nothing shown initially
          <div className="h-100 d-flex flex-column">
            <div className="d-flex flex-column align-items-center justify-content-center h-100 text-muted">
              <i className="bi bi-file-earmark" style={{ fontSize: "3rem", marginBottom: "1rem" }}></i>
              <p className="text-center">Select an item to view details</p>
            </div>
          </div>
        )}
      </div>

    {/* ADD CONTENT MODAL */}
    {showAddModal && (
      <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} onClick={() => setShowAddModal(false)}>
        <div className="modal-dialog modal-xl" onClick={(e) => e.stopPropagation()}>
          <div className="modal-content bg-light text-dark border-secondary">
            <div className="modal-header bg-light border-secondary">
              <h5 className="modal-title">Add Content to Codex</h5>
              <button type="button" className="btn-close" onClick={() => setShowAddModal(false)}></button>
            </div>
            <div className="modal-body">
              <ul className="nav nav-tabs mb-3">
                <li className="nav-item">
                  <button 
                    className={`nav-link ${activeTab === 'videos' ? 'active' : ''}`}
                    onClick={() => setActiveTab('videos')}
                  >
                    Videos
                  </button>
                </li>
                <li className="nav-item">
                  <button 
                    className={`nav-link ${activeTab === 'playlists' ? 'active' : ''}`}
                    onClick={() => setActiveTab('playlists')}
                  >
                    Playlists
                  </button>
                </li>
                <li className="nav-item">
                  <button 
                    className={`nav-link ${activeTab === 'notes' ? 'active' : ''}`}
                    onClick={() => setActiveTab('notes')}
                  >
                    Notes
                  </button>
                </li>
              </ul>
              
              {addLoading ? (
                <div className="text-center">Loading...</div>
              ) : (
                <div className="row">
                  {/* Always include "Add new note" card in notes tab */}
                  {activeTab === 'notes' && (
                    <div 
                      className="col-md-6 mb-3 cursor-pointer"
                      onClick={() => {
                        if (!creatingNewNote) {
                          setCreatingNewNote(true);
                          setNewNoteTitle('');
                        }
                      }}
                    >
                      <div className="card clickable-card bg-light text-dark border-secondary">
                        <div className="card-body">
                          <div className="d-flex align-items-center justify-content-center">
                            <i className="bi bi-plus-lg" style={{ fontSize: '2rem' }}></i>
                            <h6 className="card-title mb-0 ms-2">Add new note</h6>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                  {creatingNewNote && (
                    <div className="col-md-6 mb-3">
                      <div className="card clickable-card bg-light text-dark border-secondary">
                        <div className="card-body">
                          <div className="d-flex flex-column">
                            <input
                              type="text"
                              className="form-control mb-2"
                              placeholder="Note title"
                              value={newNoteTitle}
                              onChange={(e) => setNewNoteTitle(e.target.value)}
                              autoFocus
                            />
                            <div className="d-flex justify-content-end gap-2">
                              <button 
                                className="btn btn-outline-secondary btn-sm"
                                onClick={() => setCreatingNewNote(false)}
                              >
                                Cancel
                              </button>
                              <button 
                                className="btn btn-success btn-sm"
                                onClick={handleCreateNoteFromModal}
                                disabled={!newNoteTitle.trim()}
                              >
                                Create
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                  {addContent.length > 0 ? (
                    addContent.map((item) => {
                      // Ensure item has required properties to prevent blank cards
                      if (!item || !item.id) return null;
                      
                      return (
                        <div 
                          key={item.id}
                          className={`col-md-6 mb-3 cursor-pointer ${addSelectedItems.includes(item.id) ? 'selected' : ''}`}
                          onClick={() => {
                            if (addSelectedItems.includes(item.id)) {
                              setAddSelectedItems(addSelectedItems.filter(id => id !== item.id));
                            } else {
                              setAddSelectedItems([...addSelectedItems, item.id]);
                            }
                          }}
                        >
                          <div className={`card clickable-card ${addSelectedItems.includes(item.id) ? 'selected' : ''} bg-light text-dark border-secondary`}>
                            <div className="card-body">
                              <div className="d-flex align-items-start">
                                {item.thumbnailUrl ? (
                                  <img
                                    src={item.thumbnailUrl}
                                    alt={item.title}
                                    className="img-thumbnail me-2"
                                    style={{ width: '80px', height: 'auto' }}
                                  />
                                ) : (
                                  <div className="d-flex flex-column align-items-center justify-content-center bg-secondary rounded me-2" style={{ width: '80px', height: '80px' }}>
                                    {item.contentType === 'NOTE' ? (
                                      <i className="bi bi-file-earmark" style={{ fontSize: '2rem' }}></i>
                                    ) : item.contentType === 'PLAYLIST' ? (
                                      <i className="bi bi-list" style={{ fontSize: '2rem' }}></i>
                                    ) : (
                                      <i className="bi bi-file-earmark" style={{ fontSize: '2rem' }}></i>
                                    )}
                                  </div>
                                )}
                                <div>
                                  <h6 className="card-title mb-1">{item.title || 'Untitled'}</h6>
                                  {item.channelTitle && (
                                    <p className="card-text small text-muted mb-1">
                                      {item.channelTitle}
                                    </p>
                                  )}
                                  {item.videoCount !== null && item.videoCount !== undefined && (
                                    <p className="card-text small text-muted">
                                      {item.videoCount} videos
                                    </p>
                                  )}
                                  {item.viewCount !== null && item.viewCount !== undefined && (
                                    <p className="card-text small text-muted">
                                      {item.viewCount} views
                                    </p>
                                  )}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <div className="text-center text-muted">No content available</div>
                  )}
                </div>
              )}
            </div>
            <div className="modal-footer bg-light border-secondary">
              <button 
                type="button" 
                className="btn btn-secondary" 
                onClick={() => setShowAddModal(false)}
              >
                Close
              </button>
              <button 
                type="button" 
                className="btn btn-success" 
                onClick={handleAddContent}
                disabled={addSelectedItems.length === 0}
              >
                Add Selected
              </button>
            </div>
          </div>
        </div>
      </div>
    )}

    {/* REMOVE CONTENT MODAL */}
    {showRemoveModal && (
      <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} onClick={() => setShowRemoveModal(false)}>
        <div className="modal-dialog modal-xl" onClick={(e) => e.stopPropagation()}>
          <div className="modal-content bg-light text-dark border-secondary">
            <div className="modal-header bg-light border-secondary">
              <h5 className="modal-title">Remove Content from Codex</h5>
              <button type="button" className="btn-close" onClick={() => setShowRemoveModal(false)}></button>
            </div>
            <div className="modal-body">
              {removeLoading ? (
                <div className="text-center">Loading...</div>
              ) : (
                <div className="row">
                  {removeContent.length > 0 ? (
                    removeContent.map((item) => {
                      // Ensure item has required properties to prevent blank cards
                      if (!item || !item.id) return null;
                      
                      return (
                        <div 
                          key={item.id}
                          className={`col-md-6 mb-3 cursor-pointer ${removeSelectedItems.includes(item.id) ? 'selected' : ''}`}
                          onClick={() => {
                            if (removeSelectedItems.includes(item.id)) {
                              setRemoveSelectedItems(removeSelectedItems.filter(id => id !== item.id));
                            } else {
                              setRemoveSelectedItems([...removeSelectedItems, item.id]);
                            }
                          }}
                        >
                          <div className={`card clickable-card ${removeSelectedItems.includes(item.id) ? 'selected' : ''} bg-light text-dark border-secondary`}>
                            <div className="card-body">
                              <div className="d-flex align-items-start">
                                {item.thumbnailUrl ? (
                                  <img
                                    src={item.thumbnailUrl}
                                    alt={item.title}
                                    className="img-thumbnail me-2"
                                    style={{ width: '80px', height: 'auto' }}
                                  />
                                ) : (
                                  <div className="d-flex flex-column align-items-center justify-content-center bg-secondary rounded me-2" style={{ width: '80px', height: '80px' }}>
                                    {item.contentType === 'NOTE' ? (
                                      <i className="bi bi-file-earmark" style={{ fontSize: '2rem' }}></i>
                                    ) : item.contentType === 'PLAYLIST' ? (
                                      <i className="bi bi-list" style={{ fontSize: '2rem' }}></i>
                                    ) : (
                                      <i className="bi bi-file-earmark" style={{ fontSize: '2rem' }}></i>
                                    )}
                                  </div>
                                )}
                                <div>
                                  <h6 className="card-title mb-1">{item.title || 'Untitled'}</h6>
                                  {item.channelTitle && (
                                    <p className="card-text small text-muted mb-1">
                                      {item.channelTitle}
                                    </p>
                                  )}
                                  {item.videoCount !== null && item.videoCount !== undefined && (
                                    <p className="card-text small text-muted">
                                      {item.videoCount} videos
                                    </p>
                                  )}
                                  {item.viewCount !== null && item.viewCount !== undefined && (
                                    <p className="card-text small text-muted">
                                      {item.viewCount} views
                                    </p>
                                  )}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <div className="text-center text-muted">No content available</div>
                  )}
                </div>
              )}
            </div>
            <div className="modal-footer bg-light border-secondary">
              <button 
                type="button" 
                className="btn btn-secondary" 
                onClick={() => setShowRemoveModal(false)}
              >
                Close
              </button>
              <button 
                type="button" 
                className="btn btn-danger" 
                onClick={handleRemoveContent}
                disabled={removeSelectedItems.length === 0}
              >
                Remove Selected
              </button>
            </div>
          </div>
        </div>
      </div>
    )}
    </div>
  );
}

export default CodexDetail;