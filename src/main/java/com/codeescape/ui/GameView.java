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
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import java.util.HashSet;
import java.util.Set;

public class GameView {
    private static final double TERMINAL_WIDTH = 112;
    private static final double TERMINAL_HEIGHT = 44;

    private final GameApp app;
    private final GameState gameState;
    private final Pane gamePane = new Pane();
    private final CollisionManager collisionManager = new CollisionManager();
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private StackPane root;
    private Parent activeModal;
    private AnimationTimer movementTimer;
    private long lastFrameTime;
    private boolean wasTouchingTerminal;

    public GameView(GameApp app, GameState gameState) {
        this.app = app;
        this.gameState = gameState;
    }

    public Parent createView() {
        renderRoom();

        Button goalButton = createGoalButton();

        root = new StackPane(gamePane, goalButton);
        root.getStyleClass().add("game-screen");
        root.setPadding(new Insets(24));
        root.setFocusTraversable(true);
        StackPane.setAlignment(goalButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(goalButton, new Insets(0, 34, 34, 0));

        setupKeyboardControls(root);
        startMovementLoop();
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
            if (event.getCode() == KeyCode.ESCAPE && activeModal != null) {
                closeModal();
                event.consume();
                return;
            }

            if (activeModal != null) {
                return;
            }

            KeyCode code = event.getCode();
            if (isMovementKey(code)) {
                pressedKeys.add(code);
                event.consume();
            }
        });

        root.setOnKeyReleased(event -> {
            if (isMovementKey(event.getCode())) {
                pressedKeys.remove(event.getCode());
                event.consume();
            }
        });

        root.setOnMouseClicked(event -> root.requestFocus());
        Platform.runLater(root::requestFocus);
    }

    private void startMovementLoop() {
        movementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameTime == 0) {
                    lastFrameTime = now;
                    return;
                }

                double elapsedSeconds = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;
                updateMovement(elapsedSeconds);
            }
        };
        movementTimer.start();
    }

    private void updateMovement(double elapsedSeconds) {
        if (activeModal != null || pressedKeys.isEmpty()) {
            return;
        }

        Player player = gameState.getPlayer();
        double deltaX = 0;
        double deltaY = 0;

        if (pressedKeys.contains(KeyCode.LEFT) || pressedKeys.contains(KeyCode.A)) {
            deltaX -= 1;
        }
        if (pressedKeys.contains(KeyCode.RIGHT) || pressedKeys.contains(KeyCode.D)) {
            deltaX += 1;
        }
        if (pressedKeys.contains(KeyCode.UP) || pressedKeys.contains(KeyCode.W)) {
            deltaY -= 1;
        }
        if (pressedKeys.contains(KeyCode.DOWN) || pressedKeys.contains(KeyCode.S)) {
            deltaY += 1;
        }

        if (deltaX == 0 && deltaY == 0) {
            return;
        }

        if (deltaX != 0 && deltaY != 0) {
            double diagonalScale = 1 / Math.sqrt(2);
            deltaX *= diagonalScale;
            deltaY *= diagonalScale;
        }

        double movement = player.getSpeed() * elapsedSeconds;
        player.moveBy(deltaX * movement, deltaY * movement);

        keepPlayerInsideRoom();
        checkCollisions();
        renderRoom();
        maybeAdvanceLevel();
        maybeOpenTerminal();
    }

    private boolean isMovementKey(KeyCode code) {
        return code == KeyCode.UP
                || code == KeyCode.DOWN
                || code == KeyCode.LEFT
                || code == KeyCode.RIGHT
                || code == KeyCode.W
                || code == KeyCode.A
                || code == KeyCode.S
                || code == KeyCode.D;
    }

    private void checkCollisions() {
        collisionManager.handleTokenCollection(
                gameState.getPlayer(),
                gameState.getCurrentLevel().getRoom(),
                gameState.getInventory()
        );
    }

    private void openCodeBuilder() {
        if (activeModal != null) {
            return;
        }

        CodeBuilderView codeBuilderView = new CodeBuilderView(
                gameState.getInventory(),
                gameState.getCurrentLevel().getRoom().getPuzzle(),
                () -> {
                    gameState.getCurrentLevel().getRoom().getDoor().unlock();
                    renderRoom();
                }
        );

        Parent builder = codeBuilderView.createView();
        showModal(builder, 820, 500);
    }

    private void showGoalWindow() {
        if (activeModal != null) {
            return;
        }

        Level level = gameState.getCurrentLevel();

        Label title = new Label("Goal");
        title.getStyleClass().add("modal-title");

        Label goalLabel = new Label(level.getRoom().getPuzzle().getInstructions());
        goalLabel.getStyleClass().add("modal-copy");
        goalLabel.setWrapText(true);

        Label doorLabel = new Label(level.getRoom().getDoor().isLocked() ? "Door: locked" : "Door: open");
        doorLabel.getStyleClass().add("modal-copy");

        VBox content = new VBox(14, title, goalLabel, doorLabel);
        String example = exampleForLevel(level);
        if (!example.isBlank()) {
            Label buildLabel = new Label("Build:");
            buildLabel.getStyleClass().add("modal-copy");
            Label exampleLabel = new Label(example);
            exampleLabel.getStyleClass().add("answer-preview");
            content.getChildren().addAll(buildLabel, exampleLabel);
        }

        content.setMaxWidth(560);
        showModal(content, 620, 330);
    }

    private void showModal(Parent content, double width, double height) {
        pressedKeys.clear();

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(event -> closeModal());

        HBox closeRow = new HBox(closeButton);
        closeRow.setAlignment(Pos.TOP_LEFT);

        VBox modal = new VBox(12, closeRow, content);
        modal.setPadding(new Insets(16));
        modal.setMaxSize(width, height);
        modal.setPrefSize(width, height);
        modal.getStyleClass().add("map-modal");

        activeModal = modal;
        root.getChildren().add(modal);
        StackPane.setAlignment(modal, Pos.CENTER);
        Platform.runLater(modal::requestFocus);
    }

    private void closeModal() {
        if (activeModal != null) {
            root.getChildren().remove(activeModal);
            activeModal = null;
        }
        pressedKeys.clear();
        Platform.runLater(root::requestFocus);
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
        terminal.setLayoutX(terminalX());
        terminal.setLayoutY(terminalY());
        terminal.setPrefSize(TERMINAL_WIDTH, TERMINAL_HEIGHT);
        terminal.getStyleClass().add("terminal-tile");
        terminal.setOnMouseClicked(event -> openCodeBuilder());

        Label prompt = new Label("Terminal");
        prompt.getStyleClass().add("terminal-text");
        terminal.getChildren().add(prompt);

        gamePane.getChildren().add(terminal);
    }

    private Button createGoalButton() {
        Button button = new Button();
        button.setGraphic(createGoalIcon());
        button.getStyleClass().add("goal-icon-button");
        button.setOnAction(event -> showGoalWindow());
        return button;
    }

    private Node createGoalIcon() {
        StackPane icon = new StackPane();
        icon.setPrefSize(42, 42);

        Circle outer = new Circle(18);
        outer.setFill(Color.TRANSPARENT);
        outer.setStroke(Color.web("#f1ce54"));
        outer.setStrokeWidth(4);

        Circle middle = new Circle(10);
        middle.setFill(Color.TRANSPARENT);
        middle.setStroke(Color.web("#f1ce54"));
        middle.setStrokeWidth(3);

        Circle center = new Circle(3);
        center.setFill(Color.web("#f1ce54"));

        Line vertical = new Line(21, 2, 21, 40);
        vertical.setStroke(Color.web("#f1ce54"));
        vertical.setStrokeWidth(2);

        Line horizontal = new Line(2, 21, 40, 21);
        horizontal.setStroke(Color.web("#f1ce54"));
        horizontal.setStrokeWidth(2);

        icon.getChildren().addAll(outer, middle, vertical, horizontal, center);
        return icon;
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
            stopMovementLoop();
            app.showLevelComplete();
        }
    }

    private void stopMovementLoop() {
        if (movementTimer != null) {
            movementTimer.stop();
        }
    }

    private void maybeOpenTerminal() {
        Player player = gameState.getPlayer();
        boolean touchingTerminal = player.intersects(terminalX(), terminalY(), TERMINAL_WIDTH, TERMINAL_HEIGHT);
        if (touchingTerminal && !wasTouchingTerminal) {
            openCodeBuilder();
        }
        wasTouchingTerminal = touchingTerminal;
    }

    private double terminalX() {
        return Constants.ROOM_WIDTH / 2.0 - TERMINAL_WIDTH / 2.0;
    }

    private double terminalY() {
        return Constants.ROOM_HEIGHT / 2.0 - TERMINAL_HEIGHT / 2.0;
    }
}
