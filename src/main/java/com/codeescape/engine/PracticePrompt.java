package com.codeescape.engine;

import java.util.List;

public record PracticePrompt(
        String title,
        String summary,
        String task,
        String challengeCode,
        String sampleSolution,
        List<String> acceptedAnswers,
        List<String> coachingTips,
        String notebookEntryId
) {
    public PracticePrompt {
        challengeCode = challengeCode == null ? "" : challengeCode;
        sampleSolution = sampleSolution == null ? "" : sampleSolution;
        acceptedAnswers = acceptedAnswers == null || acceptedAnswers.isEmpty()
                ? List.of(sampleSolution)
                : List.copyOf(acceptedAnswers);
        coachingTips = List.copyOf(coachingTips);
    }
}
