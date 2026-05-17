package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.engine.NotebookLibrary;
import com.codeescape.engine.PracticePrompt;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PracticeFocusView {
    private final GameApp app;
    private final PracticePrompt prompt;
    private final Integer replayLevelNumber;
    private final String entryTitle;

    public PracticeFocusView(GameApp app, PracticePrompt prompt, Integer replayLevelNumber) {
        this.app = app;
        this.prompt = prompt;
        this.replayLevelNumber = replayLevelNumber;
        this.entryTitle = NotebookLibrary.find(prompt.notebookEntryId())
                .map(notebookEntry -> notebookEntry.title())
                .orElse("Notebook Pattern");
    }

    public Parent createView() {
        Label title = new Label("Focus Route Practice");
        title.getStyleClass().add("level-complete-title");

        Label copy = new Label("Run a targeted drill, then replay the route.");
        copy.getStyleClass().add("level-complete-message");
        copy.setWrapText(true);
        copy.setMaxWidth(860);

        VBox card = PracticeDrillView.create(app, prompt, entryTitle, false);

        Button backButton = new Button("Back To Menu");
        backButton.getStyleClass().add("pixel-button");
        backButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.showMainMenu();
        });

        HBox actions = new HBox(12);
        if (replayLevelNumber != null) {
            Button replayButton = new Button("Replay " + replayLevelNumber);
            replayButton.getStyleClass().add("pixel-button");
            replayButton.setOnAction(event -> {
                SoundManager.play(SoundEffect.BUTTON);
                app.startAtLevel(replayLevelNumber);
            });
            actions.getChildren().add(replayButton);
        }
        actions.getChildren().add(backButton);
        actions.setAlignment(Pos.CENTER);

        VBox root = new VBox(18, title, copy, card, actions);
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("level-complete-screen");
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("screen-scroll");
        return scrollPane;
    }
}
