import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../../services/api";
import ContentCard from "../misc/ContentCard";

const SORT_OPTIONS = [
  { value: "ADDED_AT", label: "Date added" },
  { value: "NAME", label: "Name" },
  { value: "WATCHED", label: "Watched" },
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

const formatVideoCount = (count) => {
  if (count === null || count === undefined || count < 0) {
    return null;
  }
  return `${count} videos`;
};

const toSummaryItem = (userPlaylist) => ({
  id: userPlaylist.id,
  watched: userPlaylist.watched,
  addedAt: userPlaylist.addedAt,
  playlist: {
    id: userPlaylist.playlist.id,
    title: userPlaylist.playlist.title,
    channelTitle: userPlaylist.playlist.channelTitle,
    thumbnailUrl: userPlaylist.playlist.thumbnailUrl,
    videoCount: userPlaylist.playlist.videoCount ?? 0,
  },
});

function PlaylistList() {
  const navigate = useNavigate();
  const location = useLocation();
  const [playlists, setPlaylists] = useState([]);
  const [input, setInput] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState(null);
  const [sortMenuOpen, setSortMenuOpen] = useState(false);
  const [sortBy, setSortBy] = useState("ADDED_AT");
  const [direction, setDirection] = useState("DESC");
  const sortMenuRef = useRef(null);

  const activeSortLabel = useMemo(
    () => SORT_OPTIONS.find((option) => option.value === sortBy)?.label ?? "Date added",
    [sortBy]
  );

  const loadPlaylists = useCallback(async (signal) => {
    try {
      const res = await api.get("/userplaylists", {
        signal,
        params: {
          sortBy,
          direction,
        },
      });
      setPlaylists(res.data);
      setError("");
    } catch (err) {
      if (err.code === "ERR_CANCELED") {
        return;
      }

      console.error("PLAYLIST LIST ERROR:", err);
      setError("Could not load your playlists.");
    } finally {
      if (!signal?.aborted) {
        setLoading(false);
      }
    }
  }, [direction, sortBy]);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);

    loadPlaylists(controller.signal);

    return () => {
      controller.abort();
    };
  }, [loadPlaylists]);

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
      const res = await api.post("/userplaylists", { url: input });
      const createdPlaylist = toSummaryItem(res.data);
      setPlaylists((current) => [createdPlaylist, ...current]);
      setInput("");
      await loadPlaylists();
    } catch (err) {
      console.error("PLAYLIST ADD ERROR:", err);
      setError("Could not add the playlist.");
    }
  };

  const handleDelete = async (event, id) => {
    event.stopPropagation();
    setError("");
    setDeletingId(id);

    try {
      await api.delete(`/userplaylists/${id}`);
      setPlaylists((current) => current.filter((playlist) => playlist.id !== id));
      await loadPlaylists();
    } catch (err) {
      console.error("PLAYLIST DELETE ERROR:", err);
      setError("Could not delete the playlist.");
    } finally {
      setDeletingId(null);
    }
  };

  const toggleWatchedStatus = async (userPlaylistId, currentWatchedStatus) => {
    try {
      const res = await api.patch(`/userplaylists/${userPlaylistId}`, {
        watched: !currentWatchedStatus
      });

      // Update the local playlists array to reflect the change
      setPlaylists(prevPlaylists => 
        prevPlaylists.map(playlist => 
          playlist.id === userPlaylistId ? { ...playlist, watched: !currentWatchedStatus } : playlist
        )
      );
    } catch (err) {
      console.error("Error toggling watched status:", err);
    }
  };

  return (
    <div className="p-3">
      {/* BACK BUTTON */}
      {location.pathname.startsWith("/home/playlists") && (
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
              <div className="video-sort-options" role="listbox" aria-label="Sort playlists by">
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
        {!loading && playlists.length === 0 && (
          <div className="text-muted">No playlists yet.</div>
        )}

        {playlists.map((p) => (
          <ContentCard
            key={p.id}
            title={p.playlist.title}
            subtitle={p.playlist.channelTitle}
            thumbnailUrl={p.playlist.thumbnailUrl}
            onClick={() => navigate(`/home/playlists/${p.id}`)}
            onToggleWatched={() => toggleWatchedStatus(p.id, p.watched)}
            watched={p.watched}
            additionalInfo={[
              p.watched ? "Watched" : "Unwatched",
              formatAddedAt(p.addedAt) && `Added: ${formatAddedAt(p.addedAt)}`,
              formatVideoCount(p.playlist.videoCount) && formatVideoCount(p.playlist.videoCount)
            ].filter(Boolean)}
          >
            <button
              type="button"
              className="btn btn-danger text-white fw-bold px-3 py-1 flex-shrink-0 align-self-center"
              onClick={(event) => {
                event.stopPropagation();
                handleDelete(event, p.id);
              }}
              disabled={deletingId === p.id}
              aria-label={`Delete ${p.playlist.title}`}
            >
              {deletingId === p.id ? "..." : "X"}
            </button>
          </ContentCard>
        ))}
      </div>
    </div>
  );
}

export default PlaylistList;