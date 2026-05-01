package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.engine.CollisionManager;
import com.codeescape.engine.GameState;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class GameView {
    private final GameApp app;
    private final GameState gameState;
    private final Pane gamePane = new Pane();
    private final InventoryView inventoryView;
    private final CollisionManager collisionManager = new CollisionManager();

    public GameView(GameApp app, GameState gameState) {
        this.app = app;
        this.gameState = gameState;
        this.inventoryView = new InventoryView(gameState.getInventory());
    }

    public Parent createView() {
        Label levelTitle = new Label(gameState.getCurrentLevel().getName());
        Label concept = new Label(gameState.getCurrentLevel().getConcept());
        Button puzzleButton = new Button("Open Puzzle");
        puzzleButton.setOnAction(event -> openCodeBuilder());

        VBox sidebar = new VBox(12, levelTitle, concept, inventoryView.createView(), puzzleButton);
        sidebar.setPadding(new Insets(16));

        renderRoom();

        BorderPane root = new BorderPane();
        root.setCenter(gamePane);
        root.setRight(sidebar);
        return root;
    }

    private void renderRoom() {
        gamePane.getChildren().clear();
        gamePane.setPrefSize(800, 600);
        gamePane.getStyleClass().add("game-room");
    }

    private void setupKeyboardControls() {
    }

    private void updatePlayerPosition() {
    }

    private void checkCollisions() {
        collisionManager.handleTokenCollection(
                gameState.getPlayer(),
                gameState.getCurrentLevel().getRoom(),
                gameState.getInventory()
        );
    }

    private void openCodeBuilder() {
        CodeBuilderView codeBuilderView = new CodeBuilderView(
                gameState.getInventory(),
                gameState.getCurrentLevel().getRoom().getPuzzle()
        );
        gamePane.getChildren().setAll(codeBuilderView.createView());
    }

    private void refresh() {
        inventoryView.refresh();
    }
}
