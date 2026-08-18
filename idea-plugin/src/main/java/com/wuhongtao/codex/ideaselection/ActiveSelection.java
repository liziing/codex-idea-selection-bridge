package com.wuhongtao.codex.ideaselection;

record ActiveSelection(
        String projectRoot,
        String filePath,
        int startLine,
        int endLine,
        long capturedAtEpochMillis,
        boolean dirty
) {
}
