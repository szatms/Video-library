import { Navigate } from "react-router-dom";
import { isAuthenticated } from "../services/tokenService";

function ProtectedRoute({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/" replace />;
  }

  return children;
}

export default ProtectedRoute;
