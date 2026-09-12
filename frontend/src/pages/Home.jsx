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
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

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

  const toggleSidebar = () => {
    setSidebarCollapsed(!sidebarCollapsed);
  };

  return (
    <div className="d-flex vh-100 text-light">

      {/* SIDEBAR */}
      <div
        className={`p-3 d-flex flex-column ${sidebarCollapsed ? 'collapsed' : ''}`}
        style={{
          width: sidebarCollapsed ? "60px" : "260px",
          background: "rgba(0,0,0,0.6)",
          borderRight: "1px solid rgba(255,255,255,0.1)",
          transition: "width 0.3s ease",
          position: "relative"
        }}
      >
        {/* VideoLibrary text that navigates to home */}
        <div className="d-flex justify-content-between align-items-center w-100">
          <button
            type="button"
            className="btn btn-link p-0 text-white fw-bold text-decoration-none text-start"
            onClick={handleGoHome}
          >
            <span className="d-flex align-items-center">
              {!sidebarCollapsed && "VideoLibrary"}
            </span>
          </button>
          
          {/* Collapse/Expand button */}
          <button
            type="button"
            className="btn btn-link p-0 text-white fw-bold text-decoration-none text-start"
            onClick={toggleSidebar}
          >
            <span className="d-flex align-items-center">
              {sidebarCollapsed ? "→" : "←"}
            </span>
          </button>
        </div>
        
        {sidebarCollapsed && (
          <div className="d-flex justify-content-center mt-2">
            <button className="btn btn-light" style={{ width: "40px", height: "40px", padding: "8px", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <i className="bi bi-house"></i>
            </button>
          </div>
        )}

        {/* Search bar removed */}

        <hr />

        {/* LIBRARY */}
        {!sidebarCollapsed && <h6 className="text-white fw-bold">Library</h6>}
        <ul className="list-unstyled mb-3">
          <li
            className={`mb-1 ${isVideosList ? "text-decoration-underline" : ""} ${sidebarCollapsed ? "d-flex justify-content-center" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={() => navigate("/home/videos")}
          >
            {!sidebarCollapsed ? "Videos" : (
              <button className="btn btn-light" style={{ width: "40px", height: "40px", padding: "8px", display: "flex", alignItems: "center", justifyContent: "center" }}>
                <i className="bi bi-camera-video"></i>
              </button>
            )}
          </li>

          <li
            className={`${isChannelsList || isChannelDetail ? "text-decoration-underline" : ""} ${sidebarCollapsed ? "d-flex justify-content-center" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={handleOpenChannels}
          >
            {!sidebarCollapsed ? "Channels" : (
              <button className="btn btn-light" style={{ width: "40px", height: "40px", padding: "8px", display: "flex", alignItems: "center", justifyContent: "center" }}>
                <i className="bi bi-person-circle"></i>
              </button>
            )}
          </li>

          <li
            className={`${location.pathname.startsWith("/home/playlists") ? "text-decoration-underline" : ""} ${sidebarCollapsed ? "d-flex justify-content-center" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={() => navigate("/home/playlists")}
          >
            {!sidebarCollapsed ? "Playlists" : (
              <button className="btn btn-light" style={{ width: "40px", height: "40px", padding: "8px", display: "flex", alignItems: "center", justifyContent: "center" }}>
                <i className="bi bi-list"></i>
              </button>
            )}
          </li>

          <hr className="my-3" />

          <li
            className={`${isTrash ? "text-decoration-underline" : ""} ${sidebarCollapsed ? "d-flex justify-content-center" : ""}`}
            style={{ cursor: "pointer" }}
            onClick={handleOpenTrash}
          >
            {!sidebarCollapsed ? "Recycling Bin" : (
              <button className="btn btn-light" style={{ width: "40px", height: "40px", padding: "8px", display: "flex", alignItems: "center", justifyContent: "center" }}>
                <i className="bi bi-trash"></i>
              </button>
            )}
          </li>
        </ul>

        <hr />

        {/* SETTINGS */}
        {!sidebarCollapsed && <h6 className={`text-white fw-bold ${location.pathname === "/home/settings" ? "text-decoration-underline" : ""}`} style={{ cursor: "pointer" }} onClick={handleOpenSettings}>Settings</h6>}
        {sidebarCollapsed && (
          <button
            className="btn btn-link p-0 text-white fw-bold text-decoration-none text-start align-self-start"
            onClick={handleOpenSettings}
          >
            <button className="btn btn-light" style={{ width: "40px", height: "40px", padding: 0, display: "flex", alignItems: "center", justifyContent: "center" }}>
              <i className="bi bi-gear"></i>
            </button>
          </button>
        )}

        <div className="mt-auto">
          {!sidebarCollapsed ? (
            <button className="btn btn-danger w-100" onClick={handleLogout}>
              Logout
            </button>
          ) : (
            <button className="btn btn-danger" onClick={handleLogout} style={{ width: "40px", height: "40px", padding: 0 }}>
              <i className="bi bi-box-arrow-left"></i>
            </button>
          )}
        </div>
      </div>

      {/* MAIN CONTENT - Use Outlet for child routes */}
      <div className="flex-grow-1 d-flex flex-column">
        {isSettings ? (
          <SettingsPanel 
            currentUser={currentUser}
            setCurrentUser={setCurrentUser}
            loadingUser={loadingUser}
            userError={userError}
            onBack={() => navigate(-1)}
          />
        ) : (
          <Outlet />
        )}
      </div>
    </div>
  );
}

export default Home;
