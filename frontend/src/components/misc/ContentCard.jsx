import { useState } from "react";

// CSS styles for watched cards
const cardStyles = `
  .video-list-card.watched-card {
    background: #a8e6cf !important;
  }
  
  .video-list-card.hidden-card {
    position: relative;
  }
  
  .video-list-card.hidden-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(128, 128, 128, 0.7);
    filter: blur(4px) grayscale(100%);
    z-index: 1;
    border-radius: 18px;
  }
  
  .video-list-card.hidden-card .button-container {
    position: relative;
    z-index: 2;
  }
  
  .video-list-card.hidden-card img {
    filter: grayscale(100%);
  }
`;

function ContentCard({ 
  title, 
  subtitle, 
  thumbnailUrl, 
  onClick, 
  onToggleWatched, 
  onToggleHidden,
  watched = false,
  hidden = false,
  additionalInfo = [],
  className = "",
  children 
}) {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div>
      <style>{cardStyles}</style>
      <div 
        className={`video-list-card d-flex gap-3 align-items-start justify-content-between mb-3 ${className} ${watched ? 'watched-card' : ''} ${hidden ? 'hidden-card' : ''}`}
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
          <div className="d-flex flex-column align-items-center gap-2 button-container">
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
            <button
              type="button"
              className="btn btn-sm btn-outline-secondary"
              onClick={(e) => {
                e.stopPropagation();
                onToggleHidden && onToggleHidden();
              }}
              title={hidden ? "Show content" : "Hide content"}
            >
              {hidden ? "👁️" : "👁️‍🗨️"}
            </button>
            {children}
          </div>
        )}
      </div>
    </div>
  );
}

export default ContentCard;