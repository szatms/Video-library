import ContentCard from "./ContentCard";

// Example usage:
const exampleCard = (
  <ContentCard 
    title="Example Video Title"
    subtitle="Channel Name"
    thumbnailUrl="https://example.com/thumbnail.jpg"
    onClick={() => console.log("Card clicked")}
    onToggleWatched={() => console.log("Watched status toggled")}
    watched={false}
    additionalInfo={["10 minutes ago", "15 views"]}
  />
);