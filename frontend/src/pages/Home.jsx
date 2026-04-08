import { useState } from "react";
import { logout } from "../services/authService";
import { useNavigate } from "react-router-dom";

function Home() {
  const navigate = useNavigate();
  const [layout, setLayout] = useState("default"); // default | cinema

  const handleLogout = () => {
    logout();
    navigate("/", { replace: true });
  };

  return (
    <div className="d-flex vh-100 text-light">

      {/* 🔹 SIDEBAR */}
      <div className="p-3" style={{ width: "250px", background: "rgba(0,0,0,0.6)" }}>
        <h5>Logo</h5>
        <input className="form-control mb-3" placeholder="Search..." />

        <hr />

        <h6>Library</h6>
        <ul className="list-unstyled">
          <li>Playlists</li>
          <li>Channels</li>
          <li>Videos</li>
          <li>Notes</li>
        </ul>

        <hr />

        <h6>Settings</h6>
        <ul className="list-unstyled">
          <li>Profile</li>
          <li>Preferences</li>
        </ul>

        <hr />

        {/* 🔹 Layout switch */}
        <button
          className="btn btn-outline-light w-100 mb-2"
          onClick={() =>
            setLayout(layout === "default" ? "cinema" : "default")
          }
        >
          Switch Layout
        </button>

        <button className="btn btn-danger w-100" onClick={handleLogout}>
          Logout
        </button>
      </div>

      {/* 🔹 MAIN CONTENT */}
      <div className="flex-grow-1 d-flex flex-column">

        {layout === "default" ? (
          <>
            {/* TOP */}
            <div className="flex-grow-1 d-flex">

              {/* VIDEO */}
              <div className="flex-grow-1 d-flex justify-content-center align-items-center">
                <h3>Video Player</h3>
              </div>

              {/* NOTES */}
              <div style={{ width: "300px", background: "rgba(0,0,0,0.6)" }} className="p-3">
                <h5>Notes</h5>
                <p>Markdown notes here...</p>
              </div>
            </div>

            {/* BOTTOM */}
            <div style={{ height: "150px", background: "rgba(0,0,0,0.5)" }} className="p-3">
              <p>Stats / Video info</p>
            </div>
          </>
        ) : (
          <>
            {/* CINEMA MODE */}

            <div className="flex-grow-1 d-flex justify-content-center align-items-center">
              <h2>Big Video Mode</h2>
            </div>

            <div style={{ height: "60px", background: "rgba(0,0,0,0.5)" }} className="d-flex align-items-center px-3">
              <p className="mb-0">Info / Stats / Tabs</p>
            </div>

            <div className="flex-grow-1 p-3">
              <p>Notes / Info / Stats content</p>
            </div>
          </>
        )}

      </div>
    </div>
  );
}

export default Home;