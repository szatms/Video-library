// Shared utility functions that were duplicated in VideoDetail and PlaylistDetail
export function getSafeExternalUrl(rawUrl) {
  try {
    const parsed = new URL(rawUrl);
    return parsed.protocol === "http:" || parsed.protocol === "https:" ? parsed.href : null;
  } catch {
    return null;
  }
}

// Utility functions only - JSX rendering moved to sharedComponents.jsx
export function formatDate(date, dateFormat) {
    const d = new Date(date);

    switch (dateFormat) {
      case "EU":
        return d.toLocaleDateString("hu-HU");

      case "US":
        return d.toLocaleDateString("en-US");

      case "ISO":
      default:
        return d.toISOString().split("T")[0];
    }
  }

export function formatDateTime(date, dateFormat) {
  if (!date) return 'N/A';
  const d = new Date(date);
  
  switch (dateFormat) {
    case "EU":
      return d.toLocaleString("hu-HU");
    case "US":
      return d.toLocaleString("en-US");
    case "ISO":
    default:
      return d.toISOString().replace('T', ' ').substring(0, 16);
  }
}

export function formatTimestamp(seconds) {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
  }

  return `${minutes}:${String(secs).padStart(2, "0")}`;
}