package com.wuhongtao.codex.ideaselection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonResponsesTest {
    @Test
    public void successEscapesPathsAndKeepsLineNumbers() {
        ActiveSelection selection = new ActiveSelection(
                "/tmp/project\"name",
                "/tmp/project\\src/Main.java",
                12,
                18,
                123456789L,
                false
        );

        assertEquals(
                "{\"ok\":true,\"projectRoot\":\"/tmp/project\\\"name\"," +
                        "\"filePath\":\"/tmp/project\\\\src/Main.java\"," +
                        "\"startLine\":12,\"endLine\":18,\"capturedAt\":123456789}",
                JsonResponses.success(selection)
        );
    }

    @Test
    public void errorEscapesControlCharacters() {
        assertEquals(
                "{\"ok\":false,\"error\":\"NO_SELECTION\"," +
                        "\"message\":\"select\\nagain\"}",
                JsonResponses.error("NO_SELECTION", "select\nagain")
        );
    }
}
