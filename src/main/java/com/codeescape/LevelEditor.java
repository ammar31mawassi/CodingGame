package com.codeescape;

import com.codeescape.engine.GridRoomBuilder;
import com.codeescape.engine.LevelLayoutOverride;
import com.codeescape.engine.LevelLayoutOverrideStore;
import com.codeescape.engine.LevelManager;
import com.codeescape.engine.RoomLayoutBuilder;
import com.codeescape.model.Chest;
import com.codeescape.model.ChestReward;
import com.codeescape.model.Door;
import com.codeescape.model.Level;
import com.codeescape.model.MultipleChoiceQuestion;
import com.codeescape.model.Room;
import com.codeescape.model.Token;
import com.codeescape.model.TokenType;
import com.codeescape.model.Wall;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LevelEditor extends Application {
    private static final int COLUMNS = RoomLayoutBuilder.GRID_COLUMNS;
    private static final int ROWS = RoomLayoutBuilder.GRID_ROWS;
    private static final double DISPLAY_CELL_WIDTH = 64;
    private static final double DISPLAY_CELL_HEIGHT = 62;
    private static final double DISPLAY_WALL_STROKE = 8;
    private static final double CHEST_WIDTH = 38;
    private static final double CHEST_HEIGHT = 28;

    private final LevelLayoutOverrideStore overrideStore = new LevelLayoutOverrideStore();
    private final int[][] cells = new int[ROWS][COLUMNS];
    private final List<ChestSpec> chests = new ArrayList<>();
    private final List<QuestionDoorSpec> questionDoors = new ArrayList<>();
    private final List<TokenSpec> tokens = new ArrayList<>();
    private final Pane gridPane = new Pane();
    private final TextArea exportArea = new TextArea();
    private final Label statusLabel = new Label("Choose a level, then edit its grid.");
    private final ComboBox<LevelChoice> levelPicker = new ComboBox<>();
    private final ComboBox<GridRoomBuilder.Side> sidePicker = new ComboBox<>();
    private final CheckBox lockedChestBox = new CheckBox("Chest is behind a question door");
    private final TextField promptField = new TextField("Which keyword declares a class?");
    private final TextField codeField = new TextField("_____ Item { }");
    private final TextField choicesField = new TextField("class,int,void,if");
    private final TextField answerField = new TextField("class");
    private final TextField rewardField = new TextField("class");
    private final TextField goalTitleField = new TextField();
    private final TextArea goalInstructionsArea = new TextArea();
    private final TextArea helperArea = new TextArea();
    private final TextArea acceptedAnswersArea = new TextArea();
    private final ComboBox<TokenChoice> tokenPicker = new ComboBox<>();
    private final TextField tokenValueField = new TextField();
    private final ComboBox<TokenType> tokenTypePicker = new ComboBox<>();
    private final ComboBox<TokenSource> tokenSourcePicker = new ComboBox<>();
    private final TextField tokenColumnField = new TextField("0");
    private final TextField tokenRowField = new TextField("0");
    private final TextField tokenChestOrderField = new TextField("0");

    private EditorMode mode = EditorMode.WALLS;
    private int activeLevelNumber = 1;
    private int selectedChestIndex = -1;
    private int selectedTokenIndex = -1;
    private boolean syncingChestControls;
    private boolean syncingTokenControls;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        populateLevelPicker();
        sidePicker.getItems().setAll(GridRoomBuilder.Side.values());
        sidePicker.setValue(GridRoomBuilder.Side.RIGHT);
        tokenTypePicker.getItems().setAll(TokenType.values());
        tokenTypePicker.setValue(TokenType.CODE);
        tokenSourcePicker.getItems().setAll(TokenSource.values());
        tokenSourcePicker.setValue(TokenSource.VISIBLE);
        tokenValueField.setPromptText("Token value");
        tokenColumnField.setPromptText("Column");
        tokenRowField.setPromptText("Row");
        tokenChestOrderField.setPromptText("Chest order");
        acceptedAnswersArea.setPromptText("One accepted final answer per line. Leave blank to use the built-in validator.");
        tokenPicker.valueProperty().addListener((observable, oldValue, selected) -> {
            if (!syncingTokenControls && selected != null) {
                selectToken(selected.index(), "Token selected.");
            }
        });
        lockedChestBox.selectedProperty().addListener((observable, oldValue, selected) -> updateSelectedChestLock(selected));

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #101419;");
        root.setLeft(createToolbar());
        root.setCenter(createGridSurface());
        root.setRight(createExportPanel());
        root.setBottom(statusLabel);
        BorderPane.setMargin(statusLabel, new Insets(8, 16, 14, 16));
        statusLabel.setStyle("""
                -fx-font-family: 'Consolas', 'Courier New', monospace;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #d9d9d7;
                """);

        loadSelectedLevel();

        Scene scene = new Scene(root, 1500, 800);
        stage.setTitle("Code Escape Level Editor");
        stage.setScene(scene);
        stage.show();
    }

    private ScrollPane createToolbar() {
        Label title = new Label("Level Editor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f1ce54;");

        Button loadButton = new Button("Load Level");
        loadButton.setMaxWidth(Double.MAX_VALUE);
        loadButton.setOnAction(event -> loadSelectedLevel());

        Button saveButton = new Button("Save To Game");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(event -> saveSelectedLevel());

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton wallsButton = modeButton("Walls", EditorMode.WALLS, modeGroup);
        RadioButton chestsButton = modeButton("Chests", EditorMode.CHESTS, modeGroup);
        RadioButton tokensButton = modeButton("Tokens", EditorMode.TOKENS, modeGroup);
        RadioButton doorsButton = modeButton("Question Doors", EditorMode.QUESTION_DOORS, modeGroup);
        wallsButton.setSelected(true);
        modeGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle != null) {
                mode = (EditorMode) newToggle.getUserData();
                statusLabel.setText(statusForMode());
            }
        });

        Button validateButton = new Button("Validate Maze");
        validateButton.setMaxWidth(Double.MAX_VALUE);
        validateButton.setOnAction(event -> validateCurrentLayout());

        Button clearButton = new Button("Clear Layout");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setOnAction(event -> clearLayout());

        Button addTokenButton = new Button("Add Token");
        addTokenButton.setMaxWidth(Double.MAX_VALUE);
        addTokenButton.setOnAction(event -> addTokenFromFields());
        Button updateTokenButton = new Button("Update Token");
        updateTokenButton.setMaxWidth(Double.MAX_VALUE);
        updateTokenButton.setOnAction(event -> updateTokenFromFields());
        Button deleteTokenButton = new Button("Delete Token");
        deleteTokenButton.setMaxWidth(Double.MAX_VALUE);
        deleteTokenButton.setOnAction(event -> deleteSelectedToken());

        goalInstructionsArea.setPrefRowCount(3);
        goalInstructionsArea.setWrapText(true);
        helperArea.setPrefRowCount(3);
        helperArea.setWrapText(true);
        acceptedAnswersArea.setPrefRowCount(4);
        acceptedAnswersArea.setWrapText(false);

        VBox toolbar = new VBox(
                9,
                title,
                new Label("Game level"),
                levelPicker,
                new HBox(8, loadButton, saveButton),
                new Separator(),
                new Label("Mode"),
                wallsButton,
                chestsButton,
                tokensButton,
                doorsButton,
                new Separator(),
                new Label("Wall / door side"),
                sidePicker,
                lockedChestBox,
                new Separator(),
                new Label("Level goal title"),
                goalTitleField,
                new Label("Goal text"),
                goalInstructionsArea,
                new Label("Helper text"),
                helperArea,
                new Label("Accepted final answers"),
                acceptedAnswersArea,
                new Separator(),
                new Label("Tokens"),
                tokenPicker,
                tokenValueField,
                tokenTypePicker,
                tokenSourcePicker,
                new HBox(8, tokenColumnField, tokenRowField, tokenChestOrderField),
                new HBox(8, addTokenButton, updateTokenButton),
                deleteTokenButton,
                new Separator(),
                new Label("Question prompt"),
                promptField,
                new Label("Code text"),
                codeField,
                new Label("Choices"),
                choicesField,
                new Label("Correct answer"),
                answerField,
                new Label("Reward token"),
                rewardField,
                new Separator(),
                validateButton,
                clearButton
        );
        toolbar.setPadding(new Insets(14));
        toolbar.setPrefWidth(280);
        toolbar.setStyle("""
                -fx-background-color: #101419;
                -fx-font-family: 'Consolas', 'Courier New', monospace;
                -fx-text-fill: #f1f1ec;
                """);
        toolbar.getChildren().stream()
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .forEach(label -> label.setStyle(label.getStyle() + "-fx-text-fill: #d9d9d7; -fx-font-weight: bold;"));
        ScrollPane scrollPane = new ScrollPane(toolbar);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(310);
        scrollPane.setStyle("-fx-background: #101419; -fx-background-color: #101419;");
        return scrollPane;
    }

    private RadioButton modeButton(String label, EditorMode editorMode, ToggleGroup modeGroup) {
        RadioButton button = new RadioButton(label);
        button.setToggleGroup(modeGroup);
        button.setUserData(editorMode);
        button.setStyle("-fx-text-fill: #f1f1ec; -fx-font-weight: bold;");
        return button;
    }

    private ScrollPane createGridSurface() {
        gridPane.setPrefSize(COLUMNS * DISPLAY_CELL_WIDTH, ROWS * DISPLAY_CELL_HEIGHT);
        gridPane.setMinSize(COLUMNS * DISPLAY_CELL_WIDTH, ROWS * DISPLAY_CELL_HEIGHT);
        gridPane.setMaxSize(COLUMNS * DISPLAY_CELL_WIDTH, ROWS * DISPLAY_CELL_HEIGHT);
        gridPane.setStyle("-fx-background-color: #252e36;");

        StackPane gridFrame = new StackPane(gridPane);
        gridFrame.setPadding(new Insets(16));
        gridFrame.setAlignment(Pos.CENTER);
        gridFrame.setStyle("-fx-background-color: #1a222b;");

        ScrollPane scrollPane = new ScrollPane(gridFrame);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #151b22; -fx-background-color: #151b22;");
        return scrollPane;
    }

    private VBox createExportPanel() {
        Label title = new Label("Export");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f1ce54;");

        exportArea.setEditable(false);
        exportArea.setWrapText(false);
        exportArea.setStyle("""
                -fx-control-inner-background: #151b22;
                -fx-text-fill: #f1f1ec;
                -fx-font-family: 'Consolas', 'Courier New', monospace;
                -fx-font-size: 12px;
                """);
        VBox.setVgrow(exportArea, Priority.ALWAYS);

        Button copyButton = new Button("Copy Export");
        copyButton.setMaxWidth(Double.MAX_VALUE);
        copyButton.setOnAction(event -> copyExport());

        Button refreshButton = new Button("Refresh Export");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(event -> refreshExport());

        HBox buttons = new HBox(10, refreshButton, copyButton);
        buttons.setAlignment(Pos.CENTER);
        HBox.setHgrow(refreshButton, Priority.ALWAYS);
        HBox.setHgrow(copyButton, Priority.ALWAYS);

        VBox panel = new VBox(12, title, exportArea, buttons);
        panel.setPadding(new Insets(14));
        panel.setPrefWidth(370);
        panel.setStyle("-fx-background-color: #101419; -fx-font-family: 'Consolas', 'Courier New', monospace;");
        return panel;
    }

    private void populateLevelPicker() {
        LevelManager manager = new LevelManager(LevelLayoutOverrideStore.disabled());
        manager.loadLevels();
        levelPicker.getItems().clear();
        for (Level level : manager.getLevels()) {
            levelPicker.getItems().add(new LevelChoice(level.getLevelNumber(), level.getDisplayId(), level.getName()));
        }
        if (!levelPicker.getItems().isEmpty()) {
            levelPicker.getSelectionModel().selectFirst();
        }
        levelPicker.setMaxWidth(Double.MAX_VALUE);
    }

    private void loadSelectedLevel() {
        LevelChoice selected = levelPicker.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        activeLevelNumber = selected.levelNumber();
        LevelManager manager = new LevelManager(LevelLayoutOverrideStore.disabled());
        manager.loadLevels();
        applyLevel(manager.getLevel(activeLevelNumber), "Loaded game default for " + selected + ".");
        overrideStore.load(activeLevelNumber)
                .ifPresent(override -> applyOverride(override, "Loaded saved edit for " + selected + "."));
    }

    private void applyOverride(LevelLayoutOverride override, String status) {
        clearLayoutStateOnly();
        int[][] savedCells = override.copyCells();
        for (int row = 0; row < Math.min(ROWS, override.rows()); row++) {
            for (int column = 0; column < Math.min(COLUMNS, override.columns()); column++) {
                cells[row][column] = savedCells[row][column];
            }
        }
        for (LevelLayoutOverride.ChestPlacement chest : override.chests()) {
            chests.add(new ChestSpec(chest.column(), chest.row(), chest.lockedRoomOnly()));
        }
        for (LevelLayoutOverride.QuestionDoorPlacement door : override.questionDoors()) {
            questionDoors.add(new QuestionDoorSpec(
                    door.column(),
                    door.row(),
                    door.side(),
                    door.prompt(),
                    door.code(),
                    String.join(",", door.choices()),
                    door.correctAnswer(),
                door.reward()
            ));
        }
        if (override.goalTitle() != null) {
            goalTitleField.setText(override.goalTitle());
        }
        if (override.goalInstructions() != null) {
            goalInstructionsArea.setText(override.goalInstructions());
        }
        if (override.helperText() != null) {
            helperArea.setText(override.helperText());
        }
        acceptedAnswersArea.setText(String.join("\n", override.acceptedAnswers()));
        if (override.tokensOverridden()) {
            tokens.clear();
            for (LevelLayoutOverride.TokenPlacement token : override.tokens()) {
                tokens.add(new TokenSpec(
                        token.value(),
                        token.type(),
                        token.kind() == LevelLayoutOverride.TokenPlacementKind.CHEST ? TokenSource.CHEST : TokenSource.VISIBLE,
                        token.column(),
                        token.row(),
                        token.chestOrder()
                ));
            }
        }
        updateQuestionFieldsFromFirstDoor();
        refreshTokenPicker();
        renderAndExport(status);
    }

    private void applyLevel(Level level, String status) {
        Room room = level.getRoom();
        clearStateOnly();
        goalTitleField.setText(room.getPuzzle().getTitle());
        goalInstructionsArea.setText(room.getPuzzle().getInstructions());
        helperArea.setText(level.getGoalHelper());
        acceptedAnswersArea.clear();
        for (Wall wall : room.getWalls()) {
            inferEdge(wall.getX(), wall.getY(), wall.getWidth(), wall.getHeight())
                    .ifPresent(edge -> cells[edge.row()][edge.column()] |= wallFlag(edge.side()));
        }

        if (room.getChallengeDoor() != null) {
            inferEdge(
                    room.getChallengeDoor().getX(),
                    room.getChallengeDoor().getY(),
                    room.getChallengeDoor().getWidth(),
                    room.getChallengeDoor().getHeight()
            ).ifPresent(edge -> {
                cells[edge.row()][edge.column()] &= ~wallFlag(edge.side());
                cells[edge.row()][edge.column()] |= doorFlag(edge.side());
                questionDoors.add(questionDoorFromRoom(edge, room.getChallengeQuestion()));
            });
        }

        for (Chest chest : room.getChests()) {
            Cell cell = cellFromRuntimePoint(
                    chest.getX() + chest.getWidth() / 2.0,
                    chest.getY() + chest.getHeight() / 2.0
            );
            if (isInside(cell)) {
                chests.add(new ChestSpec(cell.column(), cell.row(), chest.isLocked()));
            }
        }
        for (Token token : room.getTokens()) {
            Cell cell = cellFromRuntimePoint(
                    token.getX() + token.getWidth() / 2.0,
                    token.getY() + token.getHeight() / 2.0
            );
            if (isInside(cell)) {
                tokens.add(new TokenSpec(token.getValue(), token.getType(), TokenSource.VISIBLE, cell.column(), cell.row(), 0));
            }
        }
        for (int i = 0; i < room.getChestRewards().size(); i++) {
            ChestReward reward = room.getChestRewards().get(i);
            tokens.add(new TokenSpec(reward.getValue(), reward.getType(), TokenSource.CHEST, 0, 0, i));
        }
        if (room.getFinalChestReward() != null) {
            ChestReward reward = room.getFinalChestReward();
            tokens.add(new TokenSpec(reward.getValue(), reward.getType(), TokenSource.CHEST, 0, 0, room.getChestRewards().size()));
        }
        markChestsBehindDoors();
        updateQuestionFieldsFromFirstDoor();
        refreshTokenPicker();
        renderAndExport(status);
    }

    private QuestionDoorSpec questionDoorFromRoom(Edge edge, MultipleChoiceQuestion question) {
        if (question == null) {
            return new QuestionDoorSpec(
                    edge.column(),
                    edge.row(),
                    edge.side(),
                    promptField.getText(),
                    codeField.getText(),
                    choicesField.getText(),
                    answerField.getText(),
                    rewardField.getText()
            );
        }

        ChestReward reward = question.getReward();
        return new QuestionDoorSpec(
                edge.column(),
                edge.row(),
                edge.side(),
                question.getPrompt(),
                question.getCode(),
                String.join(",", question.getChoices()),
                question.getCorrectAnswer(),
                reward == null ? "" : reward.getValue()
        );
    }

    private void saveSelectedLevel() {
        syncSingleQuestionDoorFields();
        LevelLayoutOverride override = currentOverride();
        try {
            validateTokenPlacements();
            RoomLayoutBuilder.fromOverride(override);
            overrideStore.save(override);
            refreshExport();
            statusLabel.setText("Saved " + selectedLevelLabel() + " to " + overrideStore.fileForLevel(activeLevelNumber) + ".");
        } catch (RuntimeException exception) {
            statusLabel.setText("Save blocked: " + exception.getMessage());
        }
    }

    private void validateTokenPlacements() {
        boolean hasChestToken = tokens.stream().anyMatch(token -> token.source() == TokenSource.CHEST);
        if (hasChestToken && chests.isEmpty()) {
            throw new IllegalStateException("Chest tokens need at least one chest in the level.");
        }
    }

    private void renderGrid() {
        gridPane.getChildren().clear();
        renderCells();
        renderWallsAndDoors();
        renderTokens();
        renderChests();
    }

    private void renderCells() {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                Rectangle cell = new Rectangle(
                        column * DISPLAY_CELL_WIDTH,
                        row * DISPLAY_CELL_HEIGHT,
                        DISPLAY_CELL_WIDTH,
                        DISPLAY_CELL_HEIGHT
                );
                cell.setFill(Color.web("#252e36"));
                cell.setStroke(Color.web("#3d4753"));
                cell.setStrokeWidth(1);
                int currentColumn = column;
                int currentRow = row;
                cell.setOnMouseClicked(event -> {
                    handleCellClick(currentColumn, currentRow, event.getButton());
                    event.consume();
                });
                gridPane.getChildren().add(cell);
            }
        }
    }

    private void renderWallsAndDoors() {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                for (GridRoomBuilder.Side side : GridRoomBuilder.Side.values()) {
                    if (hasDoor(column, row, side)) {
                        renderEdge(column, row, side, Color.web("#d89144"), DISPLAY_WALL_STROKE + 2);
                        renderDoorMark(column, row, side);
                    } else if (hasWall(column, row, side)) {
                        renderEdge(column, row, side, Color.web("#5d6975"), DISPLAY_WALL_STROKE);
                    }
                }
            }
        }
    }

    private void renderEdge(int column, int row, GridRoomBuilder.Side side, Color color, double strokeWidth) {
        double x = column * DISPLAY_CELL_WIDTH;
        double y = row * DISPLAY_CELL_HEIGHT;
        Line line = switch (side) {
            case TOP -> new Line(x, y, x + DISPLAY_CELL_WIDTH, y);
            case RIGHT -> new Line(x + DISPLAY_CELL_WIDTH, y, x + DISPLAY_CELL_WIDTH, y + DISPLAY_CELL_HEIGHT);
            case BOTTOM -> new Line(x, y + DISPLAY_CELL_HEIGHT, x + DISPLAY_CELL_WIDTH, y + DISPLAY_CELL_HEIGHT);
            case LEFT -> new Line(x, y, x, y + DISPLAY_CELL_HEIGHT);
        };
        line.setStroke(color);
        line.setStrokeWidth(strokeWidth);
        line.setMouseTransparent(true);
        gridPane.getChildren().add(line);
    }

    private void renderDoorMark(int column, int row, GridRoomBuilder.Side side) {
        Text mark = new Text("?");
        mark.setFill(Color.web("#f6d65b"));
        mark.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Point2D point = edgeCenter(column, row, side);
        mark.setX(point.getX() - 6);
        mark.setY(point.getY() + 7);
        mark.setMouseTransparent(true);
        gridPane.getChildren().add(mark);
    }

    private void renderChests() {
        for (int i = 0; i < chests.size(); i++) {
            gridPane.getChildren().add(createChestNode(chests.get(i), i));
        }
    }

    private void renderTokens() {
        for (int i = 0; i < tokens.size(); i++) {
            TokenSpec token = tokens.get(i);
            if (token.source() != TokenSource.VISIBLE) {
                continue;
            }

            Label tokenLabel = new Label(token.value());
            tokenLabel.setStyle(tokenStyle(token.type(), i == selectedTokenIndex));
            tokenLabel.setLayoutX(token.column() * DISPLAY_CELL_WIDTH + 8);
            tokenLabel.setLayoutY(token.row() * DISPLAY_CELL_HEIGHT + DISPLAY_CELL_HEIGHT - 27);
            int tokenIndex = i;
            tokenLabel.setOnMouseClicked(event -> {
                selectToken(tokenIndex, "Token selected.");
                event.consume();
            });
            gridPane.getChildren().add(tokenLabel);
        }
    }

    private String tokenStyle(TokenType type, boolean selected) {
        String fill = switch (type) {
            case CODE -> "#f1ce54";
            case GOAL -> "#f1f1ec";
            case HELPER -> "#69d7ff";
        };
        String border = selected ? "#43d868" : "#c4932f";
        return "-fx-background-color: " + fill + ";"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 2;"
                + "-fx-padding: 2 5 2 5;"
                + "-fx-font-family: 'Consolas', 'Courier New', monospace;"
                + "-fx-font-size: 11px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #292a33;";
    }

    private StackPane createChestNode(ChestSpec chest, int index) {
        Rectangle body = new Rectangle(CHEST_WIDTH, CHEST_HEIGHT);
        body.setArcWidth(0);
        body.setArcHeight(0);
        body.setFill(chest.lockedRoomOnly() ? Color.web("#6e3a50") : Color.web("#9b6538"));
        body.setStroke(index == selectedChestIndex ? Color.web("#f1ce54") : Color.web("#5d3828"));
        body.setStrokeWidth(index == selectedChestIndex ? 4 : 3);

        Text text = new Text("?");
        text.setFill(Color.web("#f1ce54"));
        text.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        StackPane node = new StackPane(body, text);
        positionChestNode(node, chest.column(), chest.row());
        node.setCursor(Cursor.MOVE);
        node.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                chests.remove(index);
                selectedChestIndex = -1;
                renderAndExport("Removed chest.");
            } else {
                selectChest(index, "Chest selected. Drag it to move it; click empty cells to add more chests.");
            }
            event.consume();
        });
        node.setOnMouseDragged(event -> {
            selectedChestIndex = index;
            Point2D local = gridPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            node.setLayoutX(clamp(local.getX() - CHEST_WIDTH / 2.0, 0, COLUMNS * DISPLAY_CELL_WIDTH - CHEST_WIDTH));
            node.setLayoutY(clamp(local.getY() - CHEST_HEIGHT / 2.0, 0, ROWS * DISPLAY_CELL_HEIGHT - CHEST_HEIGHT));
            event.consume();
        });
        node.setOnMouseReleased(event -> {
            Point2D local = gridPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            int column = clampCell((int) (local.getX() / DISPLAY_CELL_WIDTH), COLUMNS);
            int row = clampCell((int) (local.getY() / DISPLAY_CELL_HEIGHT), ROWS);
            chests.set(index, new ChestSpec(column, row, chest.lockedRoomOnly()));
            renderAndExport("Moved chest to cell (" + column + ", " + row + ").");
            event.consume();
        });
        return node;
    }

    private void positionChestNode(StackPane node, int column, int row) {
        node.setLayoutX(column * DISPLAY_CELL_WIDTH + (DISPLAY_CELL_WIDTH - CHEST_WIDTH) / 2.0);
        node.setLayoutY(row * DISPLAY_CELL_HEIGHT + (DISPLAY_CELL_HEIGHT - CHEST_HEIGHT) / 2.0);
    }

    private void handleCellClick(int column, int row, MouseButton button) {
        if (button != MouseButton.PRIMARY) {
            return;
        }

        switch (mode) {
            case WALLS -> toggleWall(column, row, selectedSide());
            case CHESTS -> placeOrSelectChest(column, row);
            case TOKENS -> placeSelectedToken(column, row);
            case QUESTION_DOORS -> toggleQuestionDoor(column, row, selectedSide());
        }
    }

    private void toggleWall(int column, int row, GridRoomBuilder.Side side) {
        cells[row][column] &= ~doorFlag(side);
        removeQuestionDoor(column, row, side);
        cells[row][column] ^= wallFlag(side);
        renderAndExport("Toggled " + side.name().toLowerCase() + " wall at cell (" + column + ", " + row + ").");
    }

    private void placeOrSelectChest(int column, int row) {
        Optional<Integer> existingChest = chestAt(column, row);
        if (existingChest.isPresent()) {
            selectChest(existingChest.get(), "Chest selected. Drag it to move it.");
            return;
        }

        chests.add(new ChestSpec(column, row, lockedChestBox.isSelected()));
        selectChest(chests.size() - 1, "Added chest at cell (" + column + ", " + row + ").");
    }

    private void selectChest(int index, String status) {
        selectedChestIndex = index;
        syncingChestControls = true;
        lockedChestBox.setSelected(chests.get(index).lockedRoomOnly());
        syncingChestControls = false;
        renderAndExport(status);
    }

    private void updateSelectedChestLock(boolean selected) {
        if (syncingChestControls || selectedChestIndex < 0 || selectedChestIndex >= chests.size()) {
            return;
        }

        ChestSpec chest = chests.get(selectedChestIndex);
        chests.set(selectedChestIndex, new ChestSpec(chest.column(), chest.row(), selected));
        renderAndExport("Updated selected chest lock-room marker.");
    }

    private void placeSelectedToken(int column, int row) {
        tokenColumnField.setText(String.valueOf(column));
        tokenRowField.setText(String.valueOf(row));
        tokenSourcePicker.setValue(TokenSource.VISIBLE);
        if (selectedTokenIndex >= 0 && selectedTokenIndex < tokens.size()) {
            updateTokenFromFields();
            return;
        }

        tokenValueField.setText(tokenValueField.getText().isBlank() ? "token" : tokenValueField.getText());
        addTokenFromFields();
    }

    private void addTokenFromFields() {
        tokens.add(tokenFromFields());
        selectedTokenIndex = tokens.size() - 1;
        refreshTokenPicker();
        selectToken(selectedTokenIndex, "Added token.");
    }

    private void updateTokenFromFields() {
        if (selectedTokenIndex < 0 || selectedTokenIndex >= tokens.size()) {
            addTokenFromFields();
            return;
        }

        tokens.set(selectedTokenIndex, tokenFromFields());
        refreshTokenPicker();
        selectToken(selectedTokenIndex, "Updated token.");
    }

    private void deleteSelectedToken() {
        if (selectedTokenIndex < 0 || selectedTokenIndex >= tokens.size()) {
            return;
        }

        tokens.remove(selectedTokenIndex);
        selectedTokenIndex = -1;
        refreshTokenPicker();
        renderAndExport("Deleted token.");
    }

    private TokenSpec tokenFromFields() {
        return new TokenSpec(
                tokenValueField.getText(),
                tokenTypePicker.getValue(),
                tokenSourcePicker.getValue(),
                parseIntField(tokenColumnField, 0, COLUMNS - 1),
                parseIntField(tokenRowField, 0, ROWS - 1),
                parseIntField(tokenChestOrderField, 0, Math.max(0, chests.size()))
        );
    }

    private void selectToken(int index, String status) {
        if (index < 0 || index >= tokens.size()) {
            return;
        }

        selectedTokenIndex = index;
        TokenSpec token = tokens.get(index);
        syncingTokenControls = true;
        tokenValueField.setText(token.value());
        tokenTypePicker.setValue(token.type());
        tokenSourcePicker.setValue(token.source());
        tokenColumnField.setText(String.valueOf(token.column()));
        tokenRowField.setText(String.valueOf(token.row()));
        tokenChestOrderField.setText(String.valueOf(token.chestOrder()));
        tokenPicker.getSelectionModel().select(index);
        syncingTokenControls = false;
        renderAndExport(status);
    }

    private void refreshTokenPicker() {
        syncingTokenControls = true;
        tokenPicker.getItems().clear();
        for (int i = 0; i < tokens.size(); i++) {
            tokenPicker.getItems().add(new TokenChoice(i, tokens.get(i)));
        }
        if (selectedTokenIndex >= 0 && selectedTokenIndex < tokenPicker.getItems().size()) {
            tokenPicker.getSelectionModel().select(selectedTokenIndex);
        } else {
            tokenPicker.getSelectionModel().clearSelection();
        }
        syncingTokenControls = false;
    }

    private void toggleQuestionDoor(int column, int row, GridRoomBuilder.Side side) {
        Optional<QuestionDoorSpec> existingDoor = questionDoorAt(column, row, side);
        if (existingDoor.isPresent()) {
            questionDoors.remove(existingDoor.get());
            cells[row][column] &= ~doorFlag(side);
            renderAndExport("Removed question door.");
            return;
        }

        cells[row][column] &= ~wallFlag(side);
        cells[row][column] |= doorFlag(side);
        questionDoors.add(new QuestionDoorSpec(
                column,
                row,
                side,
                promptField.getText(),
                codeField.getText(),
                choicesField.getText(),
                answerField.getText(),
                rewardField.getText()
        ));
        renderAndExport("Added question door at cell (" + column + ", " + row + ").");
    }

    private void validateCurrentLayout() {
        syncSingleQuestionDoorFields();
        try {
            validateTokenPlacements();
            RoomLayoutBuilder.fromOverride(currentOverride());
            String warning = questionDoors.size() > 1
                    ? " Runtime rooms currently use the first question door; the extra door data is still saved/exported."
                    : "";
            statusLabel.setText("Layout is reachable and objects do not overlap." + warning);
        } catch (RuntimeException exception) {
            statusLabel.setText("Validation failed: " + exception.getMessage());
        }
    }

    private void clearLayout() {
        clearLayoutStateOnly();
        renderAndExport("Cleared the editor.");
    }

    private void clearStateOnly() {
        clearLayoutStateOnly();
        tokens.clear();
        selectedTokenIndex = -1;
        refreshTokenPicker();
    }

    private void clearLayoutStateOnly() {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                cells[row][column] = 0;
            }
        }
        chests.clear();
        questionDoors.clear();
        selectedChestIndex = -1;
        syncingChestControls = true;
        lockedChestBox.setSelected(false);
        syncingChestControls = false;
    }

    private void renderAndExport(String status) {
        renderGrid();
        refreshExport();
        statusLabel.setText(status);
    }

    private void refreshExport() {
        syncSingleQuestionDoorFields();
        exportArea.setText(createExportText());
    }

    private void copyExport() {
        ClipboardContent content = new ClipboardContent();
        content.putString(exportArea.getText());
        Clipboard.getSystemClipboard().setContent(content);
        statusLabel.setText("Copied the level layout export.");
    }

    private LevelLayoutOverride currentOverride() {
        syncSelectedTokenFields();
        List<LevelLayoutOverride.ChestPlacement> chestPlacements = chests.stream()
                .map(chest -> new LevelLayoutOverride.ChestPlacement(chest.column(), chest.row(), chest.lockedRoomOnly()))
                .toList();
        List<LevelLayoutOverride.QuestionDoorPlacement> doorPlacements = questionDoors.stream()
                .map(door -> new LevelLayoutOverride.QuestionDoorPlacement(
                        door.column(),
                        door.row(),
                        door.side(),
                        door.prompt(),
                        door.code(),
                        parseChoices(door.choices()),
                        door.answer(),
                        door.reward()
                ))
                .toList();
        List<LevelLayoutOverride.TokenPlacement> tokenPlacements = tokens.stream()
                .map(token -> new LevelLayoutOverride.TokenPlacement(
                        token.value(),
                        token.type(),
                        token.source() == TokenSource.CHEST
                                ? LevelLayoutOverride.TokenPlacementKind.CHEST
                                : LevelLayoutOverride.TokenPlacementKind.VISIBLE,
                        token.column(),
                        token.row(),
                        token.chestOrder()
                ))
                .toList();
        return new LevelLayoutOverride(
                activeLevelNumber,
                COLUMNS,
                ROWS,
                copyCells(),
                chestPlacements,
                doorPlacements,
                goalTitleField.getText(),
                goalInstructionsArea.getText(),
                helperArea.getText(),
                acceptedAnswers(),
                true,
                tokenPlacements
        );
    }

    private void syncSelectedTokenFields() {
        if (syncingTokenControls || selectedTokenIndex < 0 || selectedTokenIndex >= tokens.size()) {
            return;
        }

        tokens.set(selectedTokenIndex, tokenFromFields());
    }

    private int[][] copyCells() {
        int[][] copy = new int[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            System.arraycopy(cells[row], 0, copy[row], 0, COLUMNS);
        }
        return copy;
    }

    private String createExportText() {
        LevelLayoutOverride override = currentOverride();
        StringBuilder out = new StringBuilder();
        out.append("// Saved game override for ").append(selectedLevelLabel()).append("\n");
        out.append("// Matrix flags: 1 top wall, 2 right wall, 4 bottom wall, 8 left wall,\n");
        out.append("// 16 top question door, 32 right question door, 64 bottom question door, 128 left question door.\n");
        out.append("int[][] cells = {\n");
        int[][] exportedCells = override.copyCells();
        for (int row = 0; row < ROWS; row++) {
            out.append("        {");
            for (int column = 0; column < COLUMNS; column++) {
                if (column > 0) {
                    out.append(", ");
                }
                out.append(exportedCells[row][column]);
            }
            out.append("}");
            if (row < ROWS - 1) {
                out.append(",");
            }
            out.append("\n");
        }
        out.append("};\n\n");

        out.append("GridRoomBuilder.GridRoomLayout layout = GridRoomBuilder.builder(")
                .append(COLUMNS)
                .append(", ")
                .append(ROWS)
                .append(")\n")
                .append("        .origin(")
                .append((int) RoomLayoutBuilder.GRID_ORIGIN_X)
                .append(", ")
                .append((int) RoomLayoutBuilder.GRID_ORIGIN_Y)
                .append(")\n")
                .append("        .cellSize(")
                .append((int) RoomLayoutBuilder.GRID_CELL_WIDTH)
                .append(", ")
                .append((int) RoomLayoutBuilder.GRID_CELL_HEIGHT)
                .append(")\n")
                .append("        .wallThickness(")
                .append((int) RoomLayoutBuilder.WALL_THICKNESS)
                .append(")\n")
                .append("        .spawnCell(0, 0)\n");
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (exportedCells[row][column] != 0) {
                    out.append("        .cellFlags(")
                            .append(column)
                            .append(", ")
                            .append(row)
                            .append(", ")
                            .append(exportedCells[row][column])
                            .append(")\n");
                }
            }
        }
        if (!override.questionDoors().isEmpty()) {
            LevelLayoutOverride.QuestionDoorPlacement firstDoor = override.questionDoors().get(0);
            out.append("        .challengeDoor(")
                    .append(firstDoor.column())
                    .append(", ")
                    .append(firstDoor.row())
                    .append(", GridRoomBuilder.Side.")
                    .append(firstDoor.side().name())
                    .append(")\n");
        }
        for (LevelLayoutOverride.ChestPlacement chest : override.chests()) {
            out.append("        .object(")
                    .append(chest.column())
                    .append(", ")
                    .append(chest.row())
                    .append(", GridRoomBuilder.")
                    .append(chest.lockedRoomOnly() ? "lockedRoomChest" : "chest")
                    .append("())\n");
        }
        out.append("        .build();\n\n");

        out.append("// Goal title: ").append(override.goalTitle()).append("\n");
        out.append("// Goal text: ").append(override.goalInstructions()).append("\n");
        out.append("// Helper: ").append(override.helperText()).append("\n");
        out.append("// Accepted final answers:\n");
        if (override.acceptedAnswers().isEmpty()) {
            out.append("// built-in validator\n");
        } else {
            for (String answer : override.acceptedAnswers()) {
                out.append("// ").append(answer.replace("\n", "\\n")).append("\n");
            }
        }
        out.append("\n// Tokens:\n");
        if (override.tokens().isEmpty()) {
            out.append("// none\n");
        } else {
            for (LevelLayoutOverride.TokenPlacement token : override.tokens()) {
                out.append("// ")
                        .append(token.kind())
                        .append(" ")
                        .append(token.type())
                        .append(" \"")
                        .append(token.value())
                        .append("\"");
                if (token.kind() == LevelLayoutOverride.TokenPlacementKind.VISIBLE) {
                    out.append(" at cell (")
                            .append(token.column())
                            .append(", ")
                            .append(token.row())
                            .append(")");
                } else {
                    out.append(" in chest order ")
                            .append(token.chestOrder());
                }
                out.append("\n");
            }
        }
        out.append("\n");

        out.append("// Question doors:\n");
        if (override.questionDoors().isEmpty()) {
            out.append("// none\n");
        } else {
            for (int i = 0; i < override.questionDoors().size(); i++) {
                LevelLayoutOverride.QuestionDoorPlacement door = override.questionDoors().get(i);
                out.append("// Door ")
                        .append(i + 1)
                        .append(": cell (")
                        .append(door.column())
                        .append(", ")
                        .append(door.row())
                        .append(") ")
                        .append(door.side().name())
                        .append("\n");
                out.append("new MultipleChoiceQuestion(\n")
                        .append("        \"")
                        .append(escapeJava(door.prompt()))
                        .append("\",\n")
                        .append("        \"")
                        .append(escapeJava(door.code()))
                        .append("\",\n")
                        .append("        List.of(")
                        .append(quotedChoices(door.choices()))
                        .append("),\n")
                        .append("        \"")
                        .append(escapeJava(door.correctAnswer()))
                        .append("\",\n")
                        .append("        ChestReward.code(\"")
                        .append(escapeJava(door.reward()))
                        .append("\")\n")
                        .append(");\n");
            }
        }
        return out.toString();
    }

    private void syncSingleQuestionDoorFields() {
        if (questionDoors.size() != 1) {
            return;
        }

        QuestionDoorSpec door = questionDoors.get(0);
        questionDoors.set(0, new QuestionDoorSpec(
                door.column(),
                door.row(),
                door.side(),
                promptField.getText(),
                codeField.getText(),
                choicesField.getText(),
                answerField.getText(),
                rewardField.getText()
        ));
    }

    private void updateQuestionFieldsFromFirstDoor() {
        if (questionDoors.isEmpty()) {
            return;
        }

        QuestionDoorSpec door = questionDoors.get(0);
        promptField.setText(door.prompt());
        codeField.setText(door.code());
        choicesField.setText(door.choices());
        answerField.setText(door.answer());
        rewardField.setText(door.reward());
    }

    private Optional<Integer> chestAt(int column, int row) {
        for (int i = 0; i < chests.size(); i++) {
            ChestSpec chest = chests.get(i);
            if (chest.column() == column && chest.row() == row) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private Optional<QuestionDoorSpec> questionDoorAt(int column, int row, GridRoomBuilder.Side side) {
        return questionDoors.stream()
                .filter(door -> door.column() == column && door.row() == row && door.side() == side)
                .findFirst();
    }

    private void removeQuestionDoor(int column, int row, GridRoomBuilder.Side side) {
        questionDoors.removeIf(door -> door.column() == column && door.row() == row && door.side() == side);
    }

    private void markChestsBehindDoors() {
        if (questionDoors.isEmpty()) {
            return;
        }

        Set<Cell> reachableBeforeDoors = reachableCells(false);
        Set<Cell> reachableAfterDoors = reachableCells(true);
        for (int i = 0; i < chests.size(); i++) {
            ChestSpec chest = chests.get(i);
            Cell cell = new Cell(chest.column(), chest.row());
            boolean behindDoor = !reachableBeforeDoors.contains(cell) && reachableAfterDoors.contains(cell);
            if (behindDoor) {
                chests.set(i, new ChestSpec(chest.column(), chest.row(), true));
            }
        }
    }

    private Set<Cell> reachableCells(boolean doorsOpen) {
        Set<Cell> visited = new HashSet<>();
        Queue<Cell> queue = new ArrayDeque<>();
        Cell spawn = new Cell(0, 0);
        visited.add(spawn);
        queue.add(spawn);

        while (!queue.isEmpty()) {
            Cell current = queue.remove();
            for (GridRoomBuilder.Side side : GridRoomBuilder.Side.values()) {
                Cell next = neighbor(current, side);
                if (!isInside(next) || visited.contains(next) || edgeBlocksMovement(current, side, doorsOpen)) {
                    continue;
                }
                visited.add(next);
                queue.add(next);
            }
        }
        return visited;
    }

    private boolean edgeBlocksMovement(Cell cell, GridRoomBuilder.Side side, boolean doorsOpen) {
        Cell next = neighbor(cell, side);
        GridRoomBuilder.Side opposite = opposite(side);
        boolean door = hasDoor(cell.column(), cell.row(), side)
                || (isInside(next) && hasDoor(next.column(), next.row(), opposite));
        if (door) {
            return !doorsOpen;
        }

        return hasWall(cell.column(), cell.row(), side)
                || (isInside(next) && hasWall(next.column(), next.row(), opposite));
    }

    private Cell neighbor(Cell cell, GridRoomBuilder.Side side) {
        return switch (side) {
            case TOP -> new Cell(cell.column(), cell.row() - 1);
            case RIGHT -> new Cell(cell.column() + 1, cell.row());
            case BOTTOM -> new Cell(cell.column(), cell.row() + 1);
            case LEFT -> new Cell(cell.column() - 1, cell.row());
        };
    }

    private Optional<Edge> inferEdge(double x, double y, double width, double height) {
        if (width >= height) {
            int edgeRow = (int) Math.round((y + height / 2.0 - RoomLayoutBuilder.GRID_ORIGIN_Y)
                    / RoomLayoutBuilder.GRID_CELL_HEIGHT);
            int column = (int) Math.round((x - RoomLayoutBuilder.GRID_ORIGIN_X) / RoomLayoutBuilder.GRID_CELL_WIDTH);
            if (column < 0 || column >= COLUMNS) {
                return Optional.empty();
            }
            if (edgeRow <= 0) {
                return Optional.of(new Edge(column, 0, GridRoomBuilder.Side.TOP));
            }
            if (edgeRow >= ROWS) {
                return Optional.of(new Edge(column, ROWS - 1, GridRoomBuilder.Side.BOTTOM));
            }
            return Optional.of(new Edge(column, edgeRow - 1, GridRoomBuilder.Side.BOTTOM));
        }

        int edgeColumn = (int) Math.round((x + width / 2.0 - RoomLayoutBuilder.GRID_ORIGIN_X)
                / RoomLayoutBuilder.GRID_CELL_WIDTH);
        int row = (int) Math.round((y - RoomLayoutBuilder.GRID_ORIGIN_Y) / RoomLayoutBuilder.GRID_CELL_HEIGHT);
        if (row < 0 || row >= ROWS) {
            return Optional.empty();
        }
        if (edgeColumn <= 0) {
            return Optional.of(new Edge(0, row, GridRoomBuilder.Side.LEFT));
        }
        if (edgeColumn >= COLUMNS) {
            return Optional.of(new Edge(COLUMNS - 1, row, GridRoomBuilder.Side.RIGHT));
        }
        return Optional.of(new Edge(edgeColumn - 1, row, GridRoomBuilder.Side.RIGHT));
    }

    private Cell cellFromRuntimePoint(double x, double y) {
        int column = (int) ((x - RoomLayoutBuilder.GRID_ORIGIN_X) / RoomLayoutBuilder.GRID_CELL_WIDTH);
        int row = (int) ((y - RoomLayoutBuilder.GRID_ORIGIN_Y) / RoomLayoutBuilder.GRID_CELL_HEIGHT);
        return new Cell(column, row);
    }

    private boolean hasWall(int column, int row, GridRoomBuilder.Side side) {
        return (cells[row][column] & wallFlag(side)) != 0;
    }

    private boolean hasDoor(int column, int row, GridRoomBuilder.Side side) {
        return (cells[row][column] & doorFlag(side)) != 0;
    }

    private int wallFlag(GridRoomBuilder.Side side) {
        return switch (side) {
            case TOP -> GridRoomBuilder.TOP;
            case RIGHT -> GridRoomBuilder.RIGHT;
            case BOTTOM -> GridRoomBuilder.BOTTOM;
            case LEFT -> GridRoomBuilder.LEFT;
        };
    }

    private int doorFlag(GridRoomBuilder.Side side) {
        return switch (side) {
            case TOP -> GridRoomBuilder.TOP_DOOR;
            case RIGHT -> GridRoomBuilder.RIGHT_DOOR;
            case BOTTOM -> GridRoomBuilder.BOTTOM_DOOR;
            case LEFT -> GridRoomBuilder.LEFT_DOOR;
        };
    }

    private GridRoomBuilder.Side opposite(GridRoomBuilder.Side side) {
        return switch (side) {
            case TOP -> GridRoomBuilder.Side.BOTTOM;
            case RIGHT -> GridRoomBuilder.Side.LEFT;
            case BOTTOM -> GridRoomBuilder.Side.TOP;
            case LEFT -> GridRoomBuilder.Side.RIGHT;
        };
    }

    private GridRoomBuilder.Side selectedSide() {
        return sidePicker.getValue() == null ? GridRoomBuilder.Side.RIGHT : sidePicker.getValue();
    }

    private Point2D edgeCenter(int column, int row, GridRoomBuilder.Side side) {
        double x = column * DISPLAY_CELL_WIDTH;
        double y = row * DISPLAY_CELL_HEIGHT;
        return switch (side) {
            case TOP -> new Point2D(x + DISPLAY_CELL_WIDTH / 2.0, y);
            case RIGHT -> new Point2D(x + DISPLAY_CELL_WIDTH, y + DISPLAY_CELL_HEIGHT / 2.0);
            case BOTTOM -> new Point2D(x + DISPLAY_CELL_WIDTH / 2.0, y + DISPLAY_CELL_HEIGHT);
            case LEFT -> new Point2D(x, y + DISPLAY_CELL_HEIGHT / 2.0);
        };
    }

    private boolean isInside(Cell cell) {
        return cell.column() >= 0 && cell.column() < COLUMNS && cell.row() >= 0 && cell.row() < ROWS;
    }

    private int clampCell(int value, int maxExclusive) {
        return Math.max(0, Math.min(maxExclusive - 1, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String statusForMode() {
        return switch (mode) {
            case WALLS -> "Wall mode: choose a side, then click cells to toggle that wall.";
            case CHESTS -> "Chest mode: click empty cells to add chests, drag chests to move them, right-click to remove.";
            case TOKENS -> "Token mode: choose or add a token, then click a cell to place it visibly.";
            case QUESTION_DOORS -> "Question-door mode: fill the question fields, choose a side, then click a cell edge.";
        };
    }

    private List<String> parseChoices(String choicesText) {
        if (choicesText == null || choicesText.isBlank()) {
            return List.of();
        }

        List<String> choices = new ArrayList<>();
        for (String choice : choicesText.split(",")) {
            String trimmed = choice.trim();
            if (!trimmed.isEmpty()) {
                choices.add(trimmed);
            }
        }
        return choices;
    }

    private List<String> acceptedAnswers() {
        if (acceptedAnswersArea.getText() == null || acceptedAnswersArea.getText().isBlank()) {
            return List.of();
        }

        List<String> answers = new ArrayList<>();
        for (String answer : acceptedAnswersArea.getText().split("\\R")) {
            if (!answer.isBlank()) {
                answers.add(answer.trim());
            }
        }
        return answers;
    }

    private int parseIntField(TextField field, int min, int max) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException exception) {
            return min;
        }
    }

    private String quotedChoices(List<String> choices) {
        StringBuilder quoted = new StringBuilder();
        for (int i = 0; i < choices.size(); i++) {
            if (i > 0) {
                quoted.append(", ");
            }
            quoted.append("\"").append(escapeJava(choices.get(i))).append("\"");
        }
        return quoted.toString();
    }

    private String escapeJava(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String selectedLevelLabel() {
        LevelChoice selected = levelPicker.getSelectionModel().getSelectedItem();
        return selected == null ? "level " + activeLevelNumber : selected.toString();
    }

    private enum EditorMode {
        WALLS,
        CHESTS,
        TOKENS,
        QUESTION_DOORS
    }

    private enum TokenSource {
        VISIBLE,
        CHEST
    }

    private record LevelChoice(int levelNumber, String displayId, String name) {
        @Override
        public String toString() {
            return displayId + " - " + name;
        }
    }

    private record ChestSpec(int column, int row, boolean lockedRoomOnly) {
    }

    private record TokenChoice(int index, TokenSpec token) {
        @Override
        public String toString() {
            return (index + 1) + ": " + token.value() + " (" + token.source() + ")";
        }
    }

    private record TokenSpec(String value, TokenType type, TokenSource source, int column, int row, int chestOrder) {
        private TokenSpec {
            value = value == null ? "" : value;
            type = type == null ? TokenType.CODE : type;
            source = source == null ? TokenSource.VISIBLE : source;
        }
    }

    private record QuestionDoorSpec(
            int column,
            int row,
            GridRoomBuilder.Side side,
            String prompt,
            String code,
            String choices,
            String answer,
            String reward
    ) {
    }

    private record Edge(int column, int row, GridRoomBuilder.Side side) {
    }

    private record Cell(int column, int row) {
    }
}
