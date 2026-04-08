import { BrowserRouter, Routes, Route } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import { useEffect, useState } from "react";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import api from "./services/api";

function App() {
  const [loading, setLoading] = useState(true);
  const [hasUsers, setHasUsers] = useState(null);

  useEffect(() => {
    const checkUsers = async () => {
      try {
        const res = await api.get("/auth/has-users");
        setHasUsers(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    checkUsers();
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <BrowserRouter>
      <Routes>

        {/* FIRST RUN */}
        {!hasUsers && (
          <Route path="*" element={<Register />} />
        )}

        {/* NORMAL FLOW */}
        {hasUsers && (
          <>
            <Route path="/" element={<Login />} />
            <Route
              path="/home"
              element={
                <ProtectedRoute>
                  <Home />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Login />} />
          </>
        )}

      </Routes>
    </BrowserRouter>
  );
}

export default App;