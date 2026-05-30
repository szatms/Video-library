import { useCallback, useEffect, useMemo, useState } from "react";
import api from "../../services/api";

const ADMIN_MENUS = ["Videos", "Playlists", "Channels", "Users", "Account", "Preferences"];
const USER_MENUS = ["Account", "Preferences"];
const REFRESH_POLL_MS = 3000;

function normalizeRoleNames(user) {
  const sources = [user?.roles, user?.authorities, user?.role].filter(Boolean);
  const names = sources.flatMap((source) => {
    if (Array.isArray(source)) {
      return source.map((item) => {
        if (typeof item === "string") {
          return item;
        }

        return item?.name ?? item?.authority ?? item?.role ?? "";
      });
    }

    return [source];
  });

  return names
    .filter(Boolean)
    .map((role) => String(role).toUpperCase().replace(/^ROLE_/, ""));
}

function isPrivilegedUser(user) {
  const roles = normalizeRoleNames(user);
  return roles.includes("ADMIN") || roles.includes("OWNER");
}

function getJobIdFromResponse(data) {
  return data?.jobId ?? data?.id ?? data?.job?.id ?? null;
}

function getJobStatusLabel(data) {
  return data?.status ?? data?.state ?? data?.jobStatus ?? "UNKNOWN";
}

function isTerminalStatus(status) {
  return ["COMPLETED", "FAILED", "CANCELLED", "CANCELED"].includes(status);
}

function renderCounts(data) {
  if (!data || typeof data !== "object") {
    return [];
  }

  const keys = ["totalCount", "processedCount", "successCount", "failureCount", "updatedCount", "createdCount", "skippedCount"];
  const countEntries = keys
    .filter((key) => typeof data[key] === "number")
    .map((key) => [key, data[key]]);

  if (countEntries.length > 0) {
    return countEntries;
  }

  return Object.entries(data).filter(([, value]) => typeof value === "number");
}

function formatLabel(value) {
  return value
    .replace(/([A-Z])/g, " $1")
    .replace(/^./, (char) => char.toUpperCase())
    .trim();
}

function StatusCard({ title, children }) {
  return (
    <div
      className="p-3"
      style={{
        background: "rgba(255,255,255,0.04)",
        border: "1px solid rgba(255,255,255,0.08)",
        borderRadius: "8px",
      }}
    >
      <div className="fw-semibold mb-2">{title}</div>
      {children}
    </div>
  );
}

function VideosAdminPanel() {
  const [jobId, setJobId] = useState(null);
  const [jobStatus, setJobStatus] = useState(null);
  const [starting, setStarting] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState(false);
  const [error, setError] = useState("");
  const [showDetailedView, setShowDetailedView] = useState(false);

  const statusLabel = getJobStatusLabel(jobStatus);
  const counts = renderCounts(jobStatus);
  const shouldPoll = Boolean(jobId) && !isTerminalStatus(statusLabel);

  const loadStatus = useCallback(async (targetJobId, silent = false) => {
    if (!targetJobId) {
      return;
    }

    if (!silent) {
      setLoadingStatus(true);
    }

    try {
      const res = await api.get(`/admin/videos/refresh/${targetJobId}`);
      setJobStatus(res.data);
      setError("");
    } catch (err) {
      console.error("VIDEO REFRESH STATUS ERROR:", err);
      setError("Could not load refresh job status.");
    } finally {
      if (!silent) {
        setLoadingStatus(false);
      }
    }
  }, []);

  const handleStartRefresh = async () => {
    setStarting(true);
    setError("");

    try {
      const res = await api.post("/admin/videos/refresh");
      const nextJobId = getJobIdFromResponse(res.data);
      setJobId(nextJobId);
      setJobStatus(res.data);

      if (nextJobId) {
        await loadStatus(nextJobId, true);
      } else {
        setError("Refresh job started, but no job ID was returned by the backend.");
      }
    } catch (err) {
      console.error("VIDEO REFRESH START ERROR:", err);
      setError("Could not start the video refresh job.");
    } finally {
      setStarting(false);
    }
  };

  useEffect(() => {
    if (!shouldPoll) {
      return undefined;
    }

    const timeoutId = window.setTimeout(() => {
      loadStatus(jobId, true);
    }, REFRESH_POLL_MS);

    return () => window.clearTimeout(timeoutId);
  }, [jobId, loadStatus, shouldPoll, statusLabel]);

  return (
    <div className="d-flex flex-column gap-3">
      <div
        className="p-3 d-flex flex-wrap justify-content-between align-items-center gap-3"
        style={{
          background: "rgba(255,255,255,0.04)",
          border: "1px solid rgba(255,255,255,0.08)",
          borderRadius: "8px",
        }}
      >
        <div>
          <div className="fw-semibold">Video refresh</div>
          <div className="text-light-emphasis">Run the backend refresh job and monitor progress from here.</div>
        </div>
        <button
          type="button"
          className="btn btn-success"
          onClick={handleStartRefresh}
          disabled={starting || shouldPoll}
        >
          {starting ? "Starting..." : shouldPoll ? "Refresh Running" : "Start Refresh"}
        </button>
      </div>

      <div
        className="p-3 d-flex justify-content-between align-items-center gap-3"
        style={{
          background: "rgba(255,255,255,0.04)",
          border: "1px solid rgba(255,255,255,0.08)",
          borderRadius: "8px",
        }}
      >
        <div>
          <div className="fw-semibold">Detailed view</div>
          <div className="text-light-emphasis">Show or hide the raw refresh job payload.</div>
        </div>
        <div className="form-check form-switch m-0">
          <input
            id="detailed-view-toggle"
            type="checkbox"
            className="form-check-input"
            checked={showDetailedView}
            onChange={(event) => setShowDetailedView(event.target.checked)}
            style={{
              backgroundColor: showDetailedView ? "#2d6a4f" : "#ffffff",
              borderColor: showDetailedView ? "#2d6a4f" : "#ffffff",
            }}
          />
        </div>
      </div>

      {jobId && (
        <div className="d-flex justify-content-end">
          <button
            type="button"
            className="btn btn-outline-light btn-sm"
            onClick={() => loadStatus(jobId)}
            disabled={loadingStatus}
          >
            {loadingStatus ? "Checking..." : "Check Status"}
          </button>
        </div>
      )}

      {error && <div className="text-danger">{error}</div>}

      <div className="row g-3">
        <div className="col-lg-4">
          <StatusCard title="Job">
            <div className="small text-light-emphasis mb-1">Job ID</div>
            <div className="mb-3">{jobId ?? "-"}</div>
            <div className="small text-light-emphasis mb-1">Status</div>
            <div>{statusLabel}</div>
          </StatusCard>
        </div>

        <div className="col-lg-8">
          <StatusCard title="Counts">
            {loadingStatus && !jobStatus && <div className="text-light-emphasis">Loading job status...</div>}
            {!loadingStatus && counts.length === 0 && (
              <div className="text-light-emphasis">Counts will appear once the backend reports them.</div>
            )}
            {counts.length > 0 && (
              <div className="row g-2">
                {counts.map(([key, value]) => (
                  <div key={key} className="col-sm-6 col-xl-4">
                    <div
                      className="p-2 h-100"
                      style={{
                        background: "rgba(0,0,0,0.2)",
                        border: "1px solid rgba(255,255,255,0.06)",
                        borderRadius: "8px",
                      }}
                    >
                      <div className="small text-light-emphasis">{formatLabel(key)}</div>
                      <div className="fs-5 fw-semibold">{value}</div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </StatusCard>
        </div>
      </div>

      {showDetailedView && jobStatus && (
        <StatusCard title="Raw job payload">
          <pre className="mb-0 text-light small" style={{ whiteSpace: "pre-wrap" }}>
            {JSON.stringify(jobStatus, null, 2)}
          </pre>
        </StatusCard>
      )}
    </div>
  );
}

function PlaceholderPanel({ title, description }) {
  return (
    <div
      className="p-4"
      style={{
        background: "rgba(255,255,255,0.04)",
        border: "1px solid rgba(255,255,255,0.08)",
        borderRadius: "8px",
      }}
    >
      <h5 className="mb-2">{title}</h5>
      <div className="text-light-emphasis">{description}</div>
    </div>
  );
}

function SettingsPanel({ currentUser, loadingUser, userError, onBack }) {
  const privileged = isPrivilegedUser(currentUser);
  const menuItems = useMemo(() => (privileged ? ADMIN_MENUS : USER_MENUS), [privileged]);
  const [activeMenu, setActiveMenu] = useState("Preferences");

  useEffect(() => {
    if (!menuItems.includes(activeMenu)) {
      setActiveMenu(menuItems[0]);
    }
  }, [activeMenu, menuItems]);

  const accountName = currentUser?.username ?? currentUser?.name ?? currentUser?.email ?? "Current user";

  const renderContent = () => {
    if (loadingUser) {
      return <div className="p-4">Loading settings...</div>;
    }

    if (userError) {
      return <div className="p-4 text-danger">{userError}</div>;
    }

    if (activeMenu === "Videos" && privileged) {
      return <VideosAdminPanel />;
    }

    if (activeMenu === "Account") {
      return (
        <PlaceholderPanel
          title="Account"
          description={`${accountName} is signed in. Account-level controls can be added here without changing the surrounding settings navigation.`}
        />
      );
    }

    if (activeMenu === "Preferences") {
      return (
        <PlaceholderPanel
          title="Preferences"
          description="Preference settings live here. The page is wired and role-aware, so additional controls can be added without reworking navigation."
        />
      );
    }

    return (
      <PlaceholderPanel
        title={activeMenu}
        description={`${activeMenu} administration is available in the settings shell and can be expanded here.`}
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
          {menuItems.map((item) => (
            <button
              key={item}
              type="button"
              className={`btn btn-sm ${activeMenu === item ? "btn-success" : "btn-outline-light"}`}
              onClick={() => setActiveMenu(item)}
            >
              {item}
            </button>
          ))}
        </div>
      </div>

      <div className="flex-grow-1 overflow-auto p-3">
        {renderContent()}
      </div>
    </div>
  );
}

export default SettingsPanel;
