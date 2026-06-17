import { useEffect, useState } from "react";
import { logout } from "../services/authService";
import { useNavigate, useParams } from "react-router-dom";
import api from "../services/api";

import VideoList from "../components/video/VideoList";
import VideoDetail from "../components/video/VideoDetail";
import SettingsPanel from "../components/settings/SettingsPanel";
import ChannelList from "../components/channel/ChannelList";
import ChannelDetail from "../components/channel/ChannelDetail";
import TrashList from "../components/trash/TrashList";

function Home() {
  const navigate = useNavigate();
  const { videoId } = useParams();

  const [activeSection, setActiveSection] = useState("videos");
  const [currentUser, setCurrentUser] = useState(null);
  const [loadingUser, setLoadingUser] = useState(true);
  const [userError, setUserError] = useState("");

  const [selectedChannelId, setSelectedChannelId] = useState(null);
  const [selectedChannelVideoId, setSelectedChannelVideoId] = useState(null);
  const [selectedVideoId, setSelectedVideoId] = useState(null);
  const [videoSource, setVideoSource] = useState(null);

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

  const handleLogout = () => {
    logout();
    navigate("/", { replace: true });
  };

  const handleGoHome = () => {
    setActiveSection("videos");
    navigate("/home");
  };

  const handleOpenSettings = () => {
    setActiveSection("settings");
    navigate("/home");
  };

  const handleOpenChannels = () => {
    setActiveSection("channels");
    navigate("/home");
  };

  const handleOpenTrash = () => {
    setActiveSection("trash");
    navigate("/home");
  };

  const handleOpenChannel = (channelId) => {
    setSelectedChannelId(channelId);
  };

  const handleBackToChannels = () => {
    setSelectedChannelId(null);
  };

  const handleOpenChannelVideo = (videoId) => {
    setVideoSource("channel");
    setSelectedVideoId(videoId);
  };

  const handleBackFromVideo = () => {
    if (videoSource === "channel") {
      setSelectedVideoId(null);
      return;
    }

    setSelectedVideoId(null);
    setSelectedChannelId(null);
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
            className={`mb-1 ${activeSection === "videos" ? "text-decoration-underline" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={() => {
              setActiveSection("videos");
              navigate("/home");
            }}
          >
            Videos
          </li>

          <li
            className={`${activeSection === "channels" ? "text-decoration-underline" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={handleOpenChannels}
          >
            Channels
          </li>

          <li style={{ opacity: 0.5 }}>Playlists</li>

          <hr className="my-3" />

          <li
            className={`${activeSection === "trash" ? "text-decoration-underline" : ""}`}
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
            className={`${activeSection === "settings" ? "text-decoration-underline" : ""}`}
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

      {/* MAIN CONTENT */}
      <div className="flex-grow-1 d-flex flex-column">

        {/* VIDEOS SECTION */}
        {activeSection === "videos" && !videoId && (
          <VideoList />
        )}

        {activeSection === "videos" && videoId && (
          <VideoDetail
            userVideoId={videoId}
            currentUser={currentUser}
            onBack={() => navigate("/home")}
          />
        )}

        {activeSection === "settings" && (
          <SettingsPanel
            currentUser={currentUser}
            setCurrentUser={setCurrentUser}
            loadingUser={loadingUser}
            userError={userError}
            onBack={handleGoHome}
          />
        )}

        {/* CHANNEL LIST */}
        {activeSection === "channels" &&
         !selectedChannelId &&
         !selectedChannelVideoId && (
          <ChannelList
            onOpenChannel={handleOpenChannel}
          />
        )}

        {/* CHANNEL DETAIL */}
        {activeSection === "channels" &&
         selectedChannelId &&
         !selectedChannelVideoId && (
          <ChannelDetail
            channelId={selectedChannelId}
            onBack={handleBackToChannels}
            onOpenVideo={(userVideoId) =>
              setSelectedChannelVideoId(userVideoId)
            }
          />
        )}

        {/* VIDEO DETAIL FROM CHANNEL */}
        {activeSection === "channels" &&
         selectedChannelVideoId && (
          <VideoDetail
            userVideoId={selectedChannelVideoId}
            currentUser={currentUser}
            onBack={() => setSelectedChannelVideoId(null)}
          />
        )}

        {activeSection === "trash" && (
          <TrashList />
        )}

      </div>
    </div>
  );
}

export default Home;
