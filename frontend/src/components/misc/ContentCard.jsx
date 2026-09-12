import { useState } from "react";

function ContentCard({ 
  title, 
  subtitle, 
  thumbnailUrl, 
  onClick, 
  onToggleWatched, 
  watched = false,
  additionalInfo = [],
  className = "",
  children 
}) {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div 
      className={`video-list-card d-flex gap-3 align-items-start justify-content-between mb-3 ${className}`}
      onClick={onClick}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="d-flex gap-3 align-items-start min-w-0 flex-grow-1">
        <img
          src={thumbnailUrl}
          alt={title}
          className="video-list-thumbnail"
        />

        <div className="min-w-0">
          <div className="fw-semibold">{title}</div>
          {subtitle && (
            <div className="video-list-channel text-truncate">{subtitle}</div>
          )}
          <div className="d-flex flex-wrap gap-3 mt-2 text-muted small">
            {additionalInfo.map((info, index) => (
              <span key={index}>{info}</span>
            ))}
          </div>
        </div>
      </div>

      {onToggleWatched && (
        <div className="d-flex flex-column align-items-center gap-2">
          <button
            type="button"
            className={`btn btn-sm ${watched ? "btn-success" : "btn-outline-success"}`}
            onClick={(e) => {
              e.stopPropagation();
              onToggleWatched();
            }}
            title={watched ? "Mark as unwatched" : "Mark as watched"}
          >
            {watched ? "✓" : "○"}
          </button>
          {children}
        </div>
      )}
    </div>
  );
}

export default ContentCard;