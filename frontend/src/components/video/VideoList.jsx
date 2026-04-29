import { useEffect, useState } from "react";
import api from "../../services/api";

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
  },
});

function VideoList({ onSelect }) {
  const [videos, setVideos] = useState([]);
  const [input, setInput] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const controller = new AbortController();

    const loadVideos = async () => {
      try {
        const res = await api.get("/uservideos", { signal: controller.signal });
        setVideos(res.data);
      } catch (err) {
        if (err.code === "ERR_CANCELED") {
          return;
        }

        console.error("VIDEO LIST ERROR:", err);
        setError("Could not load your videos.");
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    loadVideos();

    return () => {
      controller.abort();
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
      setVideos((current) => [toSummaryItem(res.data), ...current]);
      setInput("");
    } catch (err) {
      console.error("VIDEO ADD ERROR:", err);
      setError("Could not add the video.");
    }
  };

  return (
    <div className="p-3">

      {/* ADD PANEL */}
      <div className="mb-3 d-flex gap-2">
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

      {error && (
        <div className="text-danger mb-3">
          {error}
        </div>
      )}

      {/* LIST */}
      <div className="list-group">
        {!loading && videos.length === 0 && (
          <div className="text-muted">No videos yet.</div>
        )}

        {videos.map((v) => (
          <div
            key={v.id}
            className="list-group-item list-group-item-action d-flex gap-3 align-items-start"
            onClick={() => onSelect(v)}
            style={{ cursor: "pointer" }}
          >
            <img
              src={v.video.thumbnailUrl}
              alt={v.video.title}
              style={{
                width: "120px",
                height: "68px",
                objectFit: "cover",
                borderRadius: "6px",
                flexShrink: 0,
              }}
            />

            <div className="min-w-0">
              <div className="fw-semibold">{v.video.title}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default VideoList;
