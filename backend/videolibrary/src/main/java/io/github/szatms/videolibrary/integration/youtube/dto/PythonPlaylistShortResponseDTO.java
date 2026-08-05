package io.github.szatms.videolibrary.integration.youtube.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PythonPlaylistShortResponseDTO {
    @JsonProperty("items")
    private List<String> items;
    
    @JsonProperty("pageInfo")
    private PageInfo pageInfo;
    
    @Data
    public static class PageInfo {
        @JsonProperty("totalResults")
        private Integer totalResults;
        
        @JsonProperty("resultsPerPage")
        private Integer resultsPerPage;
    }
}