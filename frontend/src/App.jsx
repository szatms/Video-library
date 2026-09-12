import { BrowserRouter, Routes, Route } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import { useEffect, useState } from "react";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import Init from "./components/user/Init";
import Hub from "./components/hub/Hub";
import VideoList from "./components/video/VideoList";
import VideoDetail from "./components/video/VideoDetail";
import VideoDetailWrapper from "./components/video/VideoDetailWrapper";
import SettingsPanel from "./components/settings/SettingsPanel";
import ChannelList from "./components/channel/ChannelList";
import ChannelDetail from "./components/channel/ChannelDetail";
import TrashList from "./components/trash/TrashList";
import PlaylistList from "./components/playlist/PlaylistList";
import PlaylistDetail from "./components/playlist/PlaylistDetail";
import Notes from "./components/notes/Notes";
import NotesPage from "./components/notes/NotesPage";
import Codices from "./components/codex/Codices";
import CodexDetail from "./components/codex/CodexDetail";
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
          <>
            <Route path="/" element={<Register />} />
            <Route path="/init" element={<Init />} />
            <Route path="*" element={<Register />} />
          </>
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
            <Route index element={<Hub />} />
            <Route path="videos" element={<VideoList />} />
            <Route path="videos/:videoId" element={<VideoDetailWrapper />} />
            <Route path="channels" element={<ChannelList />} />
            <Route path="channels/:channelId" element={<ChannelDetail />} />
            <Route path="channels/:channelId/videos/:videoId" element={<VideoDetailWrapper />} />
            <Route path="channels/:channelId/playlists/:playlistId" element={<PlaylistDetail />} />
            <Route path="channels/:channelId/playlists/:playlistId/videos/:videoId" element={<VideoDetailWrapper />} />
            <Route path="playlists" element={<PlaylistList />} />
            <Route path="playlists/:playlistId" element={<PlaylistDetail />} />
            <Route path="playlists/:playlistId/:videoId" element={<VideoDetailWrapper />} />
            <Route path="settings" element={<SettingsPanel />} />
            <Route path="trash" element={<TrashList />} />
            <Route path="notes" element={<Notes />}>
              <Route index element={<NotesPage />} />
            </Route>
            <Route path="codices" element={<Codices />} />
            <Route path="codices/:codexId" element={<CodexDetail />} />
            <Route path="codices/:codexId/video/:videoId" element={<VideoDetailWrapper />} />
            <Route path="codices/:codexId/playlist/:playlistId" element={<PlaylistDetail />} />
          </Route>
            <Route path="*" element={<Login />} />
          </>
        )}

      </Routes>
    </BrowserRouter>
  );
}

export default App;
