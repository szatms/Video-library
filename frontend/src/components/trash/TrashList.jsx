import { useEffect, useState } from "react";
import {
  getTrashItems,
  restoreTrashItem,
} from "../../services/trashService";

function TrashList() {
  const [trashItems, setTrashItems] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    loadTrash();
  }, []);

  const formatAddedAt = (dateString) => {
    if (!dateString) {
      return "";
    }

    return new Date(dateString).toLocaleDateString();
  };

  const loadTrash = async () => {
    try {
      const data = await getTrashItems();

      setTrashItems(data);
      setError("");
    } catch (err) {
      console.error("TRASH LOAD ERROR:", err);
      setError("Could not load deleted videos.");
    }
  };

  const handleRestore = async (restoreId) => {
    try {
      await restoreTrashItem(restoreId);

      setTrashItems((current) =>
        current.filter((item) => item.id !== restoreId)
      );
    } catch (err) {
      console.error("RESTORE ERROR:", err);
    }
  };

  return (
    <div className="container-fluid p-4">
      <h2 className="mb-4">Recycling Bin</h2>

      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}

      {trashItems.length === 0 ? (
        <p className="text-muted">No deleted videos.</p>
      ) : (
        <div className="video-list">
          {trashItems.map((item) => (
            <div
              key={item.id}
              className="video-list-card d-flex gap-3 align-items-start justify-content-between"
            >
              <div className="d-flex gap-3 align-items-start min-w-0 flex-grow-1">
                <img
                  src={item.thumbnailUrl}
                  alt={item.title}
                  className="video-list-thumbnail"
                />

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
                onClick={() => handleRestore(item.id)}
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