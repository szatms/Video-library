import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from "../../services/api";
import { fetchUserSettings } from "../../services/settings";

const NotesList = () => {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userSettings, setUserSettings] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchNotes = async () => {
      try {
        // Fetch user settings
        const settings = await fetchUserSettings();
        setUserSettings(settings);
        
        const response = await api.get('/notes');
        setNotes(response.data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchNotes();
  }, []);

  const handleNoteClick = (noteId) => {
    navigate(`/notes/${noteId}`);
  };

  const formatDate = (timestamp) => {
    if (!timestamp) return 'N/A';
    const date = new Date(timestamp);
    
    // Handle invalid dates
    if (isNaN(date.getTime())) {
      return 'Invalid date';
    }
    
    const format = userSettings?.dateFormat || "ISO";
    
    switch (format) {
      case "EU":
        return date.toLocaleDateString("hu-HU");
      case "US":
        return date.toLocaleDateString("en-US");
      case "ISO":
      default:
        return date.toISOString().split("T")[0];
    }
  };

  if (loading) {
    return <div className="container mt-4">Loading notes...</div>;
  }

  if (error) {
    return <div className="container mt-4">Error: {error}</div>;
  }

  return (
    <div className="container mt-4">
      <h2>Notes</h2>
      {notes.length === 0 ? (
        <p>No notes found.</p>
      ) : (
        <div className="row">
          {notes.map((note) => (
            <div key={note.id} className="col-md-4 mb-3">
              <div 
                className="card h-100 pointer"
                onClick={() => handleNoteClick(note.id)}
                style={{ cursor: 'pointer' }}
              >
                <div className="card-body">
                  <h5 className="card-title">{note.title}</h5>
                  <p className="card-text">Added: {formatDate(note.addedAt)}</p>
                  <p className="card-text">Updated: {formatDate(note.updatedAt)}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default NotesList;