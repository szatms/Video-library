import { useEffect, useRef, useState } from "react";
import YouTube from "react-youtube";
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
  const [noteDraft, setNoteDraft] = useState("");
  const [previewMode, setPreviewMode] = useState(false);
  const [saveState, setSaveState] = useState("idle");
  const [loadError, setLoadError] = useState("");
  const [saveError, setSaveError] = useState("");
  const [loading, setLoading] = useState(true);
  const textareaRef = useRef(null);
  const [timestamps, setTimestamps] = useState([]);
  const [newTimestampSeconds, setNewTimestampSeconds] = useState("");
  const [newTimestampLabel, setNewTimestampLabel] = useState("");
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
        const res = await api.get(`/uservideos/${userVideoId}`, {
          signal: controller.signal,
        });
        setVideoDetail(res.data);
        setNoteDraft(res.data.note ?? "");
        setTimestamps(res.data.timestamps ?? []);
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
  const isDirty =
    noteDraft !== currentNote ||
    JSON.stringify(timestamps) !==
      JSON.stringify(videoDetail?.timestamps ?? []);

  const applyFormat = (prefix, suffix = "", placeholder = "text") => {
    const textarea = textareaRef.current;

    if (!textarea) {
      return;
    }

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selected = noteDraft.slice(start, end) || placeholder;
    const updated =
      noteDraft.slice(0, start) +
      prefix +
      selected +
      suffix +
      noteDraft.slice(end);

    setNoteDraft(updated);
    setSaveState("idle");
    setSaveError("");

    requestAnimationFrame(() => {
      textarea.focus();
      const cursorStart = start + prefix.length;
      const cursorEnd = cursorStart + selected.length;
      textarea.setSelectionRange(cursorStart, cursorEnd);
    });
  };

  const handleSave = async () => {
    setSaveState("saving");
    setSaveError("");

    try {
      const res = await api.patch(`/uservideos/${userVideoId}`, {
        note: noteDraft,
        watched: currentWatched,
        timestamps: timestamps,
      });

      setVideoDetail(res.data);
      setNoteDraft(res.data.note ?? "");
      setTimestamps(res.data.timestamps ?? []);

      setSaveState("saved");
    } catch (err) {
      console.error("VIDEO NOTE SAVE ERROR:", err);
      setSaveError("Could not save note.");
      setSaveState("error");
    }
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

  const handlePlayerReady = (event) => {
    playerRef.current = event.target;
  };

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
      <div className="flex-grow-1 d-flex flex-column min-w-0">
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
                          currentUser?.dateFormat
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
        style={{ width: "320px", background: "rgba(0,0,0,0.6)" }}
        className="p-3 d-flex flex-column flex-shrink-0 h-100 overflow-auto"
      >
        <img
          src={activeVideo.thumbnailUrl}
          alt={activeVideo.title}
          className="img-fluid mb-3"
          style={{ borderRadius: "6px" }}
        />
        <h5>{activeVideo.title}</h5>
        <div className="d-flex align-items-center justify-content-between mb-2">
          <h6 className="mb-0">Notes</h6>
          <div className="btn-group btn-group-sm" role="group" aria-label="Note mode">
            <button
              type="button"
              className={`btn ${!previewMode ? "btn-light" : "btn-outline-light"}`}
              onClick={() => setPreviewMode(false)}
            >
              Edit
            </button>
            <button
              type="button"
              className={`btn ${previewMode ? "btn-light" : "btn-outline-light"}`}
              onClick={() => setPreviewMode(true)}
            >
              Preview
            </button>
          </div>
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
            {renderMarkdown(noteDraft)}
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
            value={noteDraft}
            onChange={(e) => {
              setNoteDraft(e.target.value);
              setSaveState("idle");
              setSaveError("");
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
            onClick={handleSave}
            disabled={!isDirty || saveState === "saving"}
          >
            {saveState === "saving" ? "Saving..." : "Save"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default VideoDetail;
