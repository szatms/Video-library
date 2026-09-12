import { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../services/api";
import NotesEditor from "../notes/NotesEditor";
import ContentCard from "../misc/ContentCard";
import { fetchUserSettings } from "../../services/settings";
import { formatDate } from "../../utils/sharedUtils";

function PlaylistDetail() {
  const { playlistId } = useParams();
  const navigate = useNavigate();
  const [playlist, setPlaylist] = useState(null);
  const [videos, setVideos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [noteDraft, setNoteDraft] = useState("");
  const [previewMode, setPreviewMode] = useState(false);
  const [saveState, setSaveState] = useState("idle");
  const [saveError, setSaveError] = useState("");
  const [isDirty, setIsDirty] = useState(false);
  const [notes, setNotes] = useState([]);
  const [loadingNotes, setLoadingNotes] = useState(false);
  const [userSettings, setUserSettings] = useState(null);

  useEffect(() => {
    const fetchPlaylistDetails = async () => {
      try {
        setLoading(true);
        setError(null);
        
        // Fetch user settings
        const settings = await fetchUserSettings();
        setUserSettings(settings);
        
        // Fetch playlist details
        const playlistResponse = await api.get(`/userplaylists/${playlistId}`);
        setPlaylist(playlistResponse.data);
        
        // Fetch videos for this specific playlist using the new endpoint
        const videosResponse = await api.get(`/uservideos/playlist/${playlistId}`);
        setVideos(videosResponse.data);

        // Extract note from the playlist data or fetch from notes endpoint
        if (playlistResponse.data.note) {
          setNoteDraft(playlistResponse.data.note);
        } else {
          // Fetch notes associated with this playlist
          try {
            const noteResponse = await api.get(`/notes?parentIds=${playlistId}`);
            if (noteResponse.data && noteResponse.data.length > 0) {
              setNoteDraft(noteResponse.data[0].content || "");
            } else {
              setNoteDraft("");
            }
          } catch (noteErr) {
            console.error("Error fetching notes:", noteErr);
            setNoteDraft("");
          }
        }
      } catch (err) {
        console.error("Error fetching playlist details:", err);
        if (err.response?.status === 401) {
          // Redirect to login if unauthorized
          navigate("/");
        } else {
          setError("Could not load playlist details.");
        }
      } finally {
        setLoading(false);
      }
    };

    if (playlistId) {
      fetchPlaylistDetails();
    }
  }, [playlistId, navigate]);

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

  const handleVideoClick = (userVideoId) => {
    // Extract channel ID from the playlist data or fallback to navigation
    const channelId = playlist?.channelId || playlist?.playlist?.channelId;
    if (channelId) {
      navigate(`/home/channels/${channelId}/playlists/${playlistId}/videos/${userVideoId}`);
    } else {
      // Fallback if channel ID not available
      navigate(`/home/playlists/${playlistId}/${userVideoId}`);
    }
  };

  const toggleWatchedStatus = async (userVideoId, currentWatchedStatus) => {
    try {
      const res = await api.patch(`/uservideos/${userVideoId}`, {
        watched: !currentWatchedStatus
      });

      // Update the local videos array to reflect the change
      setVideos(prevVideos => 
        prevVideos.map(video => 
          video.id === userVideoId ? { ...video, watched: !currentWatchedStatus } : video
        )
      );
    } catch (err) {
      console.error("Error toggling watched status:", err);
    }
  };

  const handleNoteChange = (newNote) => {
    setNoteDraft(newNote);
    setIsDirty(true);
    setSaveState("idle");
    setSaveError("");
  };

  const handleTogglePreview = (preview) => {
    setPreviewMode(preview);
  };

  const handleSave = async () => {
    setSaveState("saving");
    setSaveError("");

    try {
      // Save playlist notes using the correct API endpoint
      // First, create or update the note in the note system
      const noteData = {
        title: `${playlist?.playlist?.title || 'Playlist'} Notes`,
        content: noteDraft,
        parentIds: [playlistId],
        isPdf: false
      };

      // Check if note already exists for this playlist
      const existingNotes = await api.get(`/notes?parentIds=${playlistId}`);
      
      if (existingNotes.data && existingNotes.data.length > 0) {
        // Update existing note
        const noteId = existingNotes.data[0].id;
        await api.put(`/notes/${noteId}`, noteData);
      } else {
        // Create new note
        await api.post(`/notes/create`, noteData);
      }

      setSaveState("saved");
      setIsDirty(false);
      // Update the note in the local state to reflect saved value
      if (playlist) {
        setPlaylist({...playlist, note: noteDraft});
      }
    } catch (err) {
      console.error("PLAYLIST NOTE SAVE ERROR:", err);
      if (err.response?.status === 401) {
        // Redirect to login if unauthorized
        navigate("/");
      } else {
        setSaveError("Could not save notes.");
        setSaveState("error");
      }
    }
  };

  const fetchNotes = async () => {
    try {
      setLoadingNotes(true);
      const notesRes = await api.get(`/notes?parentIds=${playlistId}`);
      setNotes(notesRes.data);
    } catch (err) {
      console.error("Error fetching notes:", err);
      setSaveError("Failed to load notes");
    } finally {
      setLoadingNotes(false);
    }
  };

  // Fetch notes when component mounts and when playlistId changes
  useEffect(() => {
    if (playlistId) {
      fetchNotes();
    }
  }, [playlistId]);

  // Resizable notes section functionality
  useEffect(() => {
    const setupResize = () => {
      const resizer = document.getElementById('resizer');
      const notesSection = document.getElementById('notes-section');
      
      if (!resizer || !notesSection) {
        // Try again after a short delay if elements aren't available yet
        setTimeout(setupResize, 100);
        return;
      }
      
      let isResizing = false;
      let startX, startWidth;

      const startResizing = (e) => {
        e.preventDefault();
        isResizing = true;
        startX = e.clientX;
        startWidth = parseInt(document.defaultView.getComputedStyle(notesSection).width, 10);
        document.body.style.cursor = 'col-resize';
        document.body.style.userSelect = 'none';
        document.body.style.MozUserSelect = 'none';
        document.body.style.WebkitUserSelect = 'none';
      };

      const resize = (e) => {
        if (!isResizing) return;
        e.preventDefault();
        // Reverse the direction: dragging left makes it bigger, dragging right makes it smaller
        const newWidth = startWidth - (e.clientX - startX);
        // Ensure minimum width (320px) and maximum width (600px) - adjust as needed
        const minWidth = 320;
        const maxWidth = 600;
        
        if (newWidth >= minWidth && newWidth <= maxWidth) {
          notesSection.style.width = newWidth + 'px';
        }
      };

      const stopResizing = (e) => {
        e.preventDefault();
        isResizing = false;
        document.body.style.cursor = '';
        document.body.style.userSelect = '';
        document.body.style.MozUserSelect = '';
        document.body.style.WebkitUserSelect = '';
      };

      resizer.addEventListener('mousedown', startResizing);
      document.addEventListener('mousemove', resize);
      document.addEventListener('mouseup', stopResizing);

      // Cleanup
      return () => {
        resizer.removeEventListener('mousedown', startResizing);
        document.removeEventListener('mousemove', resize);
        document.removeEventListener('mouseup', stopResizing);
      };
    };

    setupResize();
  }, []);

  if (loading) {
    return <div className="p-3">Loading playlist...</div>;
  }

  if (error) {
    return <div className="p-3 text-danger">{error}</div>;
  }

  if (!playlist) {
    return <div className="p-3">Playlist not found.</div>;
  }

  return (
    <div className="d-flex h-100 overflow-hidden">
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

        {/* PLAYLIST HEADER */}
        <div className="d-flex gap-3 mb-4">
          <img
            src={playlist.playlist.thumbnailUrl}
            alt={playlist.playlist.title}
            className="video-list-thumbnail"
          />
          <div className="flex-grow-1">
            <h2 className="fw-bold">{playlist.playlist.title}</h2>
            <p className="text-muted">{playlist.playlist.channelTitle}</p>
            <div className="d-flex flex-wrap gap-3 mt-2 text-muted small">
              <span>Added: {formatAddedAt(playlist.addedAt)}</span>
              <span>{videos.length} videos</span>
            </div>
          </div>
        </div>

        {/* VIDEO LIST */}
        <div className="flex-grow-1 overflow-auto" style={{ maxHeight: 'calc(100vh - 250px)' }}>
          {videos.length === 0 ? (
            <div className="text-muted">No videos in this playlist.</div>
          ) : (
            <div className="d-flex flex-column gap-3">
              {videos.map((video) => (
                <ContentCard
                  key={video.id}
                  title={video.video.title}
                  subtitle={video.video.channelTitle}
                  thumbnailUrl={video.video.thumbnailUrl}
                  onClick={() => handleVideoClick(video.id)}
                  onToggleWatched={() => toggleWatchedStatus(video.id, video.watched)}
                  watched={video.watched}
                  additionalInfo={[
                    video.video.durationSeconds && formatDuration(video.video.durationSeconds),
                    video.addedAt && `Added: ${formatAddedAt(video.addedAt)}`
                  ].filter(Boolean)}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* NOTE EDITOR PANEL */}
      <div
        id="notes-section"
        style={{ width: "320px", background: "rgba(0,0,0,0.6)", position: "relative" }}
        className="p-3 d-flex flex-column flex-shrink-0 h-100 overflow-auto"
      >
        {/* Resizer handle */}
        <div 
          id="resizer"
          style={{
            position: "absolute",
            left: "-5px",
            top: 0,
            bottom: 0,
            width: "10px",
            cursor: "col-resize",
            zIndex: 10,
            background: "rgba(255,255,255,0.2)",
            borderLeft: "1px solid rgba(255,255,255,0.3)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            userSelect: "none"
          }}
        >
          <div style={{ 
            width: "4px", 
            height: "20px", 
            background: "rgba(255,255,255,0.6)", 
            borderRadius: "2px" 
          }} />
        </div>
        
        <NotesEditor
          note={noteDraft}
          onNoteChange={handleNoteChange}
          onSave={handleSave}
          loading={loading}
          saveState={saveState}
          saveError={saveError}
          isDirty={isDirty}
          onTogglePreview={handleTogglePreview}
          previewMode={previewMode}
          onIsDirtyChange={setIsDirty}
          parentId={playlistId}
          parentType="playlist"
          notes={notes}
          onFetchNotes={fetchNotes}
        />
      </div>
    </div>
  );
}

export default PlaylistDetail;