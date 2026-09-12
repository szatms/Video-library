import { useParams, useNavigate } from "react-router-dom";
import VideoDetail from "./VideoDetail";

function VideoDetailWrapper() {
  const { videoId, playlistId, channelId } = useParams();
  const navigate = useNavigate();
  
  const handleBack = () => {
    navigate(-1);
  };
  
  return <VideoDetail userVideoId={videoId} onBack={handleBack} />;
}

export default VideoDetailWrapper;