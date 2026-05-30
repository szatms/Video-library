import { useEffect, useState } from "react";
import { logout } from "../services/authService";
import { useNavigate, useParams } from "react-router-dom";
import api from "../services/api";

import VideoList from "../components/video/VideoList";
import VideoDetail from "../components/video/VideoDetail";
import SettingsPanel from "../components/settings/SettingsPanel";

function Home() {
  const navigate = useNavigate();
  const { videoId } = useParams();

  const [activeSection, setActiveSection] = useState("videos");
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

          <li style={{ opacity: 0.5 }}>Channels</li>
          <li style={{ opacity: 0.5 }}>Playlists</li>
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
            onBack={() => navigate("/home")}
          />
        )}

        {activeSection === "settings" && (
          <SettingsPanel
            currentUser={currentUser}
            loadingUser={loadingUser}
            userError={userError}
            onBack={handleGoHome}
          />
        )}

      </div>
    </div>
  );
}

export default Home;
