import { useCallback, useEffect, useState } from "react";
import api from "../../services/api";

function SystemTab({ currentUser, setCurrentUser, loadingUser, userError }) {
  const [systemSettings, setSystemSettings] = useState({
    deletionPeriod: null,
    dateFormat: "EU",
    timeFormat: "H_12",
    maxUsers: null
  });
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [userLoading, setUserLoading] = useState(false);
  const [userFetchError, setUserFetchError] = useState(null);
  const [newUser, setNewUser] = useState({
    username: "",
    password: "",
    role: "USER"
  });
  const [newUserError, setNewUserError] = useState(null);
  const [newUserSuccess, setNewUserSuccess] = useState(false);
  const [pendingUpdates, setPendingUpdates] = useState({});

  // Auto-dismiss messages after 5 seconds
  useEffect(() => {
    if (error || success || newUserError || newUserSuccess) {
      const timer = setTimeout(() => {
        setError(null);
        setSuccess(false);
        setNewUserError(null);
        setNewUserSuccess(false);
      }, 5000);
      
      return () => clearTimeout(timer);
    }
  }, [error, success, newUserError, newUserSuccess]);

  // Load system settings
  useEffect(() => {
    const loadSystemSettings = async () => {
      try {
        setLoading(true);
        const response = await api.get(`/settings/app`);
        setSystemSettings(response.data);
        setError(null);
      } catch (err) {
        setError("Failed to load system settings");
        console.error("Error loading system settings:", err);
      } finally {
        setLoading(false);
      }
    };

    loadSystemSettings();
  }, []);

  // Load users
  useEffect(() => {
    const loadUsers = async () => {
      try {
        setUserLoading(true);
        const response = await api.get(`/admin/users`);
        // Ensure response.data is an array
        if (Array.isArray(response.data)) {
          setUsers(response.data);
        } else {
          setUsers([]);
          setUserFetchError("Failed to load users: Invalid data format");
        }
        setUserFetchError(null);
      } catch (err) {
        // Handle 401 Unauthorized specifically
        if (err.response?.status === 401) {
          setUserFetchError("Access denied: Insufficient permissions to view users");
        } else {
          setUserFetchError("Failed to load users");
        }
        console.error("Error loading users:", err);
        setUsers([]); // Reset users to empty array on error
      } finally {
        setUserLoading(false);
      }
    };

    loadUsers();
  }, []);

  // Handle form changes
  const handleChange = useCallback((field, value) => {
    setSystemSettings(prev => ({
      ...prev,
      [field]: value
    }));
  }, []);

  // Handle new user form changes
  const handleNewUserChange = useCallback((field, value) => {
    setNewUser(prev => ({
      ...prev,
      [field]: value
    }));
  }, []);

  // Save system settings
  const handleSaveSettings = async (e) => {
    e.preventDefault();
    if (!currentUser || !currentUser.userId) return;

    try {
      setLoading(true);
      setError(null);
      setSuccess(false);
      
      const response = await api.post(`/admin/settings/app/update`, {
        deletionPeriod: systemSettings.deletionPeriod,
        dateFormat: systemSettings.dateFormat,
        timeFormat: systemSettings.timeFormat,
        maxUsers: systemSettings.maxUsers
      });
      
      setSuccess(true);
    } catch (err) {
      setError("Failed to save system settings");
      console.error("Error saving system settings:", err);
    } finally {
      setLoading(false);
    }
  };

  // Create new user
  const handleCreateUser = async (e) => {
    e.preventDefault();
    
    try {
      // Validate input
      if (!newUser.username || !newUser.password) {
        setNewUserError("Username and password are required");
        return;
      }
      
      setLoading(true);
      setNewUserError(null);
      setNewUserSuccess(false);
      
      const response = await api.post(`/auth/register`, {
        username: newUser.username,
        password: newUser.password,
        role: newUser.role
      });
      
      // Add the new user to the users list
      setUsers(prevUsers => [...prevUsers, {
        userId: response.data.user.userId,
        username: newUser.username,
        role: newUser.role
      }]);
      
      // Reset form
      setNewUser({
        username: "",
        password: "",
        role: "USER"
      });
      
      setNewUserSuccess(true);
    } catch (err) {
      if (err.response?.data?.message) {
        setNewUserError(err.response.data.message);
      } else {
        setNewUserError("Failed to create user");
      }
      console.error("Error creating user:", err);
    } finally {
      setLoading(false);
    }
  };

  // Handle changes to user fields
  const handleUserFieldChange = useCallback((userId, field, value) => {
    setPendingUpdates(prev => ({
      ...prev,
      [userId]: {
        ...(prev[userId] || {}),
        [field]: value
      }
    }));
  }, []);

  // Save user updates
  const handleSaveUserUpdates = async (userId) => {
    try {
      setUserLoading(true);
      
      const userUpdates = pendingUpdates[userId];
      if (!userUpdates) return;
      
      // Get the original user data to ensure all fields are sent
      const originalUser = users.find(u => u.userId === userId);
      if (!originalUser) {
        throw new Error("User not found");
      }
      
      // Create the update payload with all fields
      const updatePayload = {
        username: userUpdates.username !== undefined ? userUpdates.username : originalUser.username,
        password: userUpdates.password !== undefined ? userUpdates.password : (originalUser.password || ""),
        enabled: userUpdates.enabled !== undefined ? userUpdates.enabled : (originalUser.enabled !== undefined ? originalUser.enabled : true),
        role: userUpdates.role !== undefined ? userUpdates.role : originalUser.role
      };
      
      const response = await api.put(`/admin/users/update`, updatePayload);
      
      // Update the user in the list with the new data from response
      setUsers(prevUsers => 
        prevUsers.map(u => 
          u.userId === userId ? { ...u, ...updatePayload } : u
        )
      );
      
      // Clear pending updates
      setPendingUpdates(prev => {
        const newPending = { ...prev };
        delete newPending[userId];
        return newPending;
      });
      
      setSuccess(true);
    } catch (err) {
      setUserFetchError("Failed to update user");
      console.error("Error updating user:", err);
    } finally {
      setUserLoading(false);
    }
  };

  // Manual refresh handler
  const handleManualRefresh = async () => {
    try {
      setLoading(true);
      setError(null);
      setSuccess(false);
      
      await api.post(`/admin/refresh`);
      
      setSuccess("refresh");
    } catch (err) {
      if (err.response?.status === 403) {
        setError("Access denied: Insufficient permissions to refresh");
      } else {
        setError("Failed to trigger refresh");
      }
      console.error("Error triggering refresh:", err);
    } finally {
      setLoading(false);
    }
  };

  // Delete user
  const handleDeleteUser = async (userId) => {
    try {
      setUserLoading(true);
      await api.delete(`/admin/users/delete`, {
        data: [userId]
      });
      
      // Remove the user from the list
      setUsers(prevUsers => 
        prevUsers.filter(user => user.userId !== userId)
      );
      
      setSuccess(true);
    } catch (err) {
      setUserFetchError("Failed to delete user");
      console.error("Error deleting user:", err);
    } finally {
      setUserLoading(false);
    }
  };

  if (loadingUser || loading || userLoading) {
    return <div className="p-4">Loading...</div>;
  }

  if (userError || error || userFetchError || newUserError) {
    return (
      <div className="p-4">
        <div className="alert alert-danger">{userError || error || userFetchError || newUserError}</div>
      </div>
    );
  }

  return (
    <div className="p-4">
      <h5 className="mb-4">System Settings</h5>
      
      {success && (
        <div className="alert alert-success mb-3">
          {userFetchError ? "User updated successfully!" : 
           (success === "refresh" ? "Refresh triggered successfully!" : "System settings saved successfully!")}
        </div>
      )}
      
      {newUserSuccess && (
        <div className="alert alert-success mb-3">
          User created successfully!
        </div>
      )}
      
      <form onSubmit={handleSaveSettings}>
        <div className="mb-3">
          <label htmlFor="deletionPeriod" className="form-label">Deletion Period (days)</label>
          <input
            type="number"
            id="deletionPeriod"
            className="form-control"
            value={systemSettings.deletionPeriod || ""}
            onChange={(e) => handleChange("deletionPeriod", parseInt(e.target.value) || null)}
            min="0"
          />
        </div>
        
        <div className="mb-3">
          <label htmlFor="maxUsers" className="form-label">Maximum Users</label>
          <input
            type="number"
            id="maxUsers"
            className="form-control"
            value={systemSettings.maxUsers || ""}
            onChange={(e) => handleChange("maxUsers", parseInt(e.target.value) || null)}
            min="0"
          />
        </div>
        
        <div className="mb-3">
          <label htmlFor="dateFormat" className="form-label">Date Format</label>
          <select
            id="dateFormat"
            className="form-select"
            value={systemSettings.dateFormat}
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
            value={systemSettings.timeFormat}
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
      
      <hr className="my-4" />
      
      <div className="mb-4">
        <h5 className="mb-3">Manual Refresh</h5>
        <p className="text-white">Trigger an immediate refresh of all videos and playlists from YouTube.</p>
        <button 
          className="btn btn-primary"
          onClick={handleManualRefresh}
          disabled={loading}
        >
          {loading ? "Refreshing..." : "Refresh Now"}
        </button>
      </div>
      
      <h5 className="mb-4">User Management</h5>
      
      {/* New User Form */}
      <div className="card mb-4">
        <div className="card-header">
          <h6 className="mb-0">Add New User</h6>
        </div>
        <div className="card-body">
          <form onSubmit={handleCreateUser}>
            <div className="mb-3">
              <label htmlFor="newUsername" className="form-label">Username</label>
              <input
                type="text"
                id="newUsername"
                className="form-control"
                value={newUser.username}
                onChange={(e) => handleNewUserChange("username", e.target.value)}
                required
              />
            </div>
            
            <div className="mb-3">
              <label htmlFor="newPassword" className="form-label">Password</label>
              <input
                type="password"
                id="newPassword"
                className="form-control"
                value={newUser.password}
                onChange={(e) => handleNewUserChange("password", e.target.value)}
                required
              />
            </div>
            
            <div className="mb-3">
              <label htmlFor="newUserRole" className="form-label">Role</label>
              <select
                id="newUserRole"
                className="form-select"
                value={newUser.role}
                onChange={(e) => handleNewUserChange("role", e.target.value)}
              >
                <option value="USER">User</option>
                <option value="ADMIN">Admin</option>
                <option value="OWNER">Owner</option>
              </select>
            </div>
            
            <button 
              type="submit" 
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? "Creating..." : "Create User"}
            </button>
          </form>
        </div>
      </div>
      
      {userLoading && <div className="mb-3">Loading users...</div>}
      
      {userFetchError && !userLoading && (
        <div className="alert alert-warning mb-3">
          {userFetchError}
        </div>
      )}
      
      {!userFetchError && !userLoading && (
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Password</th>
                <th>Enabled</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.userId} className={user.role === "OWNER" ? "table-warning" : ""}>
                  <td>
                    {user.role === "OWNER" ? (
                      <span>{user.username}</span>
                    ) : (
                      <input
                        type="text"
                        className="form-control form-control-sm"
                        value={pendingUpdates[user.userId]?.username !== undefined ? pendingUpdates[user.userId].username : user.username}
                        onChange={(e) => handleUserFieldChange(user.userId, "username", e.target.value)}
                      />
                    )}
                  </td>
                  <td>
                    {user.role === "OWNER" ? (
                      <span>••••••••</span>
                    ) : (
                      <input
                        type="password"
                        className="form-control form-control-sm"
                        placeholder="Leave blank to keep current"
                        onChange={(e) => handleUserFieldChange(user.userId, "password", e.target.value)}
                      />
                    )}
                  </td>
                  <td>
                    {user.role === "OWNER" ? (
                      <span>{user.enabled !== undefined ? (user.enabled ? "Yes" : "No") : "Yes"}</span>
                    ) : (
                      <input
                        type="checkbox"
                        checked={pendingUpdates[user.userId]?.enabled !== undefined ? pendingUpdates[user.userId].enabled : (user.enabled !== undefined ? user.enabled : true)}
                        onChange={(e) => handleUserFieldChange(user.userId, "enabled", e.target.checked)}
                      />
                    )}
                  </td>
                  <td>
                    {user.role === "OWNER" ? (
                      <span>{user.role}</span>
                    ) : (
                      <select
                        className="form-select form-select-sm"
                        value={pendingUpdates[user.userId]?.role !== undefined ? pendingUpdates[user.userId].role : user.role}
                        onChange={(e) => handleUserFieldChange(user.userId, "role", e.target.value)}
                      >
                        <option value="USER">User</option>
                        <option value="ADMIN">Admin</option>
                        <option value="OWNER">Owner</option>
                      </select>
                    )}
                  </td>
                  <td>
                    {user.role !== "OWNER" && (
                      <>
                        <button
                          className="btn btn-primary btn-sm me-2"
                          onClick={() => handleSaveUserUpdates(user.userId)}
                          disabled={!pendingUpdates[user.userId]}
                        >
                          Save
                        </button>
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => handleDeleteUser(user.userId)}
                          disabled={user.userId === currentUser.userId}
                        >
                          Delete
                        </button>
                      </>
                    )}
                    {user.role === "OWNER" && (
                      <span className="text-muted">Owner account</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default SystemTab;