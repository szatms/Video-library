import { useNavigate } from "react-router-dom";
import ChannelList from "../components/channel/ChannelList";

function ChannelListWrapper() {
  const navigate = useNavigate();
  
  const handleBack = () => {
    navigate(-1);
  };
  
  return <ChannelList onBack={handleBack} />;
}

export default ChannelListWrapper;