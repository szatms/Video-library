import { useCallback, useEffect, useMemo, useState } from "react";
import api from "../../services/api";
import AccountTab from "./AccountTab";
import SystemTab from "./SystemTab";

function isPrivilegedUser(user) {
  // Direct approach to check user roles
  if (!user) return false;
  
  // Check for role directly in user object
  const userRole = user.role || user.roles || user.authorities;
  
  // Handle different role structures
  if (Array.isArray(userRole)) {
    // If roles is an array, check if any role is ADMIN or OWNER
    return userRole.some(role => {
      const roleName = typeof role === 'string' ? role : (role.name || role.authority || role.role);
      return roleName && (roleName.toUpperCase() === 'ADMIN' || roleName.toUpperCase() === 'OWNER');
    });
  } else if (typeof userRole === 'string') {
    // If role is a string, check directly
    return userRole.toUpperCase() === 'ADMIN' || userRole.toUpperCase() === 'OWNER';
  }
  
  // If no role found or unrecognized format
  return false;
}

function SettingsPanel({ currentUser, setCurrentUser, loadingUser, userError, onBack }) {
  // Handle potential undefined currentUser
  const user = currentUser || {};
  const privileged = isPrivilegedUser(user);
  
  const [activeTab, setActiveTab] = useState("Account");
  
  const renderContent = () => {
    if (loadingUser) {
      return <div className="p-4">Loading settings...</div>;
    }

    if (userError) {
      return <div className="p-4 text-danger">{userError}</div>;
    }

    if (activeTab === "Account") {
      return (
        <AccountTab 
          currentUser={user} 
          setCurrentUser={setCurrentUser}
          loadingUser={loadingUser}
          userError={userError}
        />
      );
    }

    if (activeTab === "System" && privileged) {
      return (
        <SystemTab 
          currentUser={user} 
          setCurrentUser={setCurrentUser}
          loadingUser={loadingUser}
          userError={userError}
        />
      );
    }

    return (
      <PlaceholderPanel
        title="Settings"
        description="Manage your preferences and settings."
      />
    );
  };

  return (
    <div className="d-flex flex-column h-100 overflow-hidden">
      <div className="px-3 py-2" style={{ background: "rgba(0,0,0,0.45)" }}>
        <button
          type="button"
          className="btn btn-link text-light p-0 text-decoration-none"
          onClick={onBack}
        >
          ← Back
        </button>
      </div>

      <div
        className="px-3 py-3 border-bottom"
        style={{ background: "rgba(0,0,0,0.35)", borderColor: "rgba(255,255,255,0.1)" }}
      >
        <div className="d-flex flex-wrap gap-2">
          <button
            type="button"
            className={`btn btn-sm ${activeTab === "Account" ? "btn-success" : "btn-outline-light"}`}
            onClick={() => setActiveTab("Account")}
          >
            Account
          </button>
          {privileged && (
            <button
              type="button"
              className={`btn btn-sm ${activeTab === "System" ? "btn-success" : "btn-outline-light"}`}
              onClick={() => setActiveTab("System")}
            >
              System
            </button>
          )}
        </div>
      </div>

      <div className="flex-grow-1 overflow-auto p-3">
        {renderContent()}
      </div>
    </div>
  );
}

export default SettingsPanel;

