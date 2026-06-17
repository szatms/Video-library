import { useEffect, useState } from "react";
import api from "../../services/api";

function ChannelDetail({channelId, onBack, onOpenVideo}) {
  const [channel, setChannel] = useState(null);
  const [videos, setVideos] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    loadChannel();
  }, [channelId]);

  const formatAddedAt = (dateString) => {
    if (!dateString) {
      return "";
    }

    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  const loadChannel = async () => {
    try {
      const [channelRes, videosRes] = await Promise.all([
        api.get(`/channels/${channelId}`),
        api.get(`/channels/${channelId}/videos`),
      ]);

      setChannel(channelRes.data);
      setVideos(videosRes.data);
      setError("");
    } catch (err) {
      console.error("CHANNEL DETAIL ERROR:", err);
      setError("Could not load channel.");
    }
  };

  if (error) {
    return (
      <div className="p-4">
        <button
          className="btn btn-secondary mb-4"
          onClick={onBack}
        >
          Back
        </button>

        <div className="alert alert-danger">
          {error}
        </div>
      </div>
    );
  }

  if (!channel) {
    return (
      <div className="p-4 text-white">
        Loading...
      </div>
    );
  }

  return (
    <div className="p-4">

      <button
        className="btn btn-secondary mb-4"
        onClick={onBack}
      >
        Back
      </button>

      <div className="d-flex align-items-center gap-4 mb-4">

        <img
          src={channel.channelThumbnail}
          alt={channel.channelTitle}
          className="rounded-circle"
          style={{
            width: "96px",
            height: "96px",
            objectFit: "cover",
          }}
        />

        <div>
          <h1 className="mb-1 text-white">
            {channel.channelTitle}
          </h1>

          <div className="text-light">
            {channel.channelHandle}
          </div>

          <div className="text-light">
            {channel.channelStats?.subscriberCount?.toLocaleString()}
            {" "}subscribers
          </div>

          <div className="text-light">
            {channel.channelStats?.videoCount?.toLocaleString()}
            {" "}videos
          </div>
        </div>

      </div>

      <div className="row">

        <div className="col-lg-6">

          <div className="video-list">

            {videos.map((video) => (
              <div
                key={video.id}
                className="video-list-card ..."
                onClick={() => onOpenVideo(video.id)}
                style={{ cursor: "pointer" }}
              >

                <div className="d-flex gap-3 align-items-start min-w-0 flex-grow-1">

                  <img
                    src={video.video.thumbnailUrl}
                    alt={video.video.title}
                    className="video-list-thumbnail"
                  />

                  <div className="min-w-0">

                    <div className="fw-semibold">
                      {video.video.title}
                    </div>

                    <div className="video-list-channel text-truncate">
                      {video.video.channelTitle}
                    </div>

                    <div className="d-flex flex-wrap gap-3 mt-2 text-muted small">

                      {!video.watched && (
                        <span>Unwatched</span>
                      )}

                      <span>
                        Added: {formatAddedAt(video.addedAt)}
                      </span>

                      <span>
                        {video.video.durationSeconds
                          ? `${Math.floor(
                              video.video.durationSeconds / 60
                            )}:${String(
                              video.video.durationSeconds % 60
                            ).padStart(2, "0")}`
                          : ""}
                      </span>

                      <span>
                        {video.video.viewCount?.toLocaleString()}
                        {" "}views
                      </span>

                    </div>

                  </div>

                </div>

              </div>
            ))}

          </div>

        </div>

      </div>

    </div>
  );
}

export default ChannelDetail;