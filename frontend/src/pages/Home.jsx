import { useState } from "react";
import { logout } from "../services/authService";
import { useNavigate } from "react-router-dom";

import VideoList from "../components/video/VideoList";
import VideoDetail from "../components/video/VideoDetail";

function Home() {
  const navigate = useNavigate();

  const [activeSection, setActiveSection] = useState("videos");
  const [selectedVideo, setSelectedVideo] = useState(null);

  const handleLogout = () => {
    logout();
    navigate("/", { replace: true });
  };

  const handleGoHome = () => {
    setActiveSection("videos");
    setSelectedVideo(null);
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
              setSelectedVideo(null);
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
          <li style={{ opacity: 0.5 }}>Preferences</li>
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
        {activeSection === "videos" && !selectedVideo && (
          <VideoList onSelect={setSelectedVideo} />
        )}

        {activeSection === "videos" && selectedVideo && (
          <VideoDetail
            userVideo={selectedVideo}
            onBack={() => setSelectedVideo(null)}
          />
        )}

        {activeSection !== "videos" && (
          <div className="d-flex justify-content-center align-items-center h-100">
            <h4 className="text-muted">Coming soon...</h4>
          </div>
        )}

      </div>
    </div>
  );
}

export default Home;
