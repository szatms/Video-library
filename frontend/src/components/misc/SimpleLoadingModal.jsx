import React from "react";

const SimpleLoadingModal = ({ show, message = "Adding content, please wait..." }) => {
  if (!show) return null;

  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1050 }} tabIndex="-1">
      <div className="modal-dialog" style={{ marginTop: '20vh' }}>
        <div className="modal-content">
          <div className="modal-body text-center">
            <div className="spinner-border text-primary mb-3" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
            <p>{message}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SimpleLoadingModal;