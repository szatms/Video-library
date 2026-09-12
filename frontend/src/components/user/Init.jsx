import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../services/api";

function Init() {
const [settings, setSettings] = useState({
    id: "",
    deletionPeriod: 30,
    updatePeriod: 7,
    dateFormat: "ISO",
    timeFormat: "H_24",
    maxUsers: 10,
    createdAt: null,
    updatedAt: null,
    updatedById: ""
  });
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  // Fetch current settings
  useEffect(() => {
    const fetchSettings = async () => {
      try {
        const res = await api.get("/settings/app");
        setSettings(res.data);
        setLoading(false);
      } catch (err) {
        // Handle 401 specifically for unauthenticated access
        if (err.response && err.response.status === 401) {
          console.error("Authentication required for settings access:", err);
          // Redirect to login when auth is required
          window.location.href = "/";
        } else {
          console.error("Failed to fetch settings:", err);
          setError("Failed to load settings. Please try again.");
        }
        setLoading(false);
      }
    };

    fetchSettings();
  }, []);

  const handleChange = (field, value) => {
    setSettings(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      // Send updated settings to the backend
      await api.post("/admin/settings/app/update", settings);
      
      // Initialize user settings after app settings are configured
      // The user should be authenticated at this point from the registration
      await api.post("/settings/user/init");
      
      // Redirect to home after successful initialization with full page refresh
      window.location.replace("/home");
    } catch (err) {
      console.error("Failed to initialize settings:", err);
      setError("Failed to initialize application settings. Please try again.");
    }
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "100vh" }}>
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "100vh" }}>
      <div className="card shadow-lg p-4" style={{ width: "500px", borderRadius: "16px" }}>
        <h2 className="text-center mb-4">Initialize Application</h2>
        <p className="text-center text-muted mb-4">
          Please configure your application settings. These can be changed later in the settings panel.
        </p>

        {error && (
          <div className="alert alert-danger mb-3">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Deletion Period (days)</label>
            <input
              type="number"
              className="form-control"
              value={settings.deletionPeriod}
              onChange={(e) => handleChange("deletionPeriod", parseInt(e.target.value) || 0)}
              min="0"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Update Period (days)</label>
            <input
              type="number"
              className="form-control"
              value={settings.updatePeriod}
              onChange={(e) => handleChange("updatePeriod", parseInt(e.target.value) || 0)}
              min="0"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Date Format</label>
            <select
              className="form-select"
              value={settings.dateFormat}
              onChange={(e) => handleChange("dateFormat", e.target.value)}
            >
              <option value="ISO">ISO (YYYY-MM-DD)</option>
              <option value="EU">European (DD/MM/YYYY)</option>
              <option value="US">US (MM/DD/YYYY)</option>
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Time Format</label>
            <select
              className="form-select"
              value={settings.timeFormat}
              onChange={(e) => handleChange("timeFormat", e.target.value)}
            >
              <option value="H_24">24 Hour</option>
              <option value="H_12">12 Hour</option>
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Max Users</label>
            <input
              type="number"
              className="form-control"
              value={settings.maxUsers}
              onChange={(e) => handleChange("maxUsers", parseInt(e.target.value) || 0)}
              min="1"
            />
          </div>

          

          <div className="d-grid gap-2">
            <button type="submit" className="btn btn-success btn-lg">
              Initialize Application
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Init;