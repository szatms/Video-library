import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../../services/api";
import ContentCard from "../misc/ContentCard";
import SimpleLoadingModal from "../misc/SimpleLoadingModal";

const SORT_OPTIONS = [
  { value: "ADDED_AT", label: "Date added" },
  { value: "VIEWS", label: "Views" },
  { value: "WATCHED", label: "Watched" },
];

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

const toSummaryItem = (userVideo) => ({
  id: userVideo.id,
  watched: userVideo.watched,
  note: userVideo.note,
  addedAt: userVideo.addedAt,
  video: {
    videoId: userVideo.video.videoId,
    youtubeId: userVideo.video.youtubeId,
    title: userVideo.video.title,
    thumbnailUrl: userVideo.video.thumbnailUrl,
    channelId: userVideo.video.channelId,
    channelTitle: userVideo.video.channelTitle,
    viewCount: userVideo.video.viewCount ?? 0,
    durationSeconds: userVideo.video.durationSeconds ?? null,
  },
});

function VideoList() {
  const navigate = useNavigate();
  const location = useLocation();
  const [videos, setVideos] = useState([]);
  const [input, setInput] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteTargetId, setDeleteTargetId] = useState(null);
  const [sortMenuOpen, setSortMenuOpen] = useState(false);
  const [sortBy, setSortBy] = useState("ADDED_AT");
  const [direction, setDirection] = useState("DESC");
  const [unwatchedFirst, setUnwatchedFirst] = useState(true);
  const [showLoadingModal, setShowLoadingModal] = useState(false);
  const sortMenuRef = useRef(null);

  const activeSortLabel = useMemo(
    () => SORT_OPTIONS.find((option) => option.value === sortBy)?.label ?? "Date added",
    [sortBy]
  );

  const loadVideos = useCallback(async (signal) => {
    try {
      const res = await api.get("/uservideos", {
        signal,
        params: {
          sortBy,
          direction,
          unwatchedFirst,
        },
      });
      // Filter videos to only show those where inPlaylist is false
      const filteredVideos = res.data.filter(video => !video.inPlaylist);
      setVideos(filteredVideos);
      setError("");
    } catch (err) {
      if (err.code === "ERR_CANCELED") {
        return;
      }

      console.error("VIDEO LIST ERROR:", err);
      setError("Could not load your videos.");
    } finally {
      if (!signal?.aborted) {
        setLoading(false);
      }
    }
  }, [direction, sortBy, unwatchedFirst]);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);

    loadVideos(controller.signal);

    return () => {
      controller.abort();
    };
  }, [loadVideos]);

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
      setError("Enter a YouTube link or ID.");
      return;
    }

    try {
      setShowLoadingModal(true);
      const res = await api.post("/uservideos", { url: input });
      const createdVideo = toSummaryItem(res.data);
      setVideos((current) => [createdVideo, ...current]);
      setInput("");
      await loadVideos();
    } catch (err) {
      console.error("VIDEO ADD ERROR:", err);
      setError("Could not add the video.");
    } finally {
      setShowLoadingModal(false);
    }
  };

  const handleDelete = async (event, id) => {
    event.stopPropagation();
    // Show confirmation modal instead of deleting immediately
    setDeleteTargetId(id);
    setShowDeleteModal(true);
  };

  const confirmDelete = async () => {
    if (!deleteTargetId) return;
    
    setError("");
    setDeletingId(deleteTargetId);

    try {
      await api.delete(`/uservideos/${deleteTargetId}`);
      setVideos((current) => current.filter((video) => video.id !== deleteTargetId));
      await loadVideos();
    } catch (err) {
      console.error("VIDEO DELETE ERROR:", err);
      setError("Could not delete the video.");
    } finally {
      setDeletingId(null);
      setShowDeleteModal(false);
      setDeleteTargetId(null);
    }
  };

  const cancelDelete = () => {
    setShowDeleteModal(false);
    setDeleteTargetId(null);
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

  return (
    <div className="d-flex h-100 overflow-hidden">
      <div className="flex-grow-1 d-flex flex-column min-w-0 p-4">
        <style>{`
          .modal-backdrop {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            z-index: 1050;
          }
          
          .modal {
            display: block;
            z-index: 1055;
          }
        `}</style>
        {/* BACK BUTTON */}
        {location.pathname.startsWith("/home/videos") && !location.pathname.includes("/videos/") && (
          <div className="mb-4">
            <button 
              className="btn btn-outline-primary" 
              onClick={() => navigate("/home")}
            >
              ← Back
            </button>
          </div>
        )}
        
        {/* PAGE TITLE */}
        <h1 className="mb-4">Videos</h1>

        {/* ADD PANEL */}
        <div className="video-toolbar mb-4">
          <div className="video-add-group">
            <input
              className="form-control"
              placeholder="YouTube link or ID"
              value={input}
              onChange={(e) => setInput(e.target.value)}
            />
            <button className="btn btn-success" onClick={handleAdd}>
              Add
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
                <div className="video-sort-options" role="listbox" aria-label="Sort videos by">
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
                  <label className="video-sort-checkbox">
                    <input
                      type="checkbox"
                      checked={unwatchedFirst}
                      onChange={(event) => setUnwatchedFirst(event.target.checked)}
                    />
                    <span>Unwatched first</span>
                  </label>

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
          <div className="text-danger mb-4">
            {error}
          </div>
        )}

        {/* LIST */}
        <div className="flex-grow-1 overflow-auto">
          {!loading && videos.length === 0 && (
            <div className="text-muted">No videos yet.</div>
          )}

          {videos.map((v) => (
            <ContentCard
              key={v.id}
              title={v.video.title}
              subtitle={v.video.channelTitle}
              thumbnailUrl={v.video.thumbnailUrl}
              onClick={() => navigate(`/home/videos/${v.id}`)}
              onToggleWatched={() => toggleWatchedStatus(v.id, v.watched)}
              watched={v.watched}
              additionalInfo={[
                formatAddedAt(v.addedAt) && `Added: ${formatAddedAt(v.addedAt)}`,
                formatDuration(v.video.durationSeconds) && formatDuration(v.video.durationSeconds),
                `${(v.video.viewCount ?? 0).toLocaleString()} views`,
                v.watched ? "Watched" : "Unwatched"
              ].filter(Boolean)}
            >
              <button
                type="button"
                className="btn btn-danger text-white fw-bold px-3 py-1 flex-shrink-0 align-self-center"
                onClick={(event) => {
                  event.stopPropagation();
                  handleDelete(event, v.id);
                }}
                disabled={deletingId === v.id}
                aria-label={`Delete ${v.video.title}`}
              >
                {deletingId === v.id ? "..." : "X"}
              </button>
            </ContentCard>
          ))}
        </div>

        {/* DELETE CONFIRMATION MODAL */}
        {showDeleteModal && (
          <div 
            className="modal show d-block" 
            tabIndex="-1" 
            onClick={cancelDelete}
            style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1050 }}
          >
            <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">Confirm Delete</h5>
                  <button type="button" className="btn-close" onClick={cancelDelete}></button>
                </div>
                <div className="modal-body">
                  Are you sure you want to delete this video? This action cannot be undone.
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={cancelDelete}>Cancel</button>
                  <button type="button" className="btn btn-danger" onClick={confirmDelete}>Delete</button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* LOADING MODAL */}
        <SimpleLoadingModal show={showLoadingModal} />
      </div>
    </div>
  );
}

export default VideoList;
