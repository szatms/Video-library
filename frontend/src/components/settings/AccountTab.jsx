import { useCallback, useEffect, useState } from "react";
import api from "../../services/api";

function AccountTab({ currentUser, setCurrentUser, loadingUser, userError }) {
  const [userSettings, setUserSettings] = useState({
    dateFormat: "EU",
    timeFormat: "H_12"
  });
  const [userData, setUserData] = useState({
    username: ""
  });
  const [passwordData, setPasswordData] = useState({
    newPassword: "",
    confirmPassword: ""
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [deleteConfirmation, setDeleteConfirmation] = useState(false);

  // Auto-dismiss messages after 5 seconds
  useEffect(() => {
    if (error || success) {
      const timer = setTimeout(() => {
        setError(null);
        setSuccess(false);
      }, 5000);
      
      return () => clearTimeout(timer);
    }
  }, [error, success]);

  // Load user settings
  useEffect(() => {
    const loadUserSettings = async () => {
      if (!currentUser || !currentUser.userId) return;
      
      try {
        setLoading(true);
        const response = await api.get(`/settings/user`);
        setUserSettings(response.data);
        setError(null);
      } catch (err) {
        // If user settings don't exist yet, initialize them
        if (err.response?.status === 404) {
          try {
            const initResponse = await api.post(`/settings/user/init`);
            setUserSettings(initResponse.data);
            setError(null);
          } catch (initErr) {
            setError("Failed to initialize user settings");
            console.error("Error initializing user settings:", initErr);
          }
        } else {
          setError("Failed to load user settings");
          console.error("Error loading user settings:", err);
        }
      } finally {
        setLoading(false);
      }
    };

    // Load user data
    const loadUserData = async () => {
      if (!currentUser || !currentUser.userId) return;
      
      try {
        const response = await api.get(`/users/me`);
        setUserData({
          username: response.data.username,
          email: response.data.email
        });
        setError(null);
      } catch (err) {
        setError("Failed to load user data");
        console.error("Error loading user data:", err);
      }
    };

    loadUserSettings();
    loadUserData();
  }, [currentUser]);

  // Handle form changes
  const handleChange = useCallback((field, value) => {
    setUserSettings(prev => ({
      ...prev,
      [field]: value
    }));
  }, []);

  // Handle user data changes
  const handleUserDataChange = useCallback((field, value) => {
    setUserData(prev => ({
      ...prev,
      [field]: value
    }));
  }, []);

  // Handle password changes
  const handlePasswordChange = useCallback((field, value) => {
    setPasswordData(prev => ({
      ...prev,
      [field]: value
    }));
  }, []);

  // Save user settings
  const handleSaveSettings = async (e) => {
    e.preventDefault();
    if (!currentUser || !currentUser.userId) return;

    try {
      setLoading(true);
      setError(null);
      setSuccess(false);
      
      const response = await api.put(`/settings/user`, {
        dateFormat: userSettings.dateFormat,
        timeFormat: userSettings.timeFormat
      });
      
      // Update the currentUser with the new settings
      if (setCurrentUser) {
        setCurrentUser(prev => ({
          ...prev,
          settings: response.data
        }));
      }
      
      setSuccess(true);
    } catch (err) {
      setError("Failed to save user settings");
      console.error("Error saving user settings:", err);
    } finally {
      setLoading(false);
    }
  };

  // Update user data
  const handleUpdateUser = async (e) => {
    e.preventDefault();
    if (!currentUser || !currentUser.userId) return;

    try {
      setLoading(true);
      setError(null);
      setSuccess(false);
      
      const response = await api.put(`/users/me`, {
        username: userData.username
      });
      
      // Update the currentUser with the new data
      if (setCurrentUser) {
        setCurrentUser(response.data);
      }
      
      setSuccess(true);
    } catch (err) {
      setError("Failed to update user data");
      console.error("Error updating user data:", err);
    } finally {
      setLoading(false);
    }
  };

  // Update password
  const handleUpdatePassword = async (e) => {
    e.preventDefault();
    if (!currentUser || !currentUser.userId) return;

    // Validate password fields
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError("New passwords do not match");
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setSuccess(false);
      
      // Use the existing /users/me endpoint with password field in DTO
      await api.put(`/users/me`, {
        username: userData.username,
        password: passwordData.newPassword
      });
      
      // Clear password fields
      setPasswordData({
        newPassword: "",
        confirmPassword: ""
      });
      
      setSuccess(true);
    } catch (err) {
      setError("Failed to update password");
      console.error("Error updating password:", err);
    } finally {
      setLoading(false);
    }
  };

  // Delete user account
  const handleDeleteAccount = async () => {
    try {
      setLoading(true);
      setError(null);
      
      await api.delete(`/users/me`);
      
      // Redirect to login or home page after deletion
      window.location.href = "/";
    } catch (err) {
      setError("Failed to delete account");
      console.error("Error deleting account:", err);
    } finally {
      setLoading(false);
      setDeleteConfirmation(false);
    }
  };

  if (loadingUser || loading) {
    return <div className="p-4">Loading account settings...</div>;
  }

  if (userError || error) {
    return (
      <div className="p-4">
        <div className="alert alert-danger">{userError || error}</div>
      </div>
    );
  }

  return (
    <div className="p-4">
      <h5 className="mb-4">Account Settings</h5>
      
      {success && (
        <div className="alert alert-success mb-3">
          Account settings saved successfully!
        </div>
      )}
      
      {/* User Data Section */}
      <div className="mb-5">
        <h6 className="mb-3">Personal Information</h6>
        <form onSubmit={handleUpdateUser}>
          <div className="mb-3">
            <label htmlFor="username" className="form-label">Username</label>
            <input
              type="text"
              id="username"
              className="form-control"
              value={userData.username}
              onChange={(e) => handleUserDataChange("username", e.target.value)}
            />
          </div>
          
          <button 
            type="submit" 
            className="btn btn-success"
            disabled={loading}
          >
            {loading ? "Saving..." : "Update Information"}
          </button>
        </form>
      </div>
      
      {/* Password Update Section */}
      <div className="mb-5">
        <h6 className="mb-3">Change Password</h6>
        <form onSubmit={handleUpdatePassword}>
          <div className="mb-3">
            <label htmlFor="newPassword" className="form-label">New Password</label>
            <input
              type="password"
              id="newPassword"
              className="form-control"
              value={passwordData.newPassword}
              onChange={(e) => handlePasswordChange("newPassword", e.target.value)}
              required
            />
          </div>
          
          <div className="mb-3">
            <label htmlFor="confirmPassword" className="form-label">Confirm New Password</label>
            <input
              type="password"
              id="confirmPassword"
              className="form-control"
              value={passwordData.confirmPassword}
              onChange={(e) => handlePasswordChange("confirmPassword", e.target.value)}
              required
            />
          </div>
          
          <button 
            type="submit" 
            className="btn btn-success"
            disabled={loading}
          >
            {loading ? "Updating..." : "Update Password"}
          </button>
        </form>
      </div>
      
      {/* Settings Section */}
      <div className="mb-5">
        <h6 className="mb-3">Account Settings</h6>
        <form onSubmit={handleSaveSettings}>
          <div className="mb-3">
            <label htmlFor="dateFormat" className="form-label">Date Format</label>
            <select
              id="dateFormat"
              className="form-select"
              value={userSettings.dateFormat}
              onChange={(e) => handleChange("dateFormat", e.target.value)}
            >
              <option value="EU">DD/MM/YYYY</option>
              <option value="US">MM/DD/YYYY</option>
              <option value="ISO">YYYY-MM-DD</option>
            </select>
          </div>
          
          <div className="mb-3">
            <label htmlFor="timeFormat" className="form-label">Time Format</label>
            <select
              id="timeFormat"
              className="form-select"
              value={userSettings.timeFormat}
              onChange={(e) => handleChange("timeFormat", e.target.value)}
            >
              <option value="H_12">12-hour</option>
              <option value="H_24">24-hour</option>
            </select>
          </div>
          
          <button 
            type="submit" 
            className="btn btn-success"
            disabled={loading}
          >
            {loading ? "Saving..." : "Save Settings"}
          </button>
        </form>
      </div>
      
      {/* Delete Account Section */}
      <div className="border-top pt-3">
        <h6 className="mb-3 text-danger">Delete Account</h6>
        <p className="text-muted small">
          Deleting your account is permanent and cannot be undone. All your data will be removed.
        </p>
        
        {deleteConfirmation ? (
          <div className="d-flex gap-2">
            <button 
              className="btn btn-danger"
              onClick={handleDeleteAccount}
              disabled={loading}
            >
              {loading ? "Deleting..." : "Confirm Delete"}
            </button>
            <button 
              className="btn btn-outline-secondary"
              onClick={() => setDeleteConfirmation(false)}
            >
              Cancel
            </button>
          </div>
        ) : (
          <button 
            className="btn btn-outline-danger"
            onClick={() => setDeleteConfirmation(true)}
          >
            Delete Account
          </button>
        )}
      </div>
    </div>
  );
}

export default AccountTab;