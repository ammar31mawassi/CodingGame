package com.codeescape.engine;

public record NotebookEntry(
        String id,
        String title,
        String summary,
        String pattern,
        int unlockLevelNumber
) {
}
