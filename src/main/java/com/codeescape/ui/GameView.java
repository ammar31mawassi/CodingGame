package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.engine.CollisionManager;
import com.codeescape.engine.GameState;
import com.codeescape.model.Chest;
import com.codeescape.model.ChestReward;
import com.codeescape.model.Door;
import com.codeescape.model.Level;
import com.codeescape.model.MultipleChoiceQuestion;
import com.codeescape.model.Player;
import com.codeescape.model.Room;
import com.codeescape.model.Token;
import com.codeescape.model.TokenType;
import com.codeescape.model.Wall;
import com.codeescape.util.Constants;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameView {
    private static final double TERMINAL_WIDTH = 112;
    private static final double TERMINAL_HEIGHT = 44;
    private static final long PICKUP_MESSAGE_DURATION_NANOS = 2_000_000_000L;
    private static final long NOTIFICATION_STACK_STEP_NANOS = 650_000_000L;
    private static final long NOTIFICATION_REFRESH_GRACE_NANOS = 50_000_000L;
    private static final String JAVA_LIFE_IMAGE_PATH = "/images/java-14-logo-png-transparent.png";
    private static final String BUG_LIFE_IMAGE_PATH = "/images/bug-icon.png";

    private enum ModalType {
        GOAL,
        TERMINAL,
        CHALLENGE
    }

    private final GameApp app;
    private final GameState gameState;
    private final Pane gamePane = new Pane();
    private final CollisionManager collisionManager = new CollisionManager();
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final List<PickupNotification> pickupNotifications = new ArrayList<>();
    private StackPane root;
    private HBox bugHud;
    private Parent activeModal;
    private ModalType activeModalType;
    private CodeBuilderView activeCodeBuilderView;
    private Image javaLifeImage;
    private Image bugLifeImage;
    private AnimationTimer movementTimer;
    private long lastFrameTime;
    private boolean wasTouchingTerminal;

    public GameView(GameApp app, GameState gameState) {
        this.app = app;
        this.gameState = gameState;
    }

    public Parent createView() {
        bugHud = createBugHud();
        renderRoom();

        Button goalButton = createGoalButton();
        VBox gameLayout = new VBox(10, bugHud, gamePane);
        gameLayout.setAlignment(Pos.CENTER);
        gameLayout.getStyleClass().add("game-layout");

        root = new StackPane(gameLayout, goalButton);
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
        addWalls();
        addDoor();
        addChallengeDoor();
        addTerminal();
        addChests();
        addTokens();
        addPlayer();
        addPickupMessage();
        refreshBugHud();
    }

    private void setupKeyboardControls(Parent root) {
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && activeModal != null) {
                closeModal();
                event.consume();
                return;
            }

            KeyCode code = event.getCode();
            if (isMovementKey(code)) {
                pressedKeys.add(code);
                event.consume();
            }
        });

        root.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
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
        if (pressedKeys.isEmpty()) {
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
        double previousX = player.getX();
        double previousY = player.getY();
        player.moveBy(deltaX * movement, deltaY * movement);

        keepPlayerInsideRoom();
        if (collisionManager.hasWallCollision(player, gameState.getCurrentLevel().getRoom())) {
            player.setPosition(previousX, previousY);
        }
        if (handleChallengeDoorCollision(player, previousX, previousY)) {
            renderRoom();
            return;
        }
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
        List<Token> collectedTokens = collisionManager.handleTokenCollection(
                gameState.getPlayer(),
                gameState.getCurrentLevel().getRoom(),
                gameState.getInventory()
        );
        for (Token token : collectedTokens) {
            showPickupMessage(token);
        }

        ChestReward reward = collisionManager.handleChestInteraction(
                gameState.getPlayer(),
                gameState.getCurrentLevel().getRoom(),
                gameState.getInventory()
        );
        if (reward != null) {
            showPickupMessage(reward);
        }

        if (!collectedTokens.isEmpty() || reward != null) {
            refreshActiveModal();
        }
    }

    private boolean handleChallengeDoorCollision(Player player, double previousX, double previousY) {
        Room room = gameState.getCurrentLevel().getRoom();
        Door challengeDoor = room.getChallengeDoor();
        if (!room.hasChallengeDoor()
                || challengeDoor == null
                || !challengeDoor.isLocked()
                || !challengeDoor.intersects(player)) {
            return false;
        }

        player.setPosition(previousX, previousY);
        showChallengeQuestion();
        return true;
    }

    private void showChallengeQuestion() {
        Room room = gameState.getCurrentLevel().getRoom();
        MultipleChoiceQuestion question = room.getChallengeQuestion();
        if (question == null || question.isSolved()) {
            return;
        }
        if (activeModal != null) {
            if (activeModalType == ModalType.CHALLENGE || activeModalType == ModalType.TERMINAL) {
                return;
            }
            closeModal();
        }

        Label title = new Label("Question Door");
        title.getStyleClass().add("modal-title");

        Label prompt = new Label(question.getPrompt());
        prompt.getStyleClass().add("modal-copy");
        prompt.setWrapText(true);

        Label code = new Label(question.getCode());
        code.getStyleClass().add("answer-preview");
        code.setMinHeight(Region.USE_PREF_SIZE);

        VBox choices = new VBox(10);
        for (String choice : question.getChoices()) {
            Button choiceButton = new Button(choice);
            choiceButton.getStyleClass().add("pixel-button");
            choiceButton.setMaxWidth(Double.MAX_VALUE);
            choiceButton.setOnAction(event -> answerChallengeQuestion(choice));
            choices.getChildren().add(choiceButton);
        }

        VBox content = new VBox(14, title, prompt, code, choices);
        content.setMaxWidth(560);
        showModal(content, 640, 420, ModalType.CHALLENGE);
    }

    private void answerChallengeQuestion(String choice) {
        Room room = gameState.getCurrentLevel().getRoom();
        MultipleChoiceQuestion question = room.getChallengeQuestion();
        if (question == null) {
            return;
        }

        if (question.answer(choice)) {
            ChestReward reward = question.getReward();
            room.getChallengeDoor().unlock();
            room.collectReward(reward, gameState.getInventory());
            closeModal();
            setPickupMessage("Correct! Room door is open.", "success-message");
            if (reward != null) {
                setPickupMessage(doorRewardMessage(reward), "pickup-message");
            }
            renderRoom();
            refreshActiveModal();
            return;
        }

        closeModal();
        recordMistake();
    }

    private void openCodeBuilder() {
        if (activeModal != null) {
            if (activeModalType == ModalType.TERMINAL) {
                return;
            }
            closeModal();
        }

        activeCodeBuilderView = new CodeBuilderView(
                gameState.getInventory(),
                gameState.getCurrentLevel().getRoom().getPuzzle(),
                gameState.getCurrentLevel().getRoom().isGoalFound(),
                result -> {
                    gameState.getCurrentLevel().getRoom().getDoor().unlock();
                    closeModal();
                    setPickupMessage("'" + result.getMessage() + "' Door is now open!", "success-message");
                    renderRoom();
                },
                result -> recordMistake()
        );

        Parent builder = activeCodeBuilderView.createView();
        showModal(builder, 920, 620, ModalType.TERMINAL);
    }

    private void showGoalWindow() {
        if (activeModal != null) {
            return;
        }

        Level level = gameState.getCurrentLevel();
        Room room = level.getRoom();
        boolean goalFound = room.isGoalFound();

        Label title = new Label("Goal");
        title.getStyleClass().add("modal-title");

        Label goalLabel = new Label(goalFound
                ? room.getPuzzle().getInstructions()
                : "The goal of this level has not been found yet.");
        goalLabel.getStyleClass().add("modal-copy");
        goalLabel.setWrapText(true);
        goalLabel.setPrefWidth(640);
        goalLabel.setMaxWidth(640);
        goalLabel.setMinHeight(Region.USE_PREF_SIZE);

        VBox content = new VBox(14, title, goalLabel);
        if (goalFound) {
            Label doorLabel = new Label(room.getDoor().isLocked() ? "Door: locked" : "Door: open");
            doorLabel.getStyleClass().add("modal-copy");
            content.getChildren().add(doorLabel);

            if (shouldShowGoalHelper(level)) {
                Label helperLabel = new Label(level.getGoalHelper());
                helperLabel.getStyleClass().add("modal-helper-copy");
                helperLabel.setWrapText(true);
                helperLabel.setPrefWidth(640);
                helperLabel.setMaxWidth(640);
                helperLabel.setMinHeight(Region.USE_PREF_SIZE);
                content.getChildren().add(helperLabel);
            }
        }
        content.setMaxWidth(640);
        showModal(content, 720, 300, ModalType.GOAL);
    }

    private void showModal(Parent content, double width, double height, ModalType modalType) {
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
        activeModalType = modalType;
        root.getChildren().add(modal);
        StackPane.setAlignment(modal, Pos.CENTER);
        Platform.runLater(modal::requestFocus);
    }

    private void closeModal() {
        if (activeModal != null) {
            ModalType closingModalType = activeModalType;
            root.getChildren().remove(activeModal);
            activeModal = null;
            activeModalType = null;
            if (closingModalType == ModalType.TERMINAL) {
                activeCodeBuilderView = null;
            }
        }
        Platform.runLater(root::requestFocus);
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
            if (token.getType() == TokenType.GOAL) {
                tokenTile.getStyleClass().add("room-goal-token");
            } else if (token.getType() == TokenType.HELPER) {
                tokenTile.getStyleClass().add("room-helper-token");
            }

            Label value = new Label(token.getValue());
            value.getStyleClass().add("room-token-text");
            tokenTile.getChildren().add(value);

            gamePane.getChildren().add(tokenTile);
        }
    }

    private void addWalls() {
        for (Wall wall : gameState.getCurrentLevel().getRoom().getWalls()) {
            Rectangle wallShape = new Rectangle(wall.getWidth(), wall.getHeight());
            wallShape.setLayoutX(wall.getX());
            wallShape.setLayoutY(wall.getY());
            wallShape.getStyleClass().add("room-wall");
            gamePane.getChildren().add(wallShape);
        }
    }

    private void addChests() {
        for (Chest chest : gameState.getCurrentLevel().getRoom().getChests()) {
            StackPane chestTile = new StackPane();
            chestTile.setLayoutX(chest.getX());
            chestTile.setLayoutY(chest.getY());
            chestTile.setPrefSize(chest.getWidth(), chest.getHeight());
            chestTile.getStyleClass().add("room-chest");
            if (chest.isOpened()) {
                chestTile.getStyleClass().add("room-chest-open");
            }

            Label mark = new Label(chest.isOpened() ? "" : "?");
            mark.getStyleClass().add("room-chest-text");
            chestTile.getChildren().add(mark);

            gamePane.getChildren().add(chestTile);
        }
    }

    private void addPickupMessage() {
        long now = System.nanoTime();
        pickupNotifications.removeIf(notification -> now > notification.expiresAt);
        if (pickupNotifications.isEmpty()) {
            return;
        }

        double y = 54;
        for (PickupNotification notification : pickupNotifications) {
            Label message = new Label(notification.message);
            double messageWidth = "success-message".equals(notification.styleClass) ? 640 : 360;
            message.setWrapText(true);
            message.setAlignment(Pos.CENTER);
            message.setPrefWidth(messageWidth);
            message.setMaxWidth(messageWidth);
            message.setMinHeight(Region.USE_PREF_SIZE);
            message.setLayoutX(Constants.ROOM_WIDTH / 2.0 - messageWidth / 2.0);
            message.setLayoutY(y);
            message.getStyleClass().add(notification.styleClass);
            gamePane.getChildren().add(message);
            y += notificationHeight(notification) + 10;
        }
    }

    private HBox createBugHud() {
        HBox bugs = new HBox(10);
        bugs.setPrefWidth(Constants.ROOM_WIDTH);
        bugs.setMaxWidth(Constants.ROOM_WIDTH);
        bugs.setMinHeight(46);
        bugs.setAlignment(Pos.CENTER_LEFT);
        bugs.getStyleClass().add("bug-hud");
        refreshBugHud(bugs);
        return bugs;
    }

    private void refreshBugHud() {
        if (bugHud != null) {
            refreshBugHud(bugHud);
        }
    }

    private void refreshBugHud(HBox bugs) {
        bugs.getChildren().clear();

        for (int i = 0; i < 3; i++) {
            StackPane bugSlot = new StackPane();
            bugSlot.setPrefSize(52, 46);
            bugSlot.getStyleClass().add("life-slot");
            if (i < gameState.getBugCount()) {
                bugSlot.getStyleClass().add("bug-life-slot");
                bugSlot.getChildren().add(createBugLifeIcon());
            } else {
                bugSlot.getChildren().add(createJavaLifeIcon());
            }
            bugs.getChildren().add(bugSlot);
        }
    }

    private Node createJavaLifeIcon() {
        ImageView icon = new ImageView(getJavaLifeImage());
        icon.setFitWidth(38);
        icon.setFitHeight(40);
        icon.setPreserveRatio(true);
        icon.setSmooth(false);
        return icon;
    }

    private Image getJavaLifeImage() {
        if (javaLifeImage == null) {
            javaLifeImage = new Image(Objects.requireNonNull(
                    getClass().getResource(JAVA_LIFE_IMAGE_PATH),
                    "Missing Java life image: " + JAVA_LIFE_IMAGE_PATH
            ).toExternalForm());
        }
        return javaLifeImage;
    }

    private Node createBugLifeIcon() {
        ImageView icon = new ImageView(getBugLifeImage());
        icon.setFitWidth(34);
        icon.setFitHeight(34);
        icon.setPreserveRatio(true);
        icon.setSmooth(false);
        return icon;
    }

    private Image getBugLifeImage() {
        if (bugLifeImage == null) {
            bugLifeImage = new Image(Objects.requireNonNull(
                    getClass().getResource(BUG_LIFE_IMAGE_PATH),
                    "Missing bug life image: " + BUG_LIFE_IMAGE_PATH
            ).toExternalForm());
        }
        return bugLifeImage;
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

    private void addChallengeDoor() {
        Room room = gameState.getCurrentLevel().getRoom();
        Door challengeDoor = room.getChallengeDoor();
        if (challengeDoor == null) {
            return;
        }

        Rectangle doorShape = new Rectangle(challengeDoor.getWidth(), challengeDoor.getHeight());
        doorShape.setLayoutX(challengeDoor.getX());
        doorShape.setLayoutY(challengeDoor.getY());
        doorShape.getStyleClass().add(challengeDoor.isLocked() ? "challenge-door-locked" : "challenge-door-open");

        gamePane.getChildren().add(doorShape);
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
            gameState.rewardCleanLevel();
            stopMovementLoop();
            app.showLevelComplete();
        }
    }

    private void stopMovementLoop() {
        if (movementTimer != null) {
            movementTimer.stop();
        }
    }

    private void restartAfterBugFailure() {
        stopMovementLoop();
        PauseTransition restartDelay = new PauseTransition(Duration.seconds(1.4));
        restartDelay.setOnFinished(event -> app.startNewGame());
        restartDelay.play();
    }

    private void recordMistake() {
        gameState.addBug();
        setPickupMessage("OH NO! A BUG!", "bug-message");
        renderRoom();
        if (gameState.hasTooManyBugs()) {
            restartAfterBugFailure();
        }
    }

    private void maybeOpenTerminal() {
        Player player = gameState.getPlayer();
        boolean touchingTerminal = player.intersects(terminalX(), terminalY(), TERMINAL_WIDTH, TERMINAL_HEIGHT);
        if (touchingTerminal && (!wasTouchingTerminal || activeModalType == ModalType.GOAL)) {
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

    private boolean shouldShowGoalHelper(Level level) {
        if (level.getGoalHelper().isBlank()) {
            return false;
        }

        Room room = level.getRoom();
        return !room.hasHiddenHelper() || room.isHelperFound();
    }

    private void refreshActiveModal() {
        if (activeModalType == ModalType.TERMINAL && activeCodeBuilderView != null) {
            activeCodeBuilderView.refresh(gameState.getCurrentLevel().getRoom().isGoalFound());
            return;
        }

        if (activeModalType == ModalType.GOAL) {
            closeModal();
            showGoalWindow();
        }
    }

    private void showPickupMessage(Token token) {
        if (token.isCodeToken()) {
            return;
        }

        String label = token.getType() == TokenType.GOAL ? "Found: Goal" : "Found: Helper";
        setPickupMessage(label, "pickup-message");
    }

    private void showPickupMessage(ChestReward reward) {
        String label = switch (reward.getType()) {
            case CODE -> "Found: " + reward.getValue();
            case GOAL -> "Found: Goal";
            case HELPER -> "Found: Helper";
        };
        setPickupMessage(label, "pickup-message");
    }

    private String doorRewardMessage(ChestReward reward) {
        return switch (reward.getType()) {
            case CODE -> "Collected token '" + reward.getValue() + "'";
            case GOAL -> "Found: Goal";
            case HELPER -> "Found: Helper";
        };
    }

    private void setPickupMessage(String message, String styleClass) {
        if (message == null || message.isBlank()) {
            return;
        }

        long now = System.nanoTime();
        pickupNotifications.removeIf(notification -> now > notification.expiresAt);
        long expiresAt = now
                + PICKUP_MESSAGE_DURATION_NANOS
                + (pickupNotifications.size() * NOTIFICATION_STACK_STEP_NANOS);

        pickupNotifications.add(new PickupNotification(message, styleClass, expiresAt));
        scheduleNotificationRefresh(expiresAt);
    }

    private double notificationHeight(PickupNotification notification) {
        if ("bug-message".equals(notification.styleClass)) {
            return 64;
        }
        if ("success-message".equals(notification.styleClass)) {
            return 56;
        }
        return 48;
    }

    private void scheduleNotificationRefresh(long expiresAt) {
        long refreshAt = expiresAt + NOTIFICATION_REFRESH_GRACE_NANOS;
        double delaySeconds = Math.max(0.05, (refreshAt - System.nanoTime()) / 1_000_000_000.0);
        PauseTransition refresh = new PauseTransition(Duration.seconds(delaySeconds));
        refresh.setOnFinished(event -> {
            pickupNotifications.removeIf(notification -> System.nanoTime() > notification.expiresAt);
            renderRoom();
            refreshActiveModal();
        });
        refresh.play();
    }

    private static class PickupNotification {
        private final String message;
        private final String styleClass;
        private final long expiresAt;

        private PickupNotification(String message, String styleClass, long expiresAt) {
            this.message = message;
            this.styleClass = styleClass;
            this.expiresAt = expiresAt;
        }
    }
}
