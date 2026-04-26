package io.github.szatms.videolibrary.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LinkUtilsTest {
    private static final String ID = "XXXXXXXXXXX";

    private final LinkUtils linkUtils = new LinkUtils();

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/watch?v=XXXXXXXXXXX",
            "https://youtube.com/watch?v=XXXXXXXXXXX",
            "https://www.youtube.com/watch?v=XXXXXXXXXXX&t=32",
            "https://www.youtube.com/watch?v=XXXXXXXXXXX&t=1m20s",
            "https://www.youtube.com/watch?v=XXXXXXXXXXX&list=PL123456",
            "https://www.youtube.com/watch?v=XXXXXXXXXXX&ab_channel=SomeChannel",
            "https://www.youtube.com/watch?v=XXXXXXXXXXX&feature=youtu.be",
            "https://youtu.be/XXXXXXXXXXX",
            "https://youtu.be/XXXXXXXXXXX?t=32",
            "https://youtu.be/XXXXXXXXXXX?si=abcdef",
            "https://youtu.be/XXXXXXXXXXX?si=abcdef&t=32",
            "https://www.youtube.com/shorts/XXXXXXXXXXX",
            "https://youtube.com/shorts/XXXXXXXXXXX",
            "https://www.youtube.com/shorts/XXXXXXXXXXX?feature=share",
            "https://www.youtube.com/embed/XXXXXXXXXXX",
            "https://www.youtube.com/embed/XXXXXXXXXXX?start=30",
            "https://m.youtube.com/watch?v=XXXXXXXXXXX",
            "https://www.youtube.com/watch?time_continue=32&v=XXXXXXXXXXX",
            "https://www.youtube.com/watch?v=XXXXXXXXXXX&time_continue=32",
            "https://www.youtube.com/watch?v=XXXXXXXXXXX#t=32",
            "XXXXXXXXXXX"
    })
    void extractsVideoIdFromSupportedInputs(String input) {
        assertEquals(ID, linkUtils.getYTVideoId(input));
    }

    @Test
    void returnsNullForInvalidOrEmptyInput() {
        assertNull(linkUtils.getYTVideoId(null));
        assertNull(linkUtils.getYTVideoId(""));
        assertNull(linkUtils.getYTVideoId("   "));
        assertNull(linkUtils.getYTVideoId("https://www.youtube.com/watch?list=PL123456"));
        assertNull(linkUtils.getYTVideoId("https://youtu.be/notAnId"));
    }
}
