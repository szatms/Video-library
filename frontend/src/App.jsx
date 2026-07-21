import { BrowserRouter, Routes, Route } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import { useEffect, useState } from "react";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import VideoList from "./components/video/VideoList";
import VideoDetail from "./components/video/VideoDetail";
import VideoDetailWrapper from "./components/video/VideoDetailWrapper";
import SettingsPanel from "./components/settings/SettingsPanel";
import ChannelList from "./components/channel/ChannelList";
import ChannelDetail from "./components/channel/ChannelDetail";
import TrashList from "./components/trash/TrashList";
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
          >
            <Route index element={<VideoList />} />
            <Route path="videos" element={<VideoList />} />
            <Route path="videos/:videoId" element={<VideoDetailWrapper />} />
            <Route path="channels" element={<ChannelList />} />
            <Route path="channels/:channelId" element={<ChannelDetail />} />
            <Route path="channels/:channelId/:videoId" element={<VideoDetailWrapper />} />
            <Route path="settings" element={<SettingsPanel />} />
            <Route path="trash" element={<TrashList />} />
          </Route>
            <Route path="*" element={<Login />} />
          </>
        )}

      </Routes>
    </BrowserRouter>
  );
}

export default App;
