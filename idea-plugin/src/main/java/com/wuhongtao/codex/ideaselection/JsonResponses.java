package com.wuhongtao.codex.ideaselection;

final class JsonResponses {
    private JsonResponses() {
    }

    static String health() {
        return "{\"ok\":true,\"service\":\"codex-idea-selection-bridge\"}";
    }

    static String success(ActiveSelection selection) {
        return "{" +
                "\"ok\":true," +
                "\"projectRoot\":\"" + escape(selection.projectRoot()) + "\"," +
                "\"filePath\":\"" + escape(selection.filePath()) + "\"," +
                "\"startLine\":" + selection.startLine() + "," +
                "\"endLine\":" + selection.endLine() + "," +
                "\"capturedAt\":" + selection.capturedAtEpochMillis() +
                "}";
    }

    static String error(String code, String message) {
        return "{" +
                "\"ok\":false," +
                "\"error\":\"" + escape(code) + "\"," +
                "\"message\":\"" + escape(message) + "\"" +
                "}";
    }

    static String fileError(String code, String message, String filePath) {
        return "{" +
                "\"ok\":false," +
                "\"error\":\"" + escape(code) + "\"," +
                "\"message\":\"" + escape(message) + "\"," +
                "\"filePath\":\"" + escape(filePath) + "\"" +
                "}";
    }

    static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
