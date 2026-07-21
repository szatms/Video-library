import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../services/api";

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
  const [videos, setVideos] = useState([]);
  const [input, setInput] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState(null);
  const [sortMenuOpen, setSortMenuOpen] = useState(false);
  const [sortBy, setSortBy] = useState("ADDED_AT");
  const [direction, setDirection] = useState("DESC");
  const [unwatchedFirst, setUnwatchedFirst] = useState(true);
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
      setVideos(res.data);
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
      const res = await api.post("/uservideos", { url: input });
      const createdVideo = toSummaryItem(res.data);
      setVideos((current) => [createdVideo, ...current]);
      setInput("");
      await loadVideos();
    } catch (err) {
      console.error("VIDEO ADD ERROR:", err);
      setError("Could not add the video.");
    }
  };

  const handleDelete = async (event, id) => {
    event.stopPropagation();
    setError("");
    setDeletingId(id);

    try {
      await api.delete(`/uservideos/${id}`);
      setVideos((current) => current.filter((video) => video.id !== id));
      await loadVideos();
    } catch (err) {
      console.error("VIDEO DELETE ERROR:", err);
      setError("Could not delete the video.");
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="p-3">

      {/* ADD PANEL */}
      <div className="video-toolbar mb-3">
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
        <div className="text-danger mb-3">
          {error}
        </div>
      )}

      {/* LIST */}
      <div className="video-list">
        {!loading && videos.length === 0 && (
          <div className="text-muted">No videos yet.</div>
        )}

        {videos.map((v) => (
          <div
            key={v.id}
            className="video-list-card d-flex gap-3 align-items-start justify-content-between"
            onClick={() => navigate(`/home/videos/${v.id}`)}
          >
            <div className="d-flex gap-3 align-items-start min-w-0 flex-grow-1">
              <img
                src={v.video.thumbnailUrl}
                alt={v.video.title}
                className="video-list-thumbnail"
              />

              <div className="min-w-0">
                <div className="fw-semibold">{v.video.title}</div>
                {v.video.channelTitle && (
                  <div className="video-list-channel text-truncate">{v.video.channelTitle}</div>
                )}
                <div className="d-flex flex-wrap gap-3 mt-2 text-muted small">
                  <span>{v.watched ? "Watched" : "Unwatched"}</span>
                  {formatAddedAt(v.addedAt) && (
                    <span>Added: {formatAddedAt(v.addedAt)}</span>
                  )}
                  {formatDuration(v.video.durationSeconds) && (
                    <span>{formatDuration(v.video.durationSeconds)}</span>
                  )}
                  <span>{(v.video.viewCount ?? 0).toLocaleString()} views</span>
                </div>
              </div>
            </div>

            <button
              type="button"
              className="btn btn-danger text-white fw-bold px-3 py-1 flex-shrink-0 align-self-center"
              onClick={(event) => handleDelete(event, v.id)}
              disabled={deletingId === v.id}
              aria-label={`Delete ${v.video.title}`}
            >
              {deletingId === v.id ? "..." : "X"}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default VideoList;
