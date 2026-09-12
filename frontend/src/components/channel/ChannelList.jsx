import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../../services/api";
import ChannelCard from "./ChannelCard";

function ChannelList() {
  const navigate = useNavigate();
  const location = useLocation();
  const [channels, setChannels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    const loadChannels = async () => {
      try {
        const res = await api.get("/channels", {
          signal: controller.signal
        });

        setChannels(res.data);
        setError("");
      } catch (err) {
        if (err.code === "ERR_CANCELED") {
          return;
        }

        console.error("CHANNEL LOAD ERROR:", err);
        setError("Could not load channels.");
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    loadChannels();

    return () => {
      controller.abort();
    };
  }, []);

  if (loading) {
    return (
      <div className="p-4 text-white">
        Loading channels...
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4 text-danger">
        {error}
      </div>
    );
  }

  return (
    <div className="p-4 text-white" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* BACK BUTTON */}
      {location.pathname.startsWith("/home/channels") && !location.pathname.includes("/channels/") && (
        <div className="mb-3">
          <button 
            className="btn btn-outline-primary" 
            onClick={() => navigate("/home")}
          >
            ← Back
          </button>
        </div>
      )}
      
      <h2 className="mb-4">Channels</h2>

      {channels.length === 0 ? (
        <p>No channels found.</p>
      ) : (
        <div className="channel-list" style={{ overflowY: 'auto', flexGrow: 1 }}>
          {channels.map((channel) => (
            <ChannelCard
              key={channel.channelId}
              channel={channel}
              onClick={() =>
                navigate(`/home/channels/${channel.channelId}`)
              }
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default ChannelList;