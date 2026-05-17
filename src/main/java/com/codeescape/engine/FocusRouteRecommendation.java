package com.codeescape.engine;

public record FocusRouteRecommendation(
        FocusRouteKind kind,
        String headline,
        String detail,
        String actionLabel,
        String notebookEntryId,
        Integer levelNumber,
        int stageNumber,
        int priorityScore
) {
}
