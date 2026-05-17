package com.codeescape.engine;

public record ConceptProgressSnapshot(
        NotebookEntry entry,
        MedalRank bestMedal,
        int hintUsage,
        int bugCount,
        int practiceCompletions,
        boolean recoveryStamp
) {
    public String focusLabel() {
        if (bugCount >= 3 || hintUsage >= 3) {
            return "Needs review";
        }
        if (practiceCompletions > 0 || recoveryStamp) {
            return "Actively improving";
        }
        if (bestMedal == MedalRank.GOLD) {
            return "Strong";
        }
        if (bestMedal == MedalRank.SILVER) {
            return "Stable";
        }
        if (bestMedal == MedalRank.BRONZE) {
            return "Cleared";
        }
        return "Unproven";
    }
}
