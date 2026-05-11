package com.codeescape.model;

import java.util.List;

public class MultipleChoiceQuestion {
    private final String prompt;
    private final String code;
    private final List<String> choices;
    private final String correctAnswer;
    private final ChestReward reward;
    private boolean solved;

    public MultipleChoiceQuestion(
            String prompt,
            String code,
            List<String> choices,
            String correctAnswer,
            ChestReward reward
    ) {
        this.prompt = prompt;
        this.code = code;
        this.choices = List.copyOf(choices);
        this.correctAnswer = correctAnswer;
        this.reward = reward;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getCode() {
        return code;
    }

    public List<String> getChoices() {
        return choices;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public ChestReward getReward() {
        return reward;
    }

    public boolean isSolved() {
        return solved;
    }

    public boolean answer(String choice) {
        solved = correctAnswer.equals(choice);
        return solved;
    }
}
