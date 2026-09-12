import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../../services/api";

// CSS styles for clickable cards
const cardStyles = `
  .clickable-card {
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .clickable-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  }

  .clickable-card.selected {
    border: 2px solid #007bff;
    background-color: #e3f2fd;
  }

  .clickable-card .form-check-input {
    margin-top: 0.5rem;
  }

  .clickable-card .form-check {
    margin-top: 0.5rem;
  }
`;

const SORT_OPTIONS = [
  { value: "ADDED_AT", label: "Date added" },
  { value: "NAME", label: "Name" },
];

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

const formatContentNumber = (count) => {
  if (count === null || count === undefined || count < 0) {
    return null;
  }
  return `${count} items`;
};

const toSummaryItem = (codex) => ({
  id: codex.id || codex._id,
  addedAt: codex.addedAt,
  title: codex.title,
  contentNumber: codex.contentNumber,
  updatedAt: codex.updatedAt,
  userId: codex.userId,
});

function Codices() {
  const navigate = useNavigate();
  const location = useLocation();
  const [codices, setCodices] = useState([]);
  const [input, setInput] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState(null);
  const [sortMenuOpen, setSortMenuOpen] = useState(false);
  const [sortBy, setSortBy] = useState("ADDED_AT");
  const [direction, setDirection] = useState("DESC");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [activeTab, setActiveTab] = useState("videos");
  const [videos, setVideos] = useState([]);
  const [playlists, setPlaylists] = useState([]);
  const [notes, setNotes] = useState([]);
  const [loadingContent, setLoadingContent] = useState(false);
  const sortMenuRef = useRef(null);

  const activeSortLabel = useMemo(
    () => SORT_OPTIONS.find((option) => option.value === sortBy)?.label ?? "Date added",
    [sortBy]
  );

  const loadCodices = useCallback(async (signal) => {
    try {
      const res = await api.get("/codices", {
        signal,
        params: {
          sortBy,
          direction,
        },
      });
      setCodices(res.data);
      setError("");
    } catch (err) {
      if (err.code === "ERR_CANCELED") {
        return;
      }

      console.error("CODICES LIST ERROR:", err);
      setError("Could not load your codices.");
    } finally {
      if (!signal?.aborted) {
        setLoading(false);
      }
    }
  }, [direction, sortBy]);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);

    loadCodices(controller.signal);

    return () => {
      controller.abort();
    };
  }, [loadCodices]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (sortMenuRef.current && !sortMenuRef.current.contains(event.target)) {
        setSortMenuOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const handleAdd = async () => {
    setError("");

    if (input.trim() === "") {
      setError("Enter a title for your codex.");
      return;
    }

    try {
      const res = await api.post("/codices/create", { title: input });
      const createdCodex = toSummaryItem(res.data);
      setCodices((current) => [createdCodex, ...current]);
      setInput("");
      await loadCodices();
    } catch (err) {
      console.error("CODICES ADD ERROR:", err);
      setError("Could not add the codex.");
    }
  };

  const handleDelete = async (event, id) => {
    event.stopPropagation();
    setError("");
    setDeletingId(id);

    try {
      await api.delete(`/codices/delete/${id}`);
      setCodices((current) => current.filter((codex) => codex.id !== id));
      await loadCodices();
    } catch (err) {
      console.error("CODICES DELETE ERROR:", err);
      setError("Could not delete the codex.");
    } finally {
      setDeletingId(null);
    }
  };

  const loadContent = useCallback(async () => {
    if (!showCreateModal) return;
    
    setLoadingContent(true);
    
    try {
      // Load videos (using UserVideoController endpoint)
      const videosRes = await api.get("/uservideos");
      setVideos(videosRes.data);
      
      // Load playlists (using UserPlaylistController endpoint)
      const playlistsRes = await api.get("/userplaylists");
      setPlaylists(playlistsRes.data);
      
      // Load notes (using NoteController endpoint)
      const notesRes = await api.get("/notes");
      setNotes(notesRes.data);
    } catch (err) {
      console.error("Error loading content:", err);
    } finally {
      setLoadingContent(false);
    }
  }, [showCreateModal]);

  const [selectedItems, setSelectedItems] = useState([]);
  const [modalTitle, setModalTitle] = useState("");

  useEffect(() => {
    if (showCreateModal) {
      loadContent();
    }
  }, [showCreateModal, loadContent]);

  // Close modal when navigating away
  useEffect(() => {
    return () => {
      if (showCreateModal) {
        setShowCreateModal(false);
      }
    };
  }, [showCreateModal]);

  // Close modal on Escape key
  useEffect(() => {
    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setShowCreateModal(false);
      }
    };

    if (showCreateModal) {
      document.addEventListener('keydown', handleEscape);
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
    };
  }, [showCreateModal]);

  // Close modal when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      const modal = document.querySelector('.modal.show');
      if (modal && !modal.contains(event.target)) {
        setShowCreateModal(false);
      }
    };

    if (showCreateModal) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showCreateModal]);

  return (
    <div className="p-3">
      <style>{cardStyles}</style>

      {/* BACK BUTTON */}
      {location.pathname.startsWith("/home/codices") && !location.pathname.includes("/codices/") && (
        <div className="mb-3">
          <button 
            className="btn btn-outline-primary" 
            onClick={() => navigate("/home")}
          >
            ← Back
          </button>
        </div>
      )}

      {/* ADD PANEL */}
      <div className="video-toolbar mb-3">
        <div className="video-add-group">
          <input
            className="form-control"
            placeholder="Codex title"
            value={input}
            onChange={(e) => setInput(e.target.value)}
          />
          <button className="btn btn-success" onClick={() => setShowCreateModal(true)}>
            Create
          </button>
        </div>

        <div className="video-sort-panel" ref={sortMenuRef}>
          <button
            type="button"
            className="btn btn-outline-light video-sort-trigger"
            onClick={() => setSortMenuOpen((current) => !current)}
            aria-expanded={sortMenuOpen}
          >
            <span className="text-start">
              <span className="video-sort-trigger-label">Sort</span>
              <span className="video-sort-trigger-value">{activeSortLabel}</span>
            </span>
            <i className={`bi ${sortMenuOpen ? "bi-chevron-up" : "bi-chevron-down"}`} aria-hidden="true" />
          </button>

          {sortMenuOpen && (
            <div className="video-sort-menu">
              <div className="video-sort-options" role="listbox" aria-label="Sort codices by">
                {SORT_OPTIONS.map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    className={`video-sort-option ${sortBy === option.value ? "active" : ""}`}
                    onClick={() => {
                      setSortBy(option.value);
                      setSortMenuOpen(false);
                    }}
                  >
                    {option.label}
                  </button>
                ))}
              </div>

              <div className="video-sort-controls">
                <button
                  type="button"
                  className="btn btn-outline-light video-sort-direction"
                  onClick={() => setDirection((current) => (current === "DESC" ? "ASC" : "DESC"))}
                  aria-label={direction === "DESC" ? "Descending order" : "Ascending order"}
                >
                  <i className={`bi ${direction === "DESC" ? "bi-arrow-down" : "bi-arrow-up"}`} aria-hidden="true" />
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {error && (
        <div className="text-danger mb-3">
          {error}
        </div>
      )}

      {/* LIST */}
      <div className="video-list" style={{ overflowY: 'auto', flexGrow: 1 }}>
        {!loading && codices.length === 0 && (
          <div className="text-muted">No codices yet.</div>
        )}

        {codices.map((c) => (
          <div
            key={String(c.id)}
            className="video-list-card d-flex gap-3 align-items-start justify-content-between"
            onClick={() => navigate(`/home/codices/${c.id}`)}
          >
            <div className="d-flex gap-3 align-items-start min-w-0 flex-grow-1">
              <div className="d-flex flex-column align-items-center justify-content-center bg-secondary rounded" style={{ width: '80px', height: '80px' }}>
                <i className="bi bi-book" style={{ fontSize: '2rem' }}></i>
              </div>

              <div className="min-w-0">
                <div className="fw-semibold">{c.title}</div>
                <div className="d-flex flex-wrap gap-3 mt-2 text-muted small">
                  {formatContentNumber(c.contentNumber) && (
                    <span>{formatContentNumber(c.contentNumber)}</span>
                  )}
                  {formatAddedAt(c.addedAt) && (
                    <span>Added: {formatAddedAt(c.addedAt)}</span>
                  )}
                </div>
              </div>
            </div>

            <div className="d-flex flex-column align-items-center gap-2">
              <button
                type="button"
                className="btn btn-danger text-white fw-bold px-3 py-1 flex-shrink-0 align-self-center"
                onClick={(event) => {
                  event.stopPropagation();
                  handleDelete(event, c.id);
                }}
                disabled={deletingId === c.id}
                aria-label={`Delete ${c.title}`}
              >
                {deletingId === c.id ? "..." : "X"}
              </button>
            </div>
          </div>
        ))}
      </div>

      

      {/* CREATE MODAL */}
      {showCreateModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Create from Content</h5>
                <button 
                  type="button" 
                  className="btn-close" 
                  onClick={() => setShowCreateModal(false)}
                ></button>
              </div>
              
              <div className="modal-body">
                <div className="mb-3">
                  <label htmlFor="codexTitle" className="form-label">Codex Title</label>
                  <input
                    type="text"
                    className="form-control"
                    id="codexTitle"
                    placeholder="Enter codex title"
                    value={modalTitle}
                    onChange={(e) => setModalTitle(e.target.value)}
                  />
                </div>
                
                {/* Tabs */}
                <ul className="nav nav-tabs" role="tablist">
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

                <div className="tab-content mt-3">
                  {/* Videos Tab */}
                  <div className={`tab-pane fade ${activeTab === 'videos' ? 'show active' : ''}`}>
                    {loadingContent ? (
                      <div>Loading videos...</div>
                    ) : (
                      <div className="row">
                        {videos.map((video) => (
                          <div 
                            key={video.id} 
                            className={`col-md-6 col-lg-4 mb-3 clickable-card ${selectedItems.includes(video.id) ? 'selected' : ''}`}
                            onClick={() => {
                              if (selectedItems.includes(video.id)) {
                                setSelectedItems(selectedItems.filter(id => id !== video.id));
                              } else {
                                setSelectedItems([...selectedItems, video.id]);
                              }
                            }}
                          >
                            <div className="card">
                              <div className="card-body">
                                <div className="d-flex align-items-start">
                                  <div className="form-check me-2">
                                    <input
                                      type="checkbox"
                                      className="form-check-input"
                                      id={`video-${video.id}`}
                                      checked={selectedItems.includes(video.id)}
                                      readOnly
                                    />
                                    <label className="form-check-label" htmlFor={`video-${video.id}`}>
                                    </label>
                                  </div>
                                  <img
                                    src={video.video?.thumbnailUrl || video.thumbnailUrl}
                                    alt={video.title || video.video?.title}
                                    className="img-thumbnail me-2"
                                    style={{ width: '80px', height: 'auto' }}
                                  />
                                  <div>
                                    <h6 className="card-title mb-1">{video.title || video.video?.title}</h6>
                                    <p className="card-text small text-muted mb-1">
                                      {video.video?.channelTitle || video.channelTitle}
                                    </p>
                                    <p className="card-text small text-muted">
                                      Added: {formatAddedAt(video.addedAt)}
                                    </p>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        ))}
                        {videos.length === 0 && (
                          <div className="text-muted">No videos found</div>
                        )}
                      </div>
                    )}
                  </div>

                  {/* Playlists Tab */}
                  <div className={`tab-pane fade ${activeTab === 'playlists' ? 'show active' : ''}`}>
                    {loadingContent ? (
                      <div>Loading playlists...</div>
                    ) : (
                      <div className="row">
                        {playlists.map((playlist) => (
                          <div 
                            key={playlist.id} 
                            className={`col-md-6 col-lg-4 mb-3 clickable-card ${selectedItems.includes(playlist.id) ? 'selected' : ''}`}
                            onClick={() => {
                              if (selectedItems.includes(playlist.id)) {
                                setSelectedItems(selectedItems.filter(id => id !== playlist.id));
                              } else {
                                setSelectedItems([...selectedItems, playlist.id]);
                              }
                            }}
                          >
                            <div className="card">
                              <div className="card-body">
                                <div className="d-flex align-items-start">
                                  <div className="form-check me-2">
                                    <input
                                      type="checkbox"
                                      className="form-check-input"
                                      id={`playlist-${playlist.id}`}
                                      checked={selectedItems.includes(playlist.id)}
                                      readOnly
                                    />
                                    <label className="form-check-label" htmlFor={`playlist-${playlist.id}`}>
                                    </label>
                                  </div>
                                  <img
                                    src={playlist.playlist?.thumbnailUrl || playlist.thumbnailUrl}
                                    alt={playlist.title || playlist.playlist?.title}
                                    className="img-thumbnail me-2"
                                    style={{ width: '80px', height: 'auto' }}
                                  />
                                  <div>
                                    <h6 className="card-title mb-1">{playlist.title || playlist.playlist?.title}</h6>
                                    <p className="card-text small text-muted mb-1">
                                      {playlist.playlist?.channelTitle || playlist.channelTitle}
                                    </p>
                                    <p className="card-text small text-muted">
                                      Added: {formatAddedAt(playlist.addedAt)}
                                    </p>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        ))}
                        {playlists.length === 0 && (
                          <div className="text-muted">No playlists found</div>
                        )}
                      </div>
                    )}
                  </div>

                  {/* Notes Tab */}
                  <div className={`tab-pane fade ${activeTab === 'notes' ? 'show active' : ''}`}>
                    {loadingContent ? (
                      <div>Loading notes...</div>
                    ) : (
                      <div className="row">
                        {notes.map((note) => (
                          <div 
                            key={note.id} 
                            className={`col-md-6 col-lg-4 mb-3 clickable-card ${selectedItems.includes(note.id) ? 'selected' : ''}`}
                            onClick={() => {
                              if (selectedItems.includes(note.id)) {
                                setSelectedItems(selectedItems.filter(id => id !== note.id));
                              } else {
                                setSelectedItems([...selectedItems, note.id]);
                              }
                            }}
                          >
                            <div className="card">
                              <div className="card-body">
                                <div className="d-flex align-items-start">
                                  <div className="form-check me-2">
                                    <input
                                      type="checkbox"
                                      className="form-check-input"
                                      id={`note-${note.id}`}
                                      checked={selectedItems.includes(note.id)}
                                      readOnly
                                    />
                                    <label className="form-check-label" htmlFor={`note-${note.id}`}>
                                    </label>
                                  </div>
                                  <div>
                                    <h6 className="card-title mb-1">{note.title}</h6>
                                    <p className="card-text small text-muted mb-1">
                                      {note.content?.substring(0, 50)}...
                                    </p>
                                    <p className="card-text small text-muted">
                                      Added: {formatAddedAt(note.addedAt)}
                                    </p>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        ))}
                        {notes.length === 0 && (
                          <div className="text-muted">No notes found</div>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </div>
              
              <div className="modal-footer">
                <button 
                  type="button" 
                  className="btn btn-secondary" 
                  onClick={() => {
                    setShowCreateModal(false);
                    setModalTitle("");
                  }}
                >
                  Close
                </button>
                <button 
                  type="button" 
                  className="btn btn-primary" 
                  onClick={async () => {
                    if (selectedItems.length > 0) {
                      try {
                        // First create a new codex
                        const codexResponse = await api.post('/codices/create', {
                          title: modalTitle || input || "New Codex",
                          contentIds: selectedItems,
                          description: ""
                        });
                        
                        // Then reload the codices list
                        setShowCreateModal(false);
                        setModalTitle("");
                        setSelectedItems([]);
                        await loadCodices();
                      } catch (err) {
                        console.error("Error creating codex:", err);
                        setError("Failed to create codex with selected items.");
                      }
                    }
                  }}
                  disabled={selectedItems.length === 0}
                >
                  Create Codex
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Codices;
