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
import com.codeescape.model.ProgrammableObject;
import com.codeescape.model.Room;
import com.codeescape.model.Token;
import com.codeescape.model.TokenDescriptions;
import com.codeescape.model.TokenType;
import com.codeescape.model.Wall;
import com.codeescape.util.Constants;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.util.Duration;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameView {
    private static final double TERMINAL_WIDTH = 112;
    private static final double TERMINAL_HEIGHT = 44;
    private static final double TERMINAL_MARGIN_RIGHT = 24;
    private static final double TERMINAL_MARGIN_BOTTOM = 26;
    private static final double INVENTORY_WIDTH = 224;
    private static final double INVENTORY_HEIGHT = 54;
    private static final double INVENTORY_MARGIN_BOTTOM = 6;
    private static final double BOTTOM_CONTROL_MARGIN = 86;
    private static final double GOAL_MARGIN_LEFT = 42;
    private static final double GOAL_MARGIN_BOTTOM = 8;
    private static final double HARD_MODE_LIGHT_RADIUS = 123.75;
    private static final double BUG_HUD_HEIGHT = 46;
    private static final double GAME_SURFACE_TOP_PADDING = 12;
    private static final double GAME_SURFACE_HUD_GAP = 10;
    private static final double GAME_SURFACE_WIDTH = Constants.ROOM_WIDTH;
    private static final double GAME_SURFACE_HEIGHT = Constants.ROOM_HEIGHT
            + BUG_HUD_HEIGHT
            + GAME_SURFACE_TOP_PADDING
            + GAME_SURFACE_HUD_GAP;
    private static final double FULLSCREEN_MARGIN = 14;
    private static final long PICKUP_MESSAGE_DURATION_NANOS = 2_000_000_000L;
    private static final long NOTIFICATION_STACK_STEP_NANOS = 650_000_000L;
    private static final long NOTIFICATION_REFRESH_GRACE_NANOS = 50_000_000L;
    private static final double ADVICE_TOAST_SECONDS = 7.0;
    private static final double CHALLENGE_MODAL_WIDTH = 760;
    private static final double CHALLENGE_CONTENT_WIDTH = 680;
    private static final double CHALLENGE_MIN_MODAL_HEIGHT = 420;
    private static final double CHALLENGE_MAX_MODAL_HEIGHT = 620;
    private static final int CHALLENGE_PROMPT_CHARS_PER_LINE = 54;
    private static final String JAVA_LIFE_IMAGE_PATH = "/images/java-14-logo-png-transparent.png";
    private static final String BUG_LIFE_IMAGE_PATH = "/images/bug-icon.png";

    private enum ModalType {
        GOAL,
        TERMINAL,
        OBJECT_TERMINAL,
        CHALLENGE,
        TUTORIAL,
        PAUSE
    }

    private final GameApp app;
    private final GameState gameState;
    private final Pane gamePane = new Pane();
    private final CollisionManager collisionManager = new CollisionManager();
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final List<PickupNotification> pickupNotifications = new ArrayList<>();
    private StackPane root;
    private StackPane gameSurface;
    private HBox bugHud;
    private Label inventoryButtonLabel;
    private VBox adviceToastLayer;
    private Parent inventoryPanel;
    private Label inventoryDescriptionLabel;
    private Parent activeModal;
    private ModalType activeModalType;
    private CodeBuilderView activeCodeBuilderView;
    private ProgrammableObject activeProgrammableObject;
    private Image javaLifeImage;
    private Image bugLifeImage;
    private AnimationTimer movementTimer;
    private long lastFrameTime;
    private boolean wasTouchingTerminal;
    private boolean wasTouchingProgrammableObject;

    public GameView(GameApp app, GameState gameState) {
        this.app = app;
        this.gameState = gameState;
    }

    public Parent createView() {
        bugHud = createBugHud();
        renderRoom();

        Button goalButton = createGoalButton();
        adviceToastLayer = createAdviceToastLayer();
        VBox playfield = new VBox(GAME_SURFACE_HUD_GAP, bugHud, gamePane);
        playfield.setPadding(new Insets(GAME_SURFACE_TOP_PADDING, 0, 0, 0));
        playfield.setAlignment(Pos.TOP_LEFT);
        playfield.setMinSize(GAME_SURFACE_WIDTH, GAME_SURFACE_HEIGHT);
        playfield.setPrefSize(GAME_SURFACE_WIDTH, GAME_SURFACE_HEIGHT);
        playfield.setMaxSize(GAME_SURFACE_WIDTH, GAME_SURFACE_HEIGHT);

        gameSurface = new StackPane(playfield);
        gameSurface.setMinSize(GAME_SURFACE_WIDTH, GAME_SURFACE_HEIGHT);
        gameSurface.setPrefSize(GAME_SURFACE_WIDTH, GAME_SURFACE_HEIGHT);
        gameSurface.setMaxSize(GAME_SURFACE_WIDTH, GAME_SURFACE_HEIGHT);

        root = new StackPane(gameSurface, goalButton, adviceToastLayer);
        root.getStyleClass().add("game-screen");
        root.setPadding(new Insets(0));
        root.setFocusTraversable(true);
        StackPane.setAlignment(goalButton, Pos.BOTTOM_LEFT);
        StackPane.setMargin(goalButton, new Insets(0, 0, GOAL_MARGIN_BOTTOM, GOAL_MARGIN_LEFT));
        StackPane.setAlignment(adviceToastLayer, Pos.TOP_RIGHT);
        StackPane.setMargin(adviceToastLayer, new Insets(22, 22, 0, 0));

        bindGameSurfaceScale();
        setupKeyboardControls(root);
        startMovementLoop();
        showStartingOverlays();
        return root;
    }

    private void bindGameSurfaceScale() {
        gameSurface.scaleXProperty().bind(Bindings.createDoubleBinding(
                this::gameSurfaceScale,
                root.widthProperty(),
                root.heightProperty()
        ));
        gameSurface.scaleYProperty().bind(gameSurface.scaleXProperty());
    }

    private double gameSurfaceScale() {
        double availableWidth = Math.max(GAME_SURFACE_WIDTH, root.getWidth() - FULLSCREEN_MARGIN * 2);
        double availableHeight = Math.max(GAME_SURFACE_HEIGHT, root.getHeight() - FULLSCREEN_MARGIN * 2);
        return Math.min(availableWidth / GAME_SURFACE_WIDTH, availableHeight / GAME_SURFACE_HEIGHT);
    }

    private void renderRoom() {
        gamePane.getChildren().clear();
        gamePane.setPrefSize(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT);
        gamePane.setMinSize(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT);
        gamePane.setMaxSize(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT);
        if (!gamePane.getStyleClass().contains("game-room")) {
            gamePane.getStyleClass().add("game-room");
        }

        addRoomFloor();
        addWalls();
        addDoor();
        addChallengeDoor();
        addTerminal();
        addChests();
        addProgrammableObjects();
        addTokens();
        addPlayer();
        addHardModeDarkness();
        addInventoryButton();
        addPickupMessage();
        refreshBugHud();
        refreshInventoryButton();
    }

    private void setupKeyboardControls(Parent root) {
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && activeModal != null) {
                closeModal();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.ESCAPE && inventoryPanel != null) {
                hideInventoryPanel();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.ESCAPE) {
                showPauseMenu();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.ENTER && activeModal == null) {
                openCodeBuilder();
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

        root.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (activeModal != null) {
                return;
            }
            if (isMovementKey(event.getCode())) {
                pressedKeys.remove(event.getCode());
                event.consume();
            }
        });

        root.setOnMouseClicked(event -> {
            if (activeModal == null) {
                root.requestFocus();
            }
        });
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
        if (isGameplayPaused() || pressedKeys.isEmpty()) {
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
        if (maybeOpenProgrammableObjectTerminal()) {
            renderRoom();
            return;
        }
        checkCollisions();
        renderRoom();
        maybeAdvanceLevel();
        maybeOpenTerminal();
    }

    private boolean isGameplayPaused() {
        return activeModal != null;
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
            refreshInventoryPanel();
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
        configureWrappingText(prompt, CHALLENGE_CONTENT_WIDTH);

        Label code = new Label(question.getCode());
        code.getStyleClass().add("answer-preview");
        configureWrappingText(code, CHALLENGE_CONTENT_WIDTH);
        code.setMinHeight(Region.USE_PREF_SIZE);

        VBox choices = new VBox(10);
        for (String choice : question.getChoices()) {
            Button choiceButton = new Button(choice);
            choiceButton.getStyleClass().add("pixel-button");
            choiceButton.setWrapText(true);
            choiceButton.setTextOverrun(OverrunStyle.CLIP);
            choiceButton.setMinHeight(Region.USE_PREF_SIZE);
            choiceButton.setMaxWidth(Double.MAX_VALUE);
            choiceButton.setOnAction(event -> answerChallengeQuestion(choice));
            choices.getChildren().add(choiceButton);
        }

        VBox content = new VBox(14, title, prompt, code, choices);
        content.setPrefWidth(CHALLENGE_CONTENT_WIDTH);
        content.setMaxWidth(CHALLENGE_CONTENT_WIDTH);
        showModal(
                content,
                CHALLENGE_MODAL_WIDTH,
                challengeModalHeight(question.getPrompt(), question.getCode(), question.getChoices().size()),
                ModalType.CHALLENGE
        );
    }

    private void configureWrappingText(Label label, double width) {
        label.setWrapText(true);
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setPrefWidth(width);
        label.setMaxWidth(width);
        label.setMinHeight(Region.USE_PREF_SIZE);
    }

    private double challengeModalHeight(String prompt, String code, int choiceCount) {
        int promptLines = estimatedWrappedLines(prompt, CHALLENGE_PROMPT_CHARS_PER_LINE);
        int codeLines = estimatedWrappedLines(code, CHALLENGE_PROMPT_CHARS_PER_LINE);
        double estimatedHeight = 350
                + promptLines * 28
                + Math.max(0, codeLines - 1) * 22
                + Math.max(0, choiceCount - 4) * 52;
        return Math.max(CHALLENGE_MIN_MODAL_HEIGHT, Math.min(CHALLENGE_MAX_MODAL_HEIGHT, estimatedHeight));
    }

    private int estimatedWrappedLines(String text, int charactersPerLine) {
        if (text == null || text.isBlank()) {
            return 1;
        }

        return Math.max(1, (int) Math.ceil((double) text.length() / charactersPerLine));
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
            refreshInventoryPanel();
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
        wasTouchingTerminal = gameState.getPlayer().intersects(terminalX(), terminalY(), TERMINAL_WIDTH, TERMINAL_HEIGHT);
    }

    private boolean maybeOpenProgrammableObjectTerminal() {
        ProgrammableObject touchedObject = collisionManager.findTouchedProgrammableObject(
                gameState.getPlayer(),
                gameState.getCurrentLevel().getRoom()
        );
        boolean touchingObject = touchedObject != null;
        if (touchingObject && !wasTouchingProgrammableObject) {
            openProgrammableObjectTerminal(touchedObject);
            wasTouchingProgrammableObject = true;
            return true;
        }

        wasTouchingProgrammableObject = touchingObject;
        return false;
    }

    private void openProgrammableObjectTerminal(ProgrammableObject programmableObject) {
        if (activeModal != null || programmableObject == null || programmableObject.isActivated()) {
            return;
        }

        activeProgrammableObject = programmableObject;
        activeCodeBuilderView = new CodeBuilderView(
                gameState.getInventory(),
                programmableObject.getPuzzle(),
                true,
                result -> {
                    programmableObject.activate();
                    closeModal();
                    setPickupMessage(programmableObject.getDisplayName() + " unlocked!", "success-message");
                    renderRoom();
                },
                result -> recordMistake()
        );

        Parent builder = activeCodeBuilderView.createView();
        showModal(builder, 920, 620, ModalType.OBJECT_TERMINAL);
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

    private void showStartingOverlays() {
        if (gameState.hasSeenTutorial()) {
            showLevelAdvice();
            return;
        }

        maybeShowTutorial();
    }

    private void maybeShowTutorial() {
        if (gameState.hasSeenTutorial()) {
            return;
        }

        Platform.runLater(this::showTutorialWindow);
    }

    private void showTutorialWindow() {
        if (activeModal != null || gameState.hasSeenTutorial()) {
            return;
        }

        Label title = new Label("Tutorial");
        title.getStyleClass().add("modal-title");

        Label movement = tutorialLine("Move with W, A, S, D or the arrow keys.");
        Label tokens = tutorialLine("Walk into tokens and chests to collect code pieces.");
        Label terminal = tutorialLine("Open the terminal by pressing Enter or clicking the green terminal.");
        Label goal = tutorialLine("Find the Goal, open it from the bottom-left button, then solve the terminal puzzle to unlock the door.");
        Label inventory = tutorialLine("Use the bottom inventory button to inspect collected tokens.");

        Button startButton = new Button("Start");
        startButton.getStyleClass().add("pixel-button");
        startButton.setOnAction(event -> closeModal());

        VBox content = new VBox(12, title, movement, tokens, terminal, goal, inventory, startButton);
        content.setMaxWidth(660);
        showModal(content, 760, 520, ModalType.TUTORIAL);
    }

    private Label tutorialLine(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("modal-copy");
        label.setWrapText(true);
        label.setPrefWidth(640);
        label.setMaxWidth(640);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    private void showPauseMenu() {
        if (activeModal != null) {
            return;
        }

        Label title = new Label("Paused");
        title.getStyleClass().add("modal-title");

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().add("pixel-button");
        resumeButton.setMaxWidth(Double.MAX_VALUE);
        resumeButton.setOnAction(event -> closeModal());

        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.getStyleClass().add("pixel-button");
        mainMenuButton.setMaxWidth(Double.MAX_VALUE);
        mainMenuButton.setOnAction(event -> returnToMainMenuFromPause());

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("pixel-button");
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setOnAction(event -> exitFromPause());

        VBox content = new VBox(14, title, resumeButton, mainMenuButton, exitButton);
        content.setMaxWidth(360);
        showModal(content, 420, 300, ModalType.PAUSE, false);
    }

    private void returnToMainMenuFromPause() {
        stopMovementLoop();
        closeModal();
        app.showMainMenu();
    }

    private void exitFromPause() {
        stopMovementLoop();
        closeModal();
        app.showExitThankYouAndClose();
    }

    private void showLevelAdvice() {
        String advice = levelAdvice();
        if (!advice.isBlank()) {
            showAdviceToast("Achievement Get!", advice);
        }
        if (gameState.getGameMode().isHard()) {
            showAdviceToast("Hard Mode", "Only nearby code is visible.");
        }
    }

    private String levelAdvice() {
        return switch (gameState.getCurrentLevel().getDisplayId()) {
            case "1-1" -> "New: collect code tokens and open the terminal.";
            case "1-2" -> "New: print statements send text to the output.";
            case "2-2" -> "New: maze chests hide the pieces you need.";
            case "2-3" -> "New: question doors unlock extra room rewards.";
            case "3-3" -> "New: methods let you bundle reusable code.";
            case "4-1" -> "New: loops repeat while their condition stays true.";
            case "5-1" -> "New: classes group fields into a blueprint.";
            case "5-2" -> "New: constructors prepare objects and methods make them act.";
            case "5-3" -> "New: object terminals let code change map objects.";
            default -> "";
        };
    }

    private void showAdviceToast(String titleText, String messageText) {
        if (adviceToastLayer == null || messageText == null || messageText.isBlank()) {
            return;
        }

        Label title = new Label(titleText);
        title.getStyleClass().add("achievement-toast-title");

        Label message = new Label(messageText);
        message.setWrapText(true);
        message.setMaxWidth(360);
        message.getStyleClass().add("achievement-toast-copy");

        VBox toast = new VBox(4, title, message);
        toast.getStyleClass().add("achievement-toast");
        adviceToastLayer.getChildren().add(toast);

        PauseTransition hideDelay = new PauseTransition(Duration.seconds(ADVICE_TOAST_SECONDS));
        hideDelay.setOnFinished(event -> adviceToastLayer.getChildren().remove(toast));
        hideDelay.play();
    }

    private void showModal(Parent content, double width, double height, ModalType modalType) {
        showModal(content, width, height, modalType, true);
    }

    private void showModal(Parent content, double width, double height, ModalType modalType, boolean showCloseButton) {
        hideInventoryPanel();
        pressedKeys.clear();
        VBox modal = new VBox(12);
        if (showCloseButton) {
            Button closeButton = new Button("X");
            closeButton.getStyleClass().add("close-button");
            closeButton.setOnAction(event -> closeModal());

            HBox closeRow = new HBox(closeButton);
            closeRow.setAlignment(Pos.TOP_LEFT);
            modal.getChildren().add(closeRow);
        }
        modal.getChildren().add(content);
        modal.setPadding(new Insets(16));
        modal.setMaxSize(width, height);
        modal.setPrefSize(width, height);
        modal.getStyleClass().add("map-modal");
        modal.setFocusTraversable(true);

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
            if (closingModalType == ModalType.TERMINAL || closingModalType == ModalType.OBJECT_TERMINAL) {
                activeCodeBuilderView = null;
            }
            if (closingModalType == ModalType.OBJECT_TERMINAL) {
                activeProgrammableObject = null;
            }
            if (closingModalType == ModalType.TUTORIAL) {
                gameState.markTutorialSeen();
                showLevelAdvice();
            }
        }
        Platform.runLater(root::requestFocus);
    }

    private void addRoomFloor() {
        Rectangle floor = new Rectangle(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT);
        floor.getStyleClass().add("room-floor");

        Rectangle innerFloor = createInnerFloor();
        innerFloor.getStyleClass().add("room-inner-floor");

        gamePane.getChildren().addAll(floor, innerFloor);
    }

    private Rectangle createInnerFloor() {
        Room room = gameState.getCurrentLevel().getRoom();
        if ("2-2".equals(gameState.getCurrentLevel().getDisplayId()) && !room.getWalls().isEmpty()) {
            double minX = room.getWalls().stream().mapToDouble(Wall::getX).min().orElse(32);
            double minY = room.getWalls().stream().mapToDouble(Wall::getY).min().orElse(36);
            double maxX = room.getWalls().stream().mapToDouble(wall -> wall.getX() + wall.getWidth()).max().orElse(Constants.ROOM_WIDTH - 32);
            double maxY = room.getWalls().stream().mapToDouble(wall -> wall.getY() + wall.getHeight()).max().orElse(Constants.ROOM_HEIGHT - 38);
            Rectangle innerFloor = new Rectangle(maxX - minX, maxY - minY);
            innerFloor.setLayoutX(minX);
            innerFloor.setLayoutY(minY);
            return innerFloor;
        }

        Rectangle innerFloor = new Rectangle(Constants.ROOM_WIDTH - 64, Constants.ROOM_HEIGHT - 74);
        innerFloor.setLayoutX(32);
        innerFloor.setLayoutY(36);
        return innerFloor;
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
            if (chest.isLocked()) {
                chestTile.getStyleClass().add("room-chest-locked");
            }
            if (chest.isOpened()) {
                chestTile.getStyleClass().add("room-chest-open");
            }

            Label mark = new Label(chest.isOpened() ? "" : chest.isLocked() ? "LOCK" : "?");
            mark.getStyleClass().add("room-chest-text");
            chestTile.getChildren().add(mark);

            gamePane.getChildren().add(chestTile);
        }
    }

    private void addProgrammableObjects() {
        for (ProgrammableObject programmableObject : gameState.getCurrentLevel().getRoom().getProgrammableObjects()) {
            StackPane objectTile = new StackPane();
            objectTile.setLayoutX(programmableObject.getX());
            objectTile.setLayoutY(programmableObject.getY());
            objectTile.setPrefSize(programmableObject.getWidth(), programmableObject.getHeight());
            objectTile.getStyleClass().add("programmable-object");
            if (programmableObject.isActivated()) {
                objectTile.getStyleClass().add("programmable-object-active");
            }

            Label label = new Label(programmableObject.getDisplayName());
            label.getStyleClass().add("programmable-object-text");
            objectTile.getChildren().add(label);

            gamePane.getChildren().add(objectTile);
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
        bugs.setPrefWidth(180);
        bugs.setMaxWidth(180);
        bugs.setMinHeight(BUG_HUD_HEIGHT);
        bugs.setPrefHeight(BUG_HUD_HEIGHT);
        bugs.setMaxHeight(BUG_HUD_HEIGHT);
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
            bugSlot.setMaxSize(52, 46);
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

    private void addHardModeDarkness() {
        if (!gameState.getGameMode().isHard()) {
            return;
        }

        Player player = gameState.getPlayer();
        Rectangle darkness = new Rectangle(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT);
        darkness.setFill(Color.BLACK);

        Circle light = new Circle(
                player.getX() + player.getWidth() / 2.0,
                player.getY() + player.getHeight() / 2.0,
                HARD_MODE_LIGHT_RADIUS
        );
        Shape overlay = Shape.subtract(darkness, light);
        overlay.setMouseTransparent(true);
        overlay.getStyleClass().add("hard-mode-darkness");
        gamePane.getChildren().add(overlay);
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
        terminal.setOnMouseClicked(event -> {
            openCodeBuilder();
            event.consume();
        });

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

    private VBox createAdviceToastLayer() {
        VBox layer = new VBox(8);
        layer.setAlignment(Pos.TOP_RIGHT);
        layer.setMouseTransparent(true);
        layer.getStyleClass().add("achievement-toast-layer");
        return layer;
    }

    private void addInventoryButton() {
        double x = Constants.ROOM_WIDTH / 2.0 - INVENTORY_WIDTH / 2.0;
        double y = Constants.ROOM_HEIGHT - INVENTORY_HEIGHT - INVENTORY_MARGIN_BOTTOM;

        Rectangle shadow = new Rectangle(INVENTORY_WIDTH, 6);
        shadow.setLayoutX(x);
        shadow.setLayoutY(y + INVENTORY_HEIGHT);
        shadow.setFill(Color.web("#5d3828"));
        shadow.getStyleClass().add("inventory-toggle-shadow");
        shadow.setMouseTransparent(true);

        Rectangle background = new Rectangle(INVENTORY_WIDTH, INVENTORY_HEIGHT);
        background.setLayoutX(x);
        background.setLayoutY(y);
        background.setFill(Color.web("#8a5a35"));
        background.getStyleClass().add("inventory-toggle-shape");
        background.setOnMouseClicked(event -> {
            toggleInventoryPanel();
            event.consume();
        });

        inventoryButtonLabel = new Label();
        inventoryButtonLabel.setLayoutX(x);
        inventoryButtonLabel.setLayoutY(y);
        inventoryButtonLabel.setPrefSize(INVENTORY_WIDTH, INVENTORY_HEIGHT);
        inventoryButtonLabel.setAlignment(Pos.CENTER);
        inventoryButtonLabel.getStyleClass().add("inventory-toggle-text");
        inventoryButtonLabel.setOnMouseClicked(event -> {
            toggleInventoryPanel();
            event.consume();
        });
        refreshInventoryButton();
        gamePane.getChildren().addAll(shadow, background, inventoryButtonLabel);
    }

    private void toggleInventoryPanel() {
        if (inventoryPanel != null) {
            hideInventoryPanel();
            return;
        }

        showInventoryPanel();
    }

    private void showInventoryPanel() {
        if (root == null || inventoryPanel != null) {
            return;
        }

        Label title = new Label("Inventory");
        title.getStyleClass().add("inventory-title");

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(event -> hideInventoryPanel());

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        HBox titleRow = new HBox(12, title, spacer, closeButton);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        FlowPane tokenBox = new FlowPane(8, 8);
        tokenBox.setPrefWrapLength(430);
        tokenBox.getStyleClass().add("inventory-token-box");

        inventoryDescriptionLabel = new Label("Click a token to see what it does.");
        inventoryDescriptionLabel.setWrapText(true);
        inventoryDescriptionLabel.setMaxWidth(430);
        inventoryDescriptionLabel.getStyleClass().add("inventory-description");

        if (gameState.getInventory().getTokens().isEmpty()) {
            Label emptyLabel = new Label("No tokens yet.");
            emptyLabel.getStyleClass().add("inventory-description");
            tokenBox.getChildren().add(emptyLabel);
        } else {
            for (Token token : gameState.getInventory().getTokens()) {
                Button tokenButton = new Button(token.getValue());
                tokenButton.getStyleClass().add("inventory-token-button");
                tokenButton.setOnAction(event -> inventoryDescriptionLabel.setText(TokenDescriptions.describe(token.getValue())));
                tokenBox.getChildren().add(tokenButton);
            }
        }

        VBox panel = new VBox(10, titleRow, tokenBox, inventoryDescriptionLabel);
        panel.setPrefWidth(480);
        panel.setMaxWidth(520);
        panel.setMaxHeight(Region.USE_PREF_SIZE);
        panel.getStyleClass().add("inventory-popover");
        inventoryPanel = panel;
        root.getChildren().add(inventoryPanel);
        StackPane.setAlignment(inventoryPanel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(inventoryPanel, new Insets(0, 0, BOTTOM_CONTROL_MARGIN + 78, 0));
    }

    private void hideInventoryPanel() {
        if (root != null && inventoryPanel != null) {
            root.getChildren().remove(inventoryPanel);
        }
        inventoryPanel = null;
        inventoryDescriptionLabel = null;
    }

    private void refreshInventoryPanel() {
        if (inventoryPanel != null) {
            hideInventoryPanel();
            showInventoryPanel();
        }
        refreshInventoryButton();
    }

    private void refreshInventoryButton() {
        if (inventoryButtonLabel != null) {
            inventoryButtonLabel.setText("Inventory (" + gameState.getInventory().getTokens().size() + ")");
        }
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
        restartDelay.setOnFinished(event -> app.restartAfterBugFailure());
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
        return Constants.ROOM_WIDTH - TERMINAL_WIDTH - TERMINAL_MARGIN_RIGHT;
    }

    private double terminalY() {
        return Constants.ROOM_HEIGHT - TERMINAL_HEIGHT - TERMINAL_MARGIN_BOTTOM;
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
