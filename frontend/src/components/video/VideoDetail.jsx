import { useEffect, useRef, useState } from "react";
import YouTube from "react-youtube";
import api from "../../services/api";
import NotesEditor from "../notes/NotesEditor";
import { fetchUserSettings } from "../../services/settings";

function formatDate(date, dateFormat) {
    const d = new Date(date);

    switch (dateFormat) {
      case "EU":
        return d.toLocaleDateString("hu-HU");

      case "US":
        return d.toLocaleDateString("en-US");

      case "ISO":
      default:
        return d.toISOString().split("T")[0];
    }
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

function VideoDetail({ userVideoId, currentUser, onBack }) {
  const [videoDetail, setVideoDetail] = useState(null);
  const [notes, setNotes] = useState([]);
  const [loadingNotes, setLoadingNotes] = useState(false);
  const [noteDraft, setNoteDraft] = useState("");
  const [previewMode, setPreviewMode] = useState(false);
  const [saveState, setSaveState] = useState("idle");
  const [loadError, setLoadError] = useState("");
  const [saveError, setSaveError] = useState("");
  const [loading, setLoading] = useState(true);
  const [userSettings, setUserSettings] = useState(null);
  const playerRef = useRef(null);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setLoadError("");
    setSaveError("");
    setSaveState("idle");
    setPreviewMode(false);

    const loadVideoDetail = async () => {
      try {
        // Fetch user settings
        const settings = await fetchUserSettings();
        setUserSettings(settings);
        
        const res = await api.get(`/uservideos/${userVideoId}`, {
          signal: controller.signal,
        });
        setVideoDetail(res.data);
        setNoteDraft(res.data.note ?? "");
        // Load notes for this video
        setLoadingNotes(true);
        const notesRes = await api.get(`/uservideos/${userVideoId}/notes`);
        setNotes(notesRes.data);
        // Initialize noteDraft with the content of the first note, if it exists
        if (notesRes.data.length > 0 && notesRes.data[0]) {
          setNoteDraft(notesRes.data[0].content ?? "");
        }
        // Set timestamps from the video data
        setTimestamps(res.data.timestamps ?? []);
        setLoadingNotes(false);
      } catch (err) {
        if (err.code === "ERR_CANCELED") {
          return;
        }

        console.error("VIDEO DETAIL ERROR:", err);
        setLoadError("Could not load video details.");
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    loadVideoDetail();

    return () => {
      controller.abort();
    };
  }, [userVideoId]);

  const activeVideo = videoDetail?.video;
  const youtubeId = activeVideo?.youtubeId;
  const stats = activeVideo?.stats;
  const currentNote = videoDetail?.note ?? "";
  const currentWatched = videoDetail?.watched ?? false;
  const isDirty = noteDraft !== currentNote;

  const handleSave = async () => {
    setSaveState("saving");
    setSaveError("");

    try {
      // First, we need to determine which note to update
      // If there are existing notes, update the first one
      // If no notes exist, we need to create one first
      let noteIdToSave = null;
      
      if (notes.length > 0) {
        // Update the first existing note
        noteIdToSave = notes[0].id;
      } else {
        // Create a new note if none exists
        const createResponse = await api.post('/notes/create', {
          parentId: null,
          title: "Untitled Note",
          content: noteDraft,
          isPdf: false,
          parentIds: [userVideoId]
        });
        noteIdToSave = createResponse.data.id;
        // Refresh notes to include the new one
        await fetchNotes();
      }

      if (noteIdToSave) {
        // Update the note content
        try {
          // Make sure we have a valid note to update
          if (!notes[0]) {
            throw new Error("No note found to update");
          }
          const updateResponse = await api.put(`/notes/${noteIdToSave}`, {
            title: notes[0].title,
            content: noteDraft
          });
        } catch (updateError) {
          console.error("Note update error:", updateError);
          throw updateError;
        }
        
        // Update the video's note reference if we just created one
        if (notes.length === 0) {
          // Fetch updated video data to get the note reference
          try {
            const res = await api.get(`/uservideos/${userVideoId}`);
            setVideoDetail(res.data);
            setNoteDraft(res.data.note ?? "");
          } catch (fetchError) {
            console.error("Video fetch error:", fetchError);
            console.error("Fetch error details:", {
              status: fetchError.response?.status,
              data: fetchError.response?.data
            });
            throw fetchError;
          }
        }
        
        setSaveState("saved");
      }
    } catch (err) {
      console.error("VIDEO NOTE SAVE ERROR:", err);
      console.error("Error details:", {
        message: err.message,
        response: err.response,
        status: err.status
      });
      setSaveError("Could not save note. " + (err.response?.data?.message || err.message || ""));
      setSaveState("error");
    }
  };

  const fetchNotes = async () => {
    try {
      setLoadingNotes(true);
      const notesRes = await api.get(`/uservideos/${userVideoId}/notes`);
      setNotes(notesRes.data);
      // Initialize noteDraft with the content of the first note, if it exists
      if (notesRes.data.length > 0 && notesRes.data[0]) {
        setNoteDraft(notesRes.data[0].content ?? "");
      }
    } catch (err) {
      console.error("Error fetching notes:", err);
      setLoadError("Failed to load notes");
    } finally {
      setLoadingNotes(false);
    }
  };

  const handlePlayerReady = (event) => {
    playerRef.current = event.target;
  };

  const handleAddTimestamp = async () => {
    const seconds = Number(newTimestampSeconds);

    if (Number.isNaN(seconds) || seconds < 0) {
      return;
    }

    const updatedTimestamps = [
      ...timestamps,
      {
        seconds,
        label: newTimestampLabel.trim(),
      },
    ];

    try {
      const res = await api.patch(`/uservideos/${userVideoId}`, {
        note: noteDraft,
        watched: currentWatched,
        timestamps: updatedTimestamps,
      });

      setTimestamps(res.data.timestamps ?? []);

      setNewTimestampSeconds("");
      setNewTimestampLabel("");
    } catch (err) {
      console.error("TIMESTAMP SAVE ERROR:", err);
    }
  };

  const handleDeleteTimestamp = async (index) => {
    const updatedTimestamps =
      timestamps.filter((_, i) => i !== index);

    try {
      const res = await api.patch(`/uservideos/${userVideoId}`, {
        note: noteDraft,
        watched: currentWatched,
        timestamps: updatedTimestamps,
      });

      setTimestamps(res.data.timestamps ?? []);
    } catch (err) {
      console.error("TIMESTAMP DELETE ERROR:", err);
    }
  };

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

  const [timestamps, setTimestamps] = useState([]);
  const [newTimestampSeconds, setNewTimestampSeconds] = useState("");
  const [newTimestampLabel, setNewTimestampLabel] = useState("");

  if (loading) {
    return <div className="p-4">Loading video...</div>;
  }

  if (loadError) {
    return <div className="p-4 text-danger">{loadError}</div>;
  }

  if (!activeVideo) {
    return <div className="p-4 text-danger">Video details are unavailable.</div>;
  }

  return (
    <div className="d-flex h-100 overflow-hidden">
      <div className="flex-grow-1 d-flex flex-column min-w-0" id="video-detail-container">
        <div
          className="px-3 py-2"
          style={{ background: "rgba(0,0,0,0.45)" }}
        >
          <button
            className="btn btn-link text-light p-0 text-decoration-none"
            onClick={onBack}
          >
            ← Back
          </button>
        </div>

        <div className="position-relative flex-grow-1 overflow-hidden">
          <YouTube
            videoId={youtubeId}
            onReady={handlePlayerReady}
            className="w-100 h-100"
            iframeClassName="w-100 h-100 border-0"
            opts={{
              width: "100%",
              height: "100%",
              playerVars: {
                rel: 0,
              },
            }}
          />
        </div>

        <div
          style={{ minHeight: "140px", background: "rgba(0,0,0,0.5)" }}
          className="p-3"
        >
          <div className="row">
            <div className="col-md-4">
              <div className="d-flex flex-column gap-2">
                <div>Views: {stats?.viewCount ?? "-"}</div>
                <div>Likes: {stats?.likeCount ?? "-"}</div>

                <div>
                  Published: {
                    activeVideo.publishedAt
                      ? formatDate(
                          activeVideo.publishedAt,
                          userSettings?.dateFormat || "ISO"
                        )
                      : "-"
                  }
                </div>
              </div>
            </div>

            <div className="col-md-8">
              <div className="fw-bold mb-2">
                Timestamps
              </div>

              <div className="row g-2 mb-3">

                <div className="col-3">
                  <input
                    type="number"
                    className="form-control form-control-sm"
                    placeholder="Sec"
                    value={newTimestampSeconds}
                    onChange={(e) => setNewTimestampSeconds(e.target.value)}
                  />
                </div>

                <div className="col">
                  <input
                    type="text"
                    className="form-control form-control-sm"
                    placeholder="Label"
                    value={newTimestampLabel}
                    onChange={(e) => setNewTimestampLabel(e.target.value)}
                  />
                </div>

                <div className="col-auto">
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() =>
                      setNewTimestampSeconds(
                        Math.floor(
                          playerRef.current.getCurrentTime()
                        )
                      )
                    }
                  >
                    Use Current
                  </button>
                </div>

                <div className="col-auto">
                  <button
                    type="button"
                    className="btn btn-success btn-sm"
                    onClick={handleAddTimestamp}
                  >
                    Add
                  </button>
                </div>

              </div>

              <div
                style={{
                  maxHeight: "90px",
                  overflowY: "auto"
                }}
              >
                {timestamps.length === 0 ? (
                  <div className="text-muted small">
                    No timestamps yet.
                  </div>
                ) : (
                  timestamps.map((timestamp, index) => (
                    <div
                      key={index}
                      className="d-flex justify-content-between align-items-center mb-1"
                    >
                      <span
                        className="text-decoration-underline"
                        style={{
                          cursor: "pointer"
                        }}
                        onClick={() => {
                          playerRef.current.seekTo(timestamp.seconds);
                          playerRef.current.playVideo();
                        }}
                      >
                        <strong>
                          {formatTimestamp(timestamp.seconds)}
                        </strong>
                        {" - "}
                        {timestamp.label}
                      </span>

                      <button
                        type="button"
                        className="btn btn-outline-danger btn-sm"
                        onClick={() => handleDeleteTimestamp(index)}
                      >
                        ×
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

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
        
        {/* Enhanced NotesEditor component */}
        <NotesEditor
          note={noteDraft}
          onNoteChange={setNoteDraft}
          onSave={handleSave}
          loading={loadingNotes}
          saveState={saveState}
          saveError={saveError}
          isDirty={isDirty}
          onTogglePreview={setPreviewMode}
          previewMode={previewMode}
          onIsDirtyChange={setSaveState}
          parentId={userVideoId}
          parentType="video"
          currentUser={currentUser}
          notes={notes}
          onFetchNotes={fetchNotes}
        />
      </div>
    </div>
  );
}

export default VideoDetail;