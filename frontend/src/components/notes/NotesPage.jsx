import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from "../../services/api";

const NotesPage = () => {
  const [notes, setNotes] = useState([]);
  const [selectedNote, setSelectedNote] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [noteContent, setNoteContent] = useState('');
  const [noteTitle, setNoteTitle] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [showParentSelector, setShowParentSelector] = useState(false);
  const [parentIds, setParentIds] = useState([]);
  const [isAddingToNote, setIsAddingToNote] = useState(false);
  const [videos, setVideos] = useState([]);
  const [playlists, setPlaylists] = useState([]);
  const [activeTab, setActiveTab] = useState('videos'); // 'videos' or 'playlists'
  const [loadingParents, setLoadingParents] = useState(false);
  const [showDetachModal, setShowDetachModal] = useState(false);
  const [showAttachedModal, setShowAttachedModal] = useState(false);
  const [detachParentIds, setDetachParentIds] = useState([]);
  const [filteredVideos, setFilteredVideos] = useState([]);
  const [filteredPlaylists, setFilteredPlaylists] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchNotes = async () => {
      try {
        const response = await api.get('/notes');
        setNotes(response.data);
        if (response.data.length > 0 && !selectedNote) {
          setSelectedNote(response.data[0]);
          setNoteTitle(response.data[0].title);
          setNoteContent(response.data[0].content || ''); // Load content if available
        }
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchNotes();
  }, []);

  const handleNoteSelect = (note) => {
    // If clicking the same note again, clear the selection
    if (selectedNote && selectedNote.id === note.id) {
      setSelectedNote(null);
      setNoteTitle('');
      setNoteContent('');
      setIsEditing(false);
    } else {
      setSelectedNote(note);
      setNoteTitle(note.title);
      setNoteContent(note.content || ''); // Load content if available
      setIsEditing(false);
    }
  };

  const handleCreateNote = async () => {
    // Show parent selector modal
    setIsAddingToNote(false);
    setShowParentSelector(true);
    setParentIds([]); // Reset parent IDs
    loadParentData(); // Load videos and playlists when modal opens
  };

  const handleAddNote = async () => {
    // Show parent selector modal for adding to existing note
    setIsAddingToNote(true);
    setShowParentSelector(true);
    setParentIds([]); // Reset parent IDs
    loadParentData(); // Load videos and playlists when modal opens
  };

  const loadParentData = async () => {
    setLoadingParents(true);
    try {
      // Fetch videos
      const videosResponse = await api.get('/uservideos');
      setVideos(videosResponse.data);

      // Fetch playlists
      const playlistsResponse = await api.get('/userplaylists');
      setPlaylists(playlistsResponse.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoadingParents(false);
    }
  };

  const createNoteWithParents = async (selectedParentIds) => {
    try {
      const response = await api.post('/notes/create', {
        parentId: null, // Send null instead of empty string
        title: noteTitle,
        content: noteContent,
        isPdf: false,
        parentIds: selectedParentIds
        // userId is automatically provided by authentication
      });

      // Add the new note to the list
      setNotes(prev => [response.data, ...prev]);
      setSelectedNote(response.data);
      setNoteTitle(response.data.title);
      setNoteContent('');
      setIsEditing(true);
    } catch (err) {
      setError(err.message);
    }
  };

  const addNoteToParents = async (selectedParentIds) => {
    try {
      // For adding parents to an existing note, we use the /add endpoint
      await api.post('/notes/add', {
        noteId: selectedNote.id,
        parentIds: selectedParentIds
      });

      // Update the note in the list to include the new parents
      setNotes(prev => prev.map(note =>
        note.id === selectedNote.id
          ? { ...note, parentIds: [...(note.parentIds || []), ...selectedParentIds] }
          : note
      ));

      // Update selected note with new parentIds
      setSelectedNote(prev => ({
        ...prev,
        parentIds: [...(prev.parentIds || []), ...selectedParentIds]
      }));

      // Close modal
      setShowParentSelector(false);
    } catch (err) {
      setError(err.message);
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
      setIsEditing(false);
    } catch (err) {
      setError(err.message);
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
    } catch (err) {
      setError(err.message);
    }
  };

  const handleParentSelect = (parentId) => {
    setParentIds(prev => {
      if (prev.includes(parentId)) {
        const newIds = prev.filter(id => id !== parentId);
        return newIds;
      } else {
        const newIds = [...prev, parentId];
        return newIds;
      }
    });
  };

  const handleDetachSelect = (parentId) => {
    setDetachParentIds(prev => {
      if (prev.includes(parentId)) {
        const newIds = prev.filter(id => id !== parentId);
        return newIds;
      } else {
        const newIds = [...prev, parentId];
        return newIds;
      }
    });
  };

  const handleDetachNoteClick = async () => {
    if (!selectedNote) return;

    // IMPORTANT: selection starts empty
    setDetachParentIds([]);
    setFilteredVideos([]);
    setFilteredPlaylists([]);
    setShowDetachModal(true);
    setLoadingParents(true);

    try {
      const response = await api.get(
        `/notes/${selectedNote.id}/parents`
      );

      const parents = response.data;

      // Process the parents data to separate videos and playlists
      const videos = [];
      const playlists = [];
      
      parents.forEach(parent => {
        if (parent.parentType === 'VIDEO') {
          videos.push({
            id: parent.id,
            video: {
              thumbnailUrl: parent.thumbnailUrl,
              title: parent.title,
              channelTitle: parent.channelTitle,
              viewCount: parent.viewCount
            },
            addedAt: new Date().toISOString()
          });
        } else if (parent.parentType === 'PLAYLIST') {
          playlists.push({
            id: parent.id,
            playlist: {
              thumbnailUrl: parent.thumbnailUrl,
              title: parent.title,
              channelTitle: parent.channelTitle,
              videoCount: parent.videoCount
            },
            addedAt: new Date().toISOString()
          });
        }
      });

      setFilteredVideos(videos);
      setFilteredPlaylists(playlists);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoadingParents(false);
    }
  };

  const handleShowAttachedClick = async () => {
    if (!selectedNote) return;

    setShowAttachedModal(true);
    setLoadingParents(true);

    try {
      const response = await api.get(
        `/notes/${selectedNote.id}/parents`
      );

      const parents = response.data;

      // Process the parents data to separate videos and playlists
      const videos = [];
      const playlists = [];
      
      parents.forEach(parent => {
        if (parent.parentType === 'VIDEO') {
          videos.push({
            id: parent.id,
            video: {
              thumbnailUrl: parent.thumbnailUrl,
              title: parent.title,
              channelTitle: parent.channelTitle,
              viewCount: parent.viewCount
            },
            addedAt: new Date().toISOString()
          });
        } else if (parent.parentType === 'PLAYLIST') {
          playlists.push({
            id: parent.id,
            playlist: {
              thumbnailUrl: parent.thumbnailUrl,
              title: parent.title,
              channelTitle: parent.channelTitle,
              videoCount: parent.videoCount
            },
            addedAt: new Date().toISOString()
          });
        }
      });

      setFilteredVideos(videos);
      setFilteredPlaylists(playlists);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoadingParents(false);
    }
  };

  const handleDetachNote = async () => {
    if (!selectedNote || detachParentIds.length === 0) return;

    try {
      // Send detach request to backend with proper DTO structure
      await api.put('/notes/remove', {
        noteId: selectedNote.id,
        parentIds: detachParentIds
      });

      // Update the note in the list to remove detached parents
      setNotes(prev => prev.map(note =>
        note.id === selectedNote.id
          ? { ...note, parentIds: (note.parentIds || []).filter(id => !detachParentIds.includes(id)) }
          : note
      ));

      // Update selected note with new parentIds
      setSelectedNote(prev => ({
        ...prev,
        parentIds: (prev.parentIds || []).filter(id => !detachParentIds.includes(id))
      }));

      // Close modal and reset selection
      setShowDetachModal(false);
      setDetachParentIds([]);
    } catch (err) {
      setError(err.message);
      console.error('Error detaching note:', err);
    }
  };

  // Helper function to filter videos/playlists to only show those attached to current note
  const filterAttachedParents = (items, noteParentIds) => {
    if (!noteParentIds || noteParentIds.length === 0) return [];
    return items.filter(item => noteParentIds.includes(item.id));
  };

  if (loading) {
    return (
      <div className="container-fluid h-100 d-flex align-items-center justify-content-center">
        <div className="text-center">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-2">Loading notes...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container-fluid h-100 d-flex align-items-center justify-content-center">
        <div className="alert alert-danger">
          Error: {error}
        </div>
      </div>
    );
  }

  return (
    <div className="d-flex h-100" style={{ height: '100vh' }}>
      <style>
        {`
          .clickable-card {
            transition: all 0.2s ease;
            border: 1px solid #495057;
          }
          .clickable-card:hover {
            box-shadow: 0 4px 8px rgba(255,255,255,0.1);
          }
          .clickable-card.selected {
            border: 2px solid #6c757d;
            background-color: #6c757d;
          }
          .list-group-item.active {
            background-color: #6c757d;
            border-color: #6c757d;
          }
        `}
      </style>

      {/* Left side - Notes List - 50% width */}
      <div className="w-50 h-100 overflow-auto">
        <div className="p-3">
      {/* BACK BUTTON */}
      {location.pathname.startsWith("/home/notes") && !location.pathname.includes("/notes/") && (
        <div className="mb-3">
          <button 
            className="btn btn-outline-primary" 
            onClick={() => navigate("/home")}
          >
            ← Back
          </button>
        </div>
      )}
          <h3>Notes</h3>
          <div className="list-group">
            {notes.map((note) => (
              <div
                key={note.id}
                className={`list-group-item list-group-item-action d-flex justify-content-between align-items-center ${
                  selectedNote?.id === note.id ? 'active' : ''
                }`}
                onClick={() => handleNoteSelect(note)}
              >
                <div>
                  <div className="fw-bold">{note.title}</div>
                  <small className="text-muted">
                    Added: {new Date(note.addedAt).toLocaleDateString()}
                  </small>
                </div>
                <div className="d-flex align-items-center">
                  <button
                    className="btn btn-sm btn-success me-1"
                    onClick={(e) => {
                      e.stopPropagation();
                      api.get(`/notes/${note.id}/download`, { responseType: 'blob' })
                        .then(response => {
                          const url = window.URL.createObjectURL(new Blob([response.data]));
                          const link = document.createElement('a');
                          link.href = url;
                          link.setAttribute('download', `${note.title}.txt`);
                          document.body.appendChild(link);
                          link.click();
                          document.body.removeChild(link);
                          window.URL.revokeObjectURL(url);
                        })
                        .catch(error => {
                          console.error('Download failed:', error);
                          // Handle error appropriately
                          alert('Download failed. Please check your authentication status.');
                        });
                    }}
                  >
                    Download
                  </button>
                  <button
                    className="btn btn-sm btn-danger"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteNote(note.id);
                    }}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right side - Editor - 50% width */}
      <div className="w-50 h-100 d-flex flex-column">
        {/* Top controls - 20% height */}
        <div className="p-3" style={{ height: '20%' }}>
          <div className="row g-2 mb-2">
            <div className="col">
              <button
                className="btn btn-primary w-100"
                onClick={handleCreateNote}
              >
                Create Note
              </button>
            </div>
            <div className="col">
              <button
                className="btn btn-success w-100"
                onClick={handleSaveNote}
                disabled={!selectedNote || !noteTitle.trim()}
              >
                Save
              </button>
            </div>

          </div>
          <div className="row g-2">
            <div className="col">
              <button
                className="btn btn-secondary w-100"
                onClick={handleAddNote}
                disabled={!selectedNote}
              >
                Add to
              </button>
            </div>
            <div className="col">
              <button
                className="btn btn-secondary w-100"
                onClick={handleDetachNoteClick}
                disabled={!selectedNote}
              >
                Remove from
              </button>
            </div>
            <div className="col">
              <button
                className="btn btn-secondary w-100"
                onClick={handleShowAttachedClick}
                disabled={!selectedNote}
              >
                Show attached
              </button>
            </div>
          </div>
        </div>

        {/* Editor content - 80% height */}
        <div className="flex-grow-1 p-3 overflow-auto" style={{ height: '80%' }}>
          <div className="h-100 d-flex flex-column">
            <input
              type="text"
              className="form-control form-control-lg mb-3 bg-dark text-white border-secondary"
              value={noteTitle}
              onChange={(e) => setNoteTitle(e.target.value)}
              placeholder="Note title"
            />
            <textarea
              className="form-control flex-grow-1 bg-dark text-white border-secondary"
              rows="15"
              value={noteContent}
              onChange={(e) => setNoteContent(e.target.value)}
              placeholder="Write your note content here..."
            />
          </div>
        </div>
      </div>

      {/* Parent Selector Modal */}
      {showParentSelector && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-xl">
            <div className="modal-content bg-dark text-white border-secondary">
              <div className="modal-header bg-dark border-secondary">
                <h5 className="modal-title">Select Parents for Note</h5>
                <button type="button" className="btn-close btn-close-white" onClick={() => setShowParentSelector(false)}></button>
              </div>

              <div className="modal-body">
                <p>Select videos or playlists to be parents of this note:</p>

                {/* Tabs */}
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
                      className={`nav-link ${activeTab === 'inPlaylistVideos' ? 'active' : ''}`}
                      onClick={() => setActiveTab('inPlaylistVideos')}
                    >
                      In-playlist Videos
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
                </ul>

                {/* Loading indicator */}
                {loadingParents && (
                  <div className="text-center my-3">
                    <div className="spinner-border" role="status">
                      <span className="visually-hidden">Loading...</span>
                    </div>
                  </div>
                )}

                {/* Parent items list */}
                {!loadingParents && (
                  <div className="row">
                    {activeTab === 'videos' ? (
                      <div className="col-12">
                        {videos.length > 0 ? (
                          <div className="row">
                            {videos.filter(video => !video.inPlaylist).map((video) => (
                              <div key={video.id} className="col-md-6 mb-3">
                                <div
                                  className={`card clickable-card ${parentIds.includes(video.id) ? 'selected' : ''} bg-dark text-white border-secondary`}
                                  style={{ cursor: 'pointer' }}
                                  onClick={() => handleParentSelect(video.id)}
                                >
                                  <div className="card-body">
                                    <div className="d-flex align-items-start">
                                      <div className="form-check me-2">
                                        <input
                                          type="checkbox"
                                          className="form-check-input"
                                          id={`video-${video.id}`}
                                          checked={parentIds.includes(video.id)}
                                          onChange={() => handleParentSelect(video.id)}
                                        />
                                        <label className="form-check-label" htmlFor={`video-${video.id}`}>
                                        </label>
                                      </div>
                                      <img
                                        src={video.video.thumbnailUrl}
                                        alt={video.video.title}
                                        className="img-thumbnail me-2"
                                        style={{ width: '80px', height: 'auto' }}
                                      />
                                      <div>
                                        <h6 className="card-title mb-1">{video.video.title}</h6>
                                        <p className="card-text small text-muted mb-1">
                                          {video.video.channelTitle}
                                        </p>
                                        <p className="card-text small text-muted">
                                          Added: {new Date(video.addedAt).toLocaleDateString()}
                                        </p>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <p className="text-muted">No videos found.</p>
                        )}
                      </div>
                    ) : activeTab === 'inPlaylistVideos' ? (
                      <div className="col-12">
                        {videos.length > 0 ? (
                          <div className="row">
                            {videos.filter(video => video.inPlaylist).map((video) => (
                              <div key={video.id} className="col-md-6 mb-3">
                                <div
                                  className={`card clickable-card ${parentIds.includes(video.id) ? 'selected' : ''} bg-dark text-white border-secondary`}
                                  style={{ cursor: 'pointer' }}
                                  onClick={() => handleParentSelect(video.id)}
                                >
                                  <div className="card-body">
                                    <div className="d-flex align-items-start">
                                      <div className="form-check me-2">
                                        <input
                                          type="checkbox"
                                          className="form-check-input"
                                          id={`video-${video.id}`}
                                          checked={parentIds.includes(video.id)}
                                          onChange={() => handleParentSelect(video.id)}
                                        />
                                        <label className="form-check-label" htmlFor={`video-${video.id}`}>
                                        </label>
                                      </div>
                                      <img
                                        src={video.video.thumbnailUrl}
                                        alt={video.video.title}
                                        className="img-thumbnail me-2"
                                        style={{ width: '80px', height: 'auto' }}
                                      />
                                      <div>
                                        <h6 className="card-title mb-1">{video.video.title}</h6>
                                        <p className="card-text small text-muted mb-1">
                                          {video.video.channelTitle}
                                        </p>
                                        <p className="card-text small text-muted">
                                          Added: {new Date(video.addedAt).toLocaleDateString()}
                                        </p>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <p className="text-muted">No in-playlist videos found.</p>
                        )}
                      </div>
                    ) : (
                      <div className="col-12">
                        {playlists.length > 0 ? (
                          <div className="row">
                            {playlists.map((playlist) => (
                              <div key={playlist.id} className="col-md-6 mb-3">
                                <div
                                  className={`card clickable-card ${parentIds.includes(playlist.id) ? 'selected' : ''} bg-dark text-white border-secondary`}
                                  style={{ cursor: 'pointer' }}
                                  onClick={() => handleParentSelect(playlist.id)}
                                >
                                  <div className="card-body">
                                    <div className="d-flex align-items-start">
                                      <div className="form-check me-2">
                                        <input
                                          type="checkbox"
                                          className="form-check-input"
                                          id={`playlist-${playlist.id}`}
                                          checked={parentIds.includes(playlist.id)}
                                          onChange={() => handleParentSelect(playlist.id)}
                                        />
                                        <label className="form-check-label" htmlFor={`playlist-${playlist.id}`}>
                                        </label>
                                      </div>
                                      <img
                                        src={playlist.playlist.thumbnailUrl}
                                        alt={playlist.playlist.title}
                                        className="img-thumbnail me-2"
                                        style={{ width: '80px', height: 'auto' }}
                                      />
                                      <div>
                                        <h6 className="card-title mb-1">{playlist.playlist.title}</h6>
                                        <p className="card-text small text-muted mb-1">
                                          {playlist.playlist.channelTitle}
                                        </p>
                                        <p className="card-text small text-muted">
                                          Added: {new Date(playlist.addedAt).toLocaleDateString()}
                                        </p>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <p className="text-muted">No playlists found.</p>
                        )}
                      </div>
                    )}
                  </div>
                )}
              </div>

              <div className="modal-footer bg-dark border-secondary">
                <button type="button" className="btn btn-secondary" onClick={() => {
                  setShowParentSelector(false);
                  setIsAddingToNote(false);
                }}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => {
                    // Filter out any null values
                    const validParentIds = parentIds.filter(id => id !== null && id !== undefined && id !== '');

                    if (isAddingToNote) {
                      addNoteToParents(validParentIds);
                    } else {
                      createNoteWithParents(validParentIds);
                      setShowParentSelector(false);
                    }
                  }}
                  disabled={false}
                >
                  {isAddingToNote ? 'Add to Note' : 'Create Note'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Detach Parents Modal */}
      {showDetachModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-xl">
            <div className="modal-content bg-dark text-white border-secondary">
              <div className="modal-header bg-dark border-secondary">
                <h5 className="modal-title">Remove Parents from Note</h5>
                <button type="button" className="btn-close btn-close-white" onClick={() => setShowDetachModal(false)}></button>
              </div>

              <div className="modal-body">
                <p>Select parents to remove from this note:</p>

                {/* Loading indicator */}
                {loadingParents && (
                  <div className="text-center my-3">
                    <div className="spinner-border" role="status">
                      <span className="visually-hidden">Loading...</span>
                    </div>
                  </div>
                )}

                {/* Parent items list */}
                {!loadingParents && (
                  <div className="row">
                    <div className="col-12">
                      {filteredVideos.length > 0 || filteredPlaylists.length > 0 ? (
                        <div className="row">
                          {/* Videos */}
                          {filteredVideos.map((video) => (
                            <div key={video.id} className="col-md-6 mb-3">
                              <div
                                className={`card clickable-card ${detachParentIds.includes(video.id) ? 'selected' : ''} bg-dark text-white border-secondary`}
                                style={{ cursor: 'pointer' }}
                                onClick={() => handleDetachSelect(video.id)}
                              >
                                <div className="card-body">
                                  <div className="d-flex align-items-start">
                                    <div className="form-check me-2">
                                      <input
                                        type="checkbox"
                                        className="form-check-input"
                                        id={`detach-video-${video.id}`}
                                        checked={detachParentIds.includes(video.id)}
                                        onChange={() => handleDetachSelect(video.id)}
                                      />
                                      <label className="form-check-label" htmlFor={`detach-video-${video.id}`}>
                                      </label>
                                    </div>
                                    <img
                                      src={video.video.thumbnailUrl}
                                      alt={video.video.title}
                                      className="img-thumbnail me-2"
                                      style={{ width: '80px', height: 'auto' }}
                                    />
                                    <div>
                                      <h6 className="card-title mb-1">{video.video.title}</h6>
                                      <p className="card-text small text-muted mb-1">
                                        {video.video.channelTitle}
                                      </p>
                                      <p className="card-text small text-muted">
                                        Added: {new Date(video.addedAt).toLocaleDateString()}
                                      </p>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          ))}
                          
                          {/* Playlists */}
                          {filteredPlaylists.map((playlist) => (
                            <div key={playlist.id} className="col-md-6 mb-3">
                              <div
                                className={`card clickable-card ${detachParentIds.includes(playlist.id) ? 'selected' : ''} bg-dark text-white border-secondary`}
                                style={{ cursor: 'pointer' }}
                                onClick={() => handleDetachSelect(playlist.id)}
                              >
                                <div className="card-body">
                                  <div className="d-flex align-items-start">
                                    <div className="form-check me-2">
                                      <input
                                        type="checkbox"
                                        className="form-check-input"
                                        id={`detach-playlist-${playlist.id}`}
                                        checked={detachParentIds.includes(playlist.id)}
                                        onChange={() => handleDetachSelect(playlist.id)}
                                      />
                                      <label className="form-check-label" htmlFor={`detach-playlist-${playlist.id}`}>
                                      </label>
                                    </div>
                                    <img
                                      src={playlist.playlist.thumbnailUrl}
                                      alt={playlist.playlist.title}
                                      className="img-thumbnail me-2"
                                      style={{ width: '80px', height: 'auto' }}
                                    />
                                    <div>
                                      <h6 className="card-title mb-1">{playlist.playlist.title}</h6>
                                      <p className="card-text small text-muted mb-1">
                                        {playlist.playlist.channelTitle}
                                      </p>
                                      <p className="card-text small text-muted">
                                        Added: {new Date(playlist.addedAt).toLocaleDateString()}
                                      </p>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-muted">No parents found for this note.</p>
                      )}
                    </div>
                  </div>
                )}
              </div>

              <div className="modal-footer bg-dark border-secondary">
                <button type="button" className="btn btn-secondary" onClick={() => setShowDetachModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={handleDetachNote}
                  disabled={detachParentIds.length === 0}
                >
                  Remove Selected
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Show Attached Parents Modal */}
      {showAttachedModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-xl">
            <div className="modal-content bg-dark text-white border-secondary">
              <div className="modal-header bg-dark border-secondary">
                <h5 className="modal-title">Attached Parents</h5>
                <button type="button" className="btn-close btn-close-white" onClick={() => setShowAttachedModal(false)}></button>
              </div>

              <div className="modal-body">
                <p>These are the parents currently attached to this note:</p>

                {/* Loading indicator */}
                {loadingParents && (
                  <div className="text-center my-3">
                    <div className="spinner-border" role="status">
                      <span className="visually-hidden">Loading...</span>
                    </div>
                  </div>
                )}

                {/* Parent items list */}
                {!loadingParents && (
                  <div className="row">
                    <div className="col-12">
                      {filteredVideos.length > 0 || filteredPlaylists.length > 0 ? (
                        <div className="row">
                          {/* Videos */}
                          {filteredVideos.map((video) => (
                            <div key={video.id} className="col-md-6 mb-3">
                              <div className="card bg-dark text-white border-secondary">
                                <div className="card-body">
                                  <div className="d-flex align-items-start">
                                    <img
                                      src={video.video.thumbnailUrl}
                                      alt={video.video.title}
                                      className="img-thumbnail me-2"
                                      style={{ width: '80px', height: 'auto' }}
                                    />
                                    <div>
                                      <h6 className="card-title mb-1">{video.video.title}</h6>
                                      <p className="card-text small text-muted mb-1">
                                        {video.video.channelTitle}
                                      </p>
                                      <p className="card-text small text-muted">
                                        Added: {new Date(video.addedAt).toLocaleDateString()}
                                      </p>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          ))}
                          
                          {/* Playlists */}
                          {filteredPlaylists.map((playlist) => (
                            <div key={playlist.id} className="col-md-6 mb-3">
                              <div className="card bg-dark text-white border-secondary">
                                <div className="card-body">
                                  <div className="d-flex align-items-start">
                                    <img
                                      src={playlist.playlist.thumbnailUrl}
                                      alt={playlist.playlist.title}
                                      className="img-thumbnail me-2"
                                      style={{ width: '80px', height: 'auto' }}
                                    />
                                    <div>
                                      <h6 className="card-title mb-1">{playlist.playlist.title}</h6>
                                      <p className="card-text small text-muted mb-1">
                                        {playlist.playlist.channelTitle}
                                      </p>
                                      <p className="card-text small text-muted">
                                        Added: {new Date(playlist.addedAt).toLocaleDateString()}
                                      </p>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-muted">No parents are currently attached to this note.</p>
                      )}
                    </div>
                  </div>
                )}
              </div>

              <div className="modal-footer bg-dark border-secondary">
                <button type="button" className="btn btn-secondary" onClick={() => setShowAttachedModal(false)}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}


    </div>
  );
};

export default NotesPage;