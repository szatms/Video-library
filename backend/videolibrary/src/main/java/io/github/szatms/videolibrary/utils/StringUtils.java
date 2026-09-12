package io.github.szatms.videolibrary.utils;

import org.springframework.stereotype.Component;

@Component
public class StringUtils {
    public String checkTitle(String title) {
        if (title == null) {
            return "Untitled Note";
        }

        // Remove or replace invalid filename characters
        // Windows reserved characters: < > : " | ? * \
        // Also remove control characters
        String sanitized = title.replaceAll("[<>:\"|?*\\\\]", "_")
                .replaceAll("[\\x00-\\x1F\\x7F]", "_");

        // Trim whitespace and limit length
        sanitized = sanitized.trim();
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200);
        }

        // If empty after sanitization, use default
        if (sanitized.isEmpty()) {
            sanitized = "Untitled Note";
        }

        return sanitized;
    }
}
