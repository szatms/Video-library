import { useEffect, useState } from "react";
import { logout } from "../services/authService";
import { useNavigate, useParams, useLocation, Outlet } from "react-router-dom";
import api from "../services/api";

import VideoList from "../components/video/VideoList";
import VideoDetail from "../components/video/VideoDetail";
import SettingsPanel from "../components/settings/SettingsPanel";
import ChannelList from "../components/channel/ChannelList";
import ChannelDetail from "../components/channel/ChannelDetail";
import TrashList from "../components/trash/TrashList";

function Home() {
  const navigate = useNavigate();
  const location = useLocation();
  const { videoId, channelId } = useParams();

  // State for channel navigation (keeping for compatibility)
  const [selectedChannelId, setSelectedChannelId] = useState(null);
  const [selectedChannelVideoId, setSelectedChannelVideoId] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [loadingUser, setLoadingUser] = useState(true);
  const [userError, setUserError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    const loadCurrentUser = async () => {
      try {
        const res = await api.get("/users/me", { signal: controller.signal });
        setCurrentUser(res.data);
        setUserError("");
      } catch (err) {
        if (err.code === "ERR_CANCELED") {
          return;
        }

        console.error("CURRENT USER ERROR:", err);
        setUserError("Could not load your account information.");
      } finally {
        if (!controller.signal.aborted) {
          setLoadingUser(false);
        }
      }
    };

    loadCurrentUser();

    return () => {
      controller.abort();
    };
  }, []);

  // Determine current section from URL to keep sidebar active state correct
  const isVideoDetail = !!videoId;
  const isVideosList = location.pathname === "/home/videos" || location.pathname === "/home";
  const isChannelsList = location.pathname === "/home/channels" || 
                        (location.pathname.startsWith("/home/channels/") && !videoId);
  const isChannelDetail = location.pathname.startsWith("/home/channels/") && videoId;
  const isSettings = location.pathname === "/home/settings";
  const isTrash = location.pathname === "/home/trash";

  const handleLogout = () => {
    logout();
    navigate("/", { replace: true });
  };

  const handleGoHome = () => {
    navigate("/home");
  };

  const handleOpenSettings = () => {
    navigate("/home/settings");
  };

  const handleOpenChannels = () => {
    navigate("/home/channels");
  };

  const handleOpenTrash = () => {
    navigate("/home/trash");
  };

  const handleOpenChannel = (channelId) => {
    setSelectedChannelId(channelId);
    navigate(`/home/channels/${channelId}`);
  };

  const handleBackToChannels = () => {
    setSelectedChannelId(null);
    navigate("/home/channels");
  };

  const handleOpenChannelVideo = (videoId) => {
    setSelectedChannelVideoId(videoId);
    if (selectedChannelId) {
      navigate(`/home/channels/${selectedChannelId}/${videoId}`);
    } else {
      navigate(`/home/channels/${channelId}/${videoId}`);
    }
  };

  const handleBackFromVideo = () => {
    // Use browser history to go back
    navigate(-1);
  };

  return (
    <div className="d-flex vh-100 text-light">

      {/* SIDEBAR */}
      <div
        className="p-3 d-flex flex-column"
        style={{
          width: "260px",
          background: "rgba(0,0,0,0.6)",
          borderRight: "1px solid rgba(255,255,255,0.1)"
        }}
      >
        <button
          type="button"
          className="btn btn-link p-0 mb-3 text-white fw-bold text-decoration-none text-start align-self-start"
          onClick={handleGoHome}
        >
          VideoLibrary
        </button>

        <input
          className="form-control mb-3"
          placeholder="Search..."
        />

        <hr />

        {/* LIBRARY */}
        <h6 className="text-white fw-bold">Library</h6>
        <ul className="list-unstyled mb-3">
          <li
            className={`mb-1 ${isVideosList ? "text-decoration-underline" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={() => navigate("/home/videos")}
          >
            Videos
          </li>

          <li
            className={`${isChannelsList || isChannelDetail ? "text-decoration-underline" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={handleOpenChannels}
          >
            Channels
          </li>

          <li style={{ opacity: 0.5 }}>Playlists</li>

          <hr className="my-3" />

          <li
            className={`${isTrash ? "text-decoration-underline" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={handleOpenTrash}
          >
            Recycling Bin
          </li>
        </ul>

        <hr />

        {/* SETTINGS */}
        <h6 className="text-white fw-bold">Settings</h6>
        <ul className="list-unstyled mb-3">
          <li style={{ opacity: 0.5 }}>Profile</li>
          <li
            className={`${isSettings ? "text-decoration-underline" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={handleOpenSettings}
          >
            Preferences
          </li>
        </ul>

        <div className="mt-auto">
          <button className="btn btn-danger w-100" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>

      {/* MAIN CONTENT - Use Outlet for child routes */}
      <div className="flex-grow-1 d-flex flex-column">
        <Outlet />
      </div>
    </div>
  );
}

export default Home;
