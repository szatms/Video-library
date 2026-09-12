import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  getDeletedUserVideos,
  getDeletedUserPlaylists,
  getDeletedNotes,
  getDeletedCodices,
  restoreVideo,
  restorePlaylist,
  restoreNote,
  restoreCodex,
} from "../../services/trashService";

function TrashList() {
  const navigate = useNavigate();
  const location = useLocation();
  const [trashItems, setTrashItems] = useState([]);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState("videos");
  const [loading, setLoading] = useState(false);
  const [selectedItems, setSelectedItems] = useState([]);
  const [selectionMode, setSelectionMode] = useState(false);

  useEffect(() => {
    loadTrash();
  }, [activeTab]);

  const formatAddedAt = (dateString) => {
    if (!dateString) {
      return "";
    }

    return new Date(dateString).toLocaleDateString();
  };

  const loadTrash = async () => {
    setLoading(true);
    setError(""); // Clear any previous errors
    try {
      let data = [];
      if (activeTab === "videos") {
        // Get all deleted videos and filter to show only those not in playlists
        const allVideos = await getDeletedUserVideos();
        // Ensure allVideos is an array before filtering
        if (Array.isArray(allVideos)) {
          data = allVideos.filter(item => !item.inPlaylist);
        } else {
          throw new Error("Invalid data received from server");
        }
      } else if (activeTab === "playlists") {
        data = await getDeletedUserPlaylists();
      } else if (activeTab === "notes") {
        data = await getDeletedNotes();
      } else {
        data = await getDeletedCodices();
      }

      setTrashItems(data);
      setSelectedItems([]);
      setSelectionMode(false);
    } catch (err) {
      console.error("TRASH LOAD ERROR:", err);
      if (err.message === "Authentication required") {
        setError("Authentication required. Please log in again.");
      } else {
        setError("Could not load deleted items.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRestore = async (restoreId) => {
    try {
      if (activeTab === "videos") {
        await restoreVideo(restoreId);
      } else if (activeTab === "playlists") {
        await restorePlaylist(restoreId);
      } else if (activeTab === "notes") {
        await restoreNote(restoreId);
      } else {
        await restoreCodex(restoreId);
      }

      setTrashItems((current) =>
        current.filter((item) => item.restoreId !== restoreId)
      );
    } catch (err) {
      console.error("RESTORE ERROR:", err);
      if (err.message === "Authentication required") {
        setError("Authentication required. Please log in again.");
      } else {
        setError("Failed to restore item. Please try again.");
      }
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedItems.length === 0) return;
    
    try {
      // Call the backend endpoint to delete selected items based on active tab
      let deleteEndpoint = '';
      switch (activeTab) {
        case 'videos':
          deleteEndpoint = '/trash/delete/videos';
          break;
        case 'playlists':
          deleteEndpoint = '/trash/delete/playlists';
          break;
        case 'notes':
          deleteEndpoint = '/trash/delete/notes';
          break;
        case 'codices':
          deleteEndpoint = '/trash/delete/codices';
          break;
        default:
          throw new Error('Unknown tab for deletion');
      }
      
      const response = await fetch(`http://localhost:8080/api${deleteEndpoint}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(selectedItems),
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      // Remove deleted items from the UI
      setTrashItems((current) =>
        current.filter((item) => !selectedItems.includes(item.restoreId))
      );
      setSelectedItems([]);
      setSelectionMode(false);
    } catch (err) {
      console.error("DELETE ERROR:", err);
      setError("Failed to delete items. Please try again.");
    }
  };

  const handleSelectItem = (restoreId) => {
    if (selectionMode) {
      if (selectedItems.includes(restoreId)) {
        setSelectedItems(selectedItems.filter(id => id !== restoreId));
      } else {
        setSelectedItems([...selectedItems, restoreId]);
      }
    }
  };

  const handleTrashIconClick = () => {
    // Toggle selection mode for all tabs
    setSelectionMode(!selectionMode);
    if (!selectionMode) {
      setSelectedItems([]);
    }
  };

  return (
    <div className="container-fluid p-4">
      {/* BACK BUTTON */}
      {location.pathname.startsWith("/home/trash") && (
        <div className="mb-3">
          <button 
            className="btn btn-outline-primary" 
            onClick={() => navigate("/home")}
          >
            ← Back
          </button>
        </div>
      )}
      
      <h2 className="mb-4">Recycling Bin</h2>

      {/* TAB NAVIGATION */}
      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === "videos" ? "active" : ""}`}
            onClick={() => setActiveTab("videos")}
          >
            Videos
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === "playlists" ? "active" : ""}`}
            onClick={() => setActiveTab("playlists")}
          >
            Playlists
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === "notes" ? "active" : ""}`}
            onClick={() => setActiveTab("notes")}
          >
            Notes
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === "codices" ? "active" : ""}`}
            onClick={() => setActiveTab("codices")}
          >
            Codices
          </button>
        </li>
      </ul>

      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      ) : trashItems.length === 0 ? (
        <p className="text-muted">
          {activeTab === "videos" 
            ? "No deleted videos." 
            : activeTab === "playlists"
            ? "No deleted playlists."
            : activeTab === "notes"
            ? "No deleted notes."
            : "No deleted codices."}
        </p>
      ) : (
        <div className="video-list" style={{ overflowY: 'auto', flexGrow: 1 }}>
          <div className="d-flex align-items-center mb-3">
            <button
              className={`btn ${selectionMode ? 'btn-danger' : 'btn-outline-danger'}`}
              onClick={handleTrashIconClick}
              style={{ display: 'flex', alignItems: 'center' }}
            >
              <i className="bi bi-trash3" style={{ fontSize: '1.5rem' }}></i>
              {selectionMode && (
                <span className="ms-2">Selected: {selectedItems.length}</span>
              )}
            </button>
            {selectionMode && (
              <button
                className="btn btn-danger ms-2"
                onClick={handleDeleteSelected}
              >
                Delete Selected ({selectedItems.length})
              </button>
            )}
          </div>
          {trashItems.map((item) => (
              <div
                key={item.restoreId}
                className={`video-list-card d-flex gap-3 align-items-start justify-content-between ${selectedItems.includes(item.restoreId) ? 'bg-danger bg-opacity-50 p-3' : ''}`}
                onClick={() => handleSelectItem(item.restoreId)}
              >
              <div className="d-flex gap-3 align-items-start min-w-0 flex-grow-1 position-relative">
                {activeTab === "notes" || activeTab === "codices" ? (
                  <div className="d-flex flex-column align-items-center justify-content-center bg-secondary rounded" style={{ width: '80px', height: '80px' }}>
                    <i className={activeTab === "notes" ? "bi bi-stickies" : "bi bi-book"} style={{ fontSize: '2rem' }}></i>
                  </div>
                ) : (
                  <img
                    src={item.thumbnailUrl || "/default-thumbnail.png"}
                    alt={item.title}
                    className="video-list-thumbnail"
                  />
                )}

                {selectedItems.includes(item.restoreId) && (
                  <div className="position-absolute top-0 end-0 mt-2 me-2 text-danger">
                    <i className="bi bi-check-lg fs-4"></i>
                  </div>
                )}

                <div className="min-w-0">
                  <div className="fw-semibold">
                    {item.title}
                  </div>

                  {item.channelTitle && (
                    <div className="video-list-channel text-truncate">
                      {item.channelTitle}
                    </div>
                  )}

                  <div className="d-flex flex-wrap gap-3 mt-2 text-muted small">
                    <span>
                      Added: {formatAddedAt(item.addedAt)}
                    </span>

                    <span>
                      Deleted: {formatAddedAt(item.deletedAt)}
                    </span>

                    <span>
                      Expires: {formatAddedAt(item.purgeAt)}
                    </span>
                  </div>
                </div>
              </div>

              <button
                type="button"
                className="btn btn-success flex-shrink-0 align-self-center"
                onClick={() => handleRestore(item.restoreId)}
                aria-label={`Restore ${item.title}`}
              >
                <i className="bi bi-arrow-counterclockwise"></i>
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default TrashList;