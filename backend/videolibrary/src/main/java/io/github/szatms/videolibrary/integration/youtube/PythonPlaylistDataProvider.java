package io.github.szatms.videolibrary.integration.youtube;

import io.github.szatms.videolibrary.integration.youtube.dto.PythonPlaylistResponseDTO;
import io.github.szatms.videolibrary.integration.youtube.dto.PythonPlaylistShortResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PythonPlaylistDataProvider {
    private final RestClient restClient;
    
    public PythonPlaylistResponseDTO load(String playlistId) {
        // Create the full YouTube playlist URL
        String fullUrl = "https://www.youtube.com/playlist?list=" + playlistId;
        
        // Get playlist metadata from /youtube/playlist/long endpoint
        // Using the DTO directly instead of intermediate class
        PythonPlaylistResponseDTO longResponse = restClient.get()
                .uri("/youtube/playlist/long?url=" + fullUrl)
                .retrieve()
                .body(PythonPlaylistResponseDTO.class);
        
        // Get playlist items from /youtube/playlist/short endpoint
        PythonPlaylistShortResponseDTO shortResponse = restClient.get()
                .uri("/youtube/playlist/short?url=" + fullUrl)
                .retrieve()
                .body(PythonPlaylistShortResponseDTO.class);
        
        // Merge data from both responses directly
        PythonPlaylistResponseDTO dto = null;
        
        if (longResponse != null) {
            dto = longResponse;
        }
        
        // Merge data from both responses
        if (dto != null && shortResponse != null) {
            // Add items from short response
            dto.setItems(shortResponse.getItems());
            
            // Merge pageInfo data
            if (shortResponse.getPageInfo() != null) {
                // Create a new PageInfo object to match the DTO structure
                PythonPlaylistResponseDTO.PageInfo pageInfo = new PythonPlaylistResponseDTO.PageInfo();
                pageInfo.setTotalResults(shortResponse.getPageInfo().getTotalResults());
                pageInfo.setResultsPerPage(shortResponse.getPageInfo().getResultsPerPage());
                dto.setPageInfo(pageInfo);
                
                // Set video count from pageInfo if not already set
                if (dto.getVideoCount() == null) {
                    Integer totalResults = shortResponse.getPageInfo().getTotalResults();
                    if (totalResults != null) {
                        dto.setVideoCount(totalResults);
                    } else if (shortResponse.getItems() != null) {
                        // Fallback: use item count as video count
                        dto.setVideoCount(shortResponse.getItems().size());
                    }
                }
            }
        }
        
        return dto;
    }
}
