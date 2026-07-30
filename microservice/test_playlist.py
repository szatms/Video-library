#!/usr/bin/env python3

import os
import sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from main import get_playlist_items, PlaylistResponse

# Test that we can import the functions correctly
print("✓ Successfully imported get_playlist_items function")

# Test the PlaylistResponse model
test_response = PlaylistResponse(
    kind="youtube#playlistItemListResponse",
    etag="test-etag",
    items=["https://www.youtube.com/watch?v=test1", "https://www.youtube.com/watch?v=test2"],
    pageInfo={"totalResults": 2, "resultsPerPage": 50}
)

print("✓ PlaylistResponse model works correctly")
print(f"Response kind: {test_response.kind}")
print(f"Number of items: {len(test_response.items)}")