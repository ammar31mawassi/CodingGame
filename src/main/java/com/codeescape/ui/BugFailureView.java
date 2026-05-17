package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.engine.NotebookEntry;
import com.codeescape.engine.NotebookLibrary;
import com.codeescape.model.Level;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class BugFailureView {
    private final GameApp app;
    private final Level failedLevel;

    public BugFailureView(GameApp app, Level failedLevel) {
        this.app = app;
        this.failedLevel = failedLevel;
    }

    public Parent createView() {
        Label title = new Label("Bug Break");
        title.getStyleClass().add("level-complete-title");

        String messageText = failedLevel == null
                ? "Too many bugs this run. Practice, then retry."
                : "Three bugs hit in " + failedLevel.getDisplayId() + ". Practice, then retry clean.";
        Label message = new Label(messageText);
        message.getStyleClass().add("level-complete-message");
        message.setWrapText(true);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setMaxWidth(1040);

        VBox root = new VBox(20, PixelArtView.bug(5), title, message);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("level-complete-screen");

        app.getPracticePromptForLevel(failedLevel).ifPresent(prompt -> {
            VBox practiceCard = PracticeDrillView.create(app, prompt, notebookTitle(prompt.notebookEntryId()), true);
            root.getChildren().add(practiceCard);
        });

        Button retryButton = new Button("Retry Level");
        retryButton.getStyleClass().add("pixel-button");
        retryButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.retryCurrentLevel();
        });

        Button menuButton = new Button("Main Menu");
        menuButton.getStyleClass().add("pixel-button");
        menuButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.showMainMenu();
        });

        HBox actions = new HBox(12, retryButton, menuButton);
        actions.setAlignment(Pos.CENTER);
        root.getChildren().add(actions);
        return scrollable(root);
    }

    private Parent scrollable(VBox content) {
        content.setAlignment(Pos.TOP_CENTER);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("screen-scroll");
        return scrollPane;
    }

    private String notebookTitle(String entryId) {
        Optional<NotebookEntry> entry = NotebookLibrary.find(entryId);
        return entry.map(NotebookEntry::title).orElse("Notebook Pattern");
    }
}
