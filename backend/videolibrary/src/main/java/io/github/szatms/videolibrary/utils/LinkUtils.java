package io.github.szatms.videolibrary.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LinkUtils {
    private static final Pattern RAW_ID = Pattern.compile("^[A-Za-z0-9_-]{11}$");
    private static final Pattern WATCH_V_PARAM = Pattern.compile("(?i)(?:\\?|&|#)v=([A-Za-z0-9_-]{11})(?:[&#]|$)");
    private static final Pattern PATH_ID = Pattern.compile(
            "(?i)(?:youtu\\.be/|youtube(?:-nocookie)?\\.com/(?:embed/|shorts/|v/))([A-Za-z0-9_-]{11})(?:\\b|/|\\?|&|#|$)"
    );

    public String getYTVideoId(String link){
        if (link == null) {
            return null;
        }

        String trimmed = link.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (RAW_ID.matcher(trimmed).matches()) {
            return trimmed;
        }

        Matcher queryParamMatch = WATCH_V_PARAM.matcher(trimmed);
        if (queryParamMatch.find()) {
            return queryParamMatch.group(1);
        }

        Matcher pathMatch = PATH_ID.matcher(trimmed);
        if (pathMatch.find()) {
            return pathMatch.group(1);
        }

        return null;
    }
}
