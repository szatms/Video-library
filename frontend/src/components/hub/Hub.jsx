import { useNavigate } from "react-router-dom";

function Hub() {
  const navigate = useNavigate();

  const handleNavigate = (path) => {
    navigate(`/home${path}`);
  };

  return (
    <div className="container-fluid">
      <div className="row">
        <div className="col-12">
          <h2 className="mb-4">Dashboard</h2>
        </div>
      </div>
      
      <div className="row">
        <div className="col-md-3 mb-3">
          <div 
            className="card h-100 pointer"
            onClick={() => handleNavigate("/videos")}
            style={{ cursor: "pointer" }}
          >
            <div className="card-body text-center">
              <h5 className="card-title">Videos</h5>
              <p className="card-text">Manage your videos</p>
            </div>
          </div>
        </div>
        
        <div className="col-md-3 mb-3">
          <div 
            className="card h-100 pointer"
            onClick={() => handleNavigate("/playlists")}
            style={{ cursor: "pointer" }}
          >
            <div className="card-body text-center">
              <h5 className="card-title">Playlists</h5>
              <p className="card-text">Manage your playlists</p>
            </div>
          </div>
        </div>
        
        <div className="col-md-3 mb-3">
          <div 
            className="card h-100 pointer"
            onClick={() => handleNavigate("/channels")}
            style={{ cursor: "pointer" }}
          >
            <div className="card-body text-center">
              <h5 className="card-title">Channels</h5>
              <p className="card-text">Manage your channels</p>
            </div>
          </div>
        </div>
        
        <div className="col-md-3 mb-3">
          <div 
            className="card h-100 pointer"
            onClick={() => handleNavigate("/notes")}
            style={{ cursor: "pointer" }}
          >
            <div className="card-body text-center">
              <h5 className="card-title">Notes</h5>
              <p className="card-text">Manage your notes</p>
            </div>
          </div>
        </div>
        
        <div className="col-md-3 mb-3">
          <div 
            className="card h-100 pointer"
            onClick={() => handleNavigate("/codices")}
            style={{ cursor: "pointer" }}
          >
            <div className="card-body text-center">
              <h5 className="card-title">Codices</h5>
              <p className="card-text">Manage your codices</p>
            </div>
          </div>
        </div>
        
        <div className="col-md-3 mb-3">
          <div 
            className="card h-100 pointer"
            onClick={() => handleNavigate("/trash")}
            style={{ cursor: "pointer" }}
          >
            <div className="card-body text-center">
              <h5 className="card-title">Recycling Bin</h5>
              <p className="card-text">View deleted items</p>
            </div>
          </div>
        </div>
        
        <div className="col-md-3 mb-3">
          <div 
            className="card h-100 pointer"
            onClick={() => handleNavigate("/settings")}
            style={{ cursor: "pointer" }}
          >
            <div className="card-body text-center">
              <h5 className="card-title">Settings</h5>
              <p className="card-text">Configure your preferences</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Hub;