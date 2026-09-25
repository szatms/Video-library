import React, { useState, useEffect } from "react";

function LoadingModal({ 
  show, 
  title = "Loading", 
  message = "Please wait...",
  logs = [],
  progress = 0,
  maxProgress = 100,
  isSuccess = false
 }) {
  if (!show) return null;

  // Calculate percentage for display
  const percentage = Math.min(100, Math.max(0, Math.round((progress / maxProgress) * 100)));

  // Handle flashing effect for success
  const [isFlashing, setIsFlashing] = useState(false);
  
  useEffect(() => {
    if (isSuccess) {
      setIsFlashing(true);
      const flashTimer = setTimeout(() => {
        setIsFlashing(false);
      }, 2000);
      return () => clearTimeout(flashTimer);
    }
  }, [isSuccess]);

  return (
    <div 
      className={`modal show d-block ${isFlashing ? 'flash-success' : ''}`} 
      style={{ backgroundColor: 'rgba(0,0,0,0.7)' }} 
      role="dialog"
    >
      <div className="modal-dialog modal-dialog-centered modal-lg" role="document">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">{title}</h5>
          </div>
          <div className="modal-body">
            <div className="d-flex flex-column gap-3">
              {/* Progress bar */}
              <div>
                <div className="d-flex justify-content-between mb-1">
                  <span>Progress</span>
                  <span>{percentage}%</span>
                </div>
                <div className="progress">
                  <div 
                    className="progress-bar" 
                    role="progressbar" 
                    style={{ width: `${percentage}%` }}
                    aria-valuenow={percentage} 
                    aria-valuemin="0" 
                    aria-valuemax="100"
                  >
                    {percentage}%
                  </div>
                </div>
              </div>

              {/* Status message */}
              <p className="text-center">
                {isSuccess ? "Update done successfully" : message}
              </p>

              {/* Log viewer */}
              <div>
                <h6>Recent Logs:</h6>
                <div className="border rounded p-2" style={{ height: '200px', overflowY: 'auto', backgroundColor: 'rgba(0,0,0,0.2)' }}>
                  {logs.length > 0 ? (
                    logs.map((log, index) => (
                      <div key={index} className="small mb-1" style={{ fontFamily: 'monospace' }}>
                        {log}
                      </div>
                    ))
                  ) : (
                    <div className="text-muted small">No logs yet...</div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoadingModal;