function ChannelCard({ channel, onClick }) {
  return (
    <div
      className="channel-card"
      onClick={onClick}
      style={{ cursor: "pointer" }}
    >
      <div className="d-flex align-items-center gap-3">

        <img
          src={channel.channelThumbnail}
          alt={channel.channelTitle}
          className="channel-avatar"
        />

        <div className="flex-grow-1">
          <h5 className="mb-1">
            {channel.channelTitle}
          </h5>

          <div className="text-muted">
            {channel.subscriberCount?.toLocaleString()} subscribers
          </div>
        </div>

      </div>
    </div>
  );
}

export default ChannelCard;