package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.engine.CollisionManager;
import com.codeescape.engine.GameState;
import com.codeescape.model.Door;
import com.codeescape.model.Level;
import com.codeescape.model.Player;
import com.codeescape.model.Token;
import com.codeescape.util.Constants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameView {
    private final GameApp app;
    private final GameState gameState;
    private final Pane gamePane = new Pane();
    private final InventoryView inventoryView;
    private final CollisionManager collisionManager = new CollisionManager();
    private BorderPane root;
    private VBox sidePanel;

    public GameView(GameApp app, GameState gameState) {
        this.app = app;
        this.gameState = gameState;
        this.inventoryView = new InventoryView(gameState.getInventory());
    }

    public Parent createView() {
        renderRoom();

        VBox leftColumn = new VBox(16, createTitle(), gamePane, inventoryView.createView());
        leftColumn.getStyleClass().add("game-left-column");

        sidePanel = createGoalPanel();

        HBox content = new HBox(22, leftColumn, sidePanel);
        content.setAlignment(Pos.TOP_LEFT);

        root = new BorderPane(content);
        root.getStyleClass().add("game-screen");
        root.setFocusTraversable(true);
        setupKeyboardControls(root);
        BorderPane.setMargin(content, new Insets(24));
        return root;
    }

    private void renderRoom() {
        gamePane.getChildren().clear();
        gamePane.setPrefSize(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT);
        gamePane.setMinSize(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT);
        gamePane.setMaxSize(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT);
        gamePane.getStyleClass().add("game-room");

        addRoomFloor();
        addDoor();
        addTerminal();
        addTokens();
        addPlayer();
    }

    private void setupKeyboardControls(Parent root) {
        root.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            Player player = gameState.getPlayer();

            if (code == KeyCode.UP || code == KeyCode.W) {
                player.moveUp();
            } else if (code == KeyCode.DOWN || code == KeyCode.S) {
                player.moveDown();
            } else if (code == KeyCode.LEFT || code == KeyCode.A) {
                player.moveLeft();
            } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
                player.moveRight();
            } else {
                return;
            }

            keepPlayerInsideRoom();
            checkCollisions();
            renderRoom();
            inventoryView.refresh();
            maybeAdvanceLevel();
            event.consume();
        });

        root.setOnMouseClicked(event -> root.requestFocus());
        Platform.runLater(root::requestFocus);
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
                gameState.getCurrentLevel().getRoom().getPuzzle(),
                () -> {
                    gameState.getCurrentLevel().getRoom().getDoor().unlock();
                    refresh();
                }
        );
        Parent builder = codeBuilderView.createView();

        Button closeButton = new Button("Back");
        closeButton.getStyleClass().add("pixel-button");
        closeButton.setOnAction(event -> {
            showGoalPanel();
            Platform.runLater(root::requestFocus);
        });

        sidePanel.getChildren().setAll(closeButton, builder);
        Platform.runLater(builder::requestFocus);
    }

    private void refresh() {
        inventoryView.refresh();
        renderRoom();
        showGoalPanel();
    }

    private Label createTitle() {
        Level level = gameState.getCurrentLevel();
        Label title = new Label("CODE ESCAPE: " + level.getLevelNumber() + ". " + level.getConcept());
        title.getStyleClass().add("game-title");
        return title;
    }

    private VBox createGoalPanel() {
        Level level = gameState.getCurrentLevel();

        Label roomLabel = new Label("Goal:");
        roomLabel.getStyleClass().add("panel-heading");

        Label goalLabel = new Label(level.getRoom().getPuzzle().getInstructions());
        Label doorLabel = new Label(level.getRoom().getDoor().isLocked() ? "Door: locked" : "Door: open");

        Button puzzleButton = new Button("Open Terminal");
        puzzleButton.getStyleClass().add("pixel-button");
        puzzleButton.setOnAction(event -> openCodeBuilder());

        VBox sidebar = new VBox(12, roomLabel, goalLabel);
        String example = exampleForLevel(level);
        if (!example.isBlank()) {
            Label buildLabel = new Label("Build:");
            Label exampleLabel = new Label(example);
            sidebar.getChildren().addAll(buildLabel, exampleLabel);
        }
        sidebar.getChildren().addAll(doorLabel, puzzleButton);
        sidebar.setPadding(new Insets(18));
        sidebar.setPrefWidth(270);
        sidebar.getStyleClass().add("goal-panel");
        return sidebar;
    }

    private String exampleForLevel(Level level) {
        return switch (level.getLevelNumber()) {
            case 1 -> "int x = 5;";
            case 3 -> "if (x > 5) {}";
            case 4 -> "class Person {}";
            default -> "";
        };
    }

    private void addRoomFloor() {
        Rectangle floor = new Rectangle(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT);
        floor.getStyleClass().add("room-floor");

        Rectangle innerFloor = new Rectangle(Constants.ROOM_WIDTH - 64, Constants.ROOM_HEIGHT - 74);
        innerFloor.setLayoutX(32);
        innerFloor.setLayoutY(36);
        innerFloor.getStyleClass().add("room-inner-floor");

        gamePane.getChildren().addAll(floor, innerFloor);
    }

    private void addTokens() {
        for (Token token : gameState.getCurrentLevel().getRoom().getTokens()) {
            if (token.isCollected()) {
                continue;
            }

            StackPane tokenTile = new StackPane();
            tokenTile.setLayoutX(token.getX());
            tokenTile.setLayoutY(token.getY());
            tokenTile.setPrefSize(token.getWidth(), token.getHeight());
            tokenTile.getStyleClass().add("room-token");

            Label value = new Label(token.getValue());
            value.getStyleClass().add("room-token-text");
            tokenTile.getChildren().add(value);

            gamePane.getChildren().add(tokenTile);
        }
    }

    private void addPlayer() {
        Player player = gameState.getPlayer();
        double x = player.getX();
        double y = player.getY();
        double width = player.getWidth();

        Rectangle head = new Rectangle(16, 14);
        head.setLayoutX(x + 4);
        head.setLayoutY(y);
        head.getStyleClass().add("player-head");

        Rectangle body = new Rectangle(width, 18);
        body.setLayoutX(x);
        body.setLayoutY(y + 14);
        body.getStyleClass().add("player-body");

        Rectangle leftLeg = new Rectangle(8, 8);
        leftLeg.setLayoutX(x + 2);
        leftLeg.setLayoutY(y + 32);
        leftLeg.getStyleClass().add("player-leg");

        Rectangle rightLeg = new Rectangle(8, 8);
        rightLeg.setLayoutX(x + 14);
        rightLeg.setLayoutY(y + 32);
        rightLeg.getStyleClass().add("player-leg");

        gamePane.getChildren().addAll(head, body, leftLeg, rightLeg);
    }

    private void addDoor() {
        Door door = gameState.getCurrentLevel().getRoom().getDoor();

        Rectangle doorShape = new Rectangle(door.getWidth(), door.getHeight());
        doorShape.setLayoutX(door.getX());
        doorShape.setLayoutY(door.getY());
        doorShape.getStyleClass().add(door.isLocked() ? "door-locked" : "door-open");

        Rectangle knob = new Rectangle(16, 24);
        knob.setLayoutX(doorShape.getLayoutX() + 18);
        knob.setLayoutY(doorShape.getLayoutY() + 44);
        knob.setFill(Color.web("#f3d45a"));

        gamePane.getChildren().addAll(doorShape, knob);
    }

    private void addTerminal() {
        StackPane terminal = new StackPane();
        terminal.setLayoutX(Constants.ROOM_WIDTH - 82);
        terminal.setLayoutY(Constants.ROOM_HEIGHT - 78);
        terminal.setPrefSize(54, 44);
        terminal.getStyleClass().add("terminal-tile");
        terminal.setOnMouseClicked(event -> openCodeBuilder());

        Label prompt = new Label(">");
        prompt.getStyleClass().add("terminal-text");
        terminal.getChildren().add(prompt);

        gamePane.getChildren().add(terminal);
    }

    private void keepPlayerInsideRoom() {
        Player player = gameState.getPlayer();
        double maxX = gameState.getCurrentLevel().getRoom().getWidth() - player.getWidth();
        double maxY = gameState.getCurrentLevel().getRoom().getHeight() - player.getHeight();
        double clampedX = Math.max(32, Math.min(player.getX(), maxX - 32));
        double clampedY = Math.max(36, Math.min(player.getY(), maxY - 36));
        player.setPosition(clampedX, clampedY);
    }

    private void maybeAdvanceLevel() {
        if (collisionManager.canExitLevel(
                gameState.getPlayer(),
                gameState.getCurrentLevel().getRoom().getDoor()
        )) {
            gameState.getCurrentLevel().complete();
            app.goToNextLevel();
        }
    }

    private void showGoalPanel() {
        if (sidePanel == null) {
            return;
        }

        VBox goalPanel = createGoalPanel();
        sidePanel.getChildren().setAll(goalPanel.getChildren());
    }
}
