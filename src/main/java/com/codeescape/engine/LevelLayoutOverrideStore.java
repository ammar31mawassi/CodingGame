package com.codeescape.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import com.codeescape.model.TokenType;

public class LevelLayoutOverrideStore {
    private static final String DIRECTORY_PROPERTY = "codeescape.levelOverrideDir";
    private static final String DEFAULT_DIRECTORY = "level-overrides";
    private static final String FILE_PREFIX = "level-";
    private static final String FILE_SUFFIX = ".properties";
    private static final int FORMAT_VERSION = 1;

    private final Path overrideDirectory;
    private final boolean enabled;

    public LevelLayoutOverrideStore() {
        this(Paths.get(System.getProperty(DIRECTORY_PROPERTY, DEFAULT_DIRECTORY)), true);
    }

    public LevelLayoutOverrideStore(Path overrideDirectory) {
        this(overrideDirectory, true);
    }

    private LevelLayoutOverrideStore(Path overrideDirectory, boolean enabled) {
        this.overrideDirectory = overrideDirectory;
        this.enabled = enabled;
    }

    public static LevelLayoutOverrideStore disabled() {
        return new LevelLayoutOverrideStore(Paths.get(DEFAULT_DIRECTORY), false);
    }

    public Optional<LevelLayoutOverride> load(int levelNumber) {
        if (!enabled) {
            return Optional.empty();
        }

        Path file = fileForLevel(levelNumber);
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            return Optional.of(fromProperties(levelNumber, properties));
        } catch (IOException | RuntimeException exception) {
            System.err.println("Could not load level layout override " + file + ": " + exception.getMessage());
            return Optional.empty();
        }
    }

    public void save(LevelLayoutOverride override) {
        if (!enabled) {
            throw new IllegalStateException("Level layout override store is disabled.");
        }

        try {
            Files.createDirectories(overrideDirectory);
            Properties properties = toProperties(override);
            try (OutputStream output = Files.newOutputStream(fileForLevel(override.levelNumber()))) {
                properties.store(output, "Code Escape level layout override");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save level layout override.", exception);
        }
    }

    public Path fileForLevel(int levelNumber) {
        return overrideDirectory.resolve(FILE_PREFIX + levelNumber + FILE_SUFFIX);
    }

    public List<LevelLayoutOverride> loadAll() {
        if (!enabled || !Files.isDirectory(overrideDirectory)) {
            return List.of();
        }

        try (java.util.stream.Stream<Path> files = Files.list(overrideDirectory)) {
            return files
                    .filter(path -> path.getFileName().toString().startsWith(FILE_PREFIX))
                    .filter(path -> path.getFileName().toString().endsWith(FILE_SUFFIX))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(this::loadFromPath)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (IOException exception) {
            System.err.println("Could not scan override directory " + overrideDirectory + ": " + exception.getMessage());
            return List.of();
        }
    }

    private Properties toProperties(LevelLayoutOverride override) {
        Properties properties = new Properties();
        properties.setProperty("version", String.valueOf(FORMAT_VERSION));
        properties.setProperty("levelNumber", String.valueOf(override.levelNumber()));
        properties.setProperty("columns", String.valueOf(override.columns()));
        properties.setProperty("rows", String.valueOf(override.rows()));
        properties.setProperty("cells", encodeCells(override.copyCells()));
        if (override.goalTitle() != null) {
            properties.setProperty("goal.title", override.goalTitle());
        }
        if (override.goalInstructions() != null) {
            properties.setProperty("goal.instructions", override.goalInstructions());
        }
        if (override.helperText() != null) {
            properties.setProperty("goal.helper", override.helperText());
        }
        properties.setProperty("acceptedAnswers", String.join("\u001e", override.acceptedAnswers()));
        properties.setProperty("tokens.overridden", String.valueOf(override.tokensOverridden()));

        properties.setProperty("chest.count", String.valueOf(override.chests().size()));
        for (int i = 0; i < override.chests().size(); i++) {
            LevelLayoutOverride.ChestPlacement chest = override.chests().get(i);
            properties.setProperty("chest." + i + ".column", String.valueOf(chest.column()));
            properties.setProperty("chest." + i + ".row", String.valueOf(chest.row()));
            properties.setProperty("chest." + i + ".lockedRoomOnly", String.valueOf(chest.lockedRoomOnly()));
        }

        properties.setProperty("questionDoor.count", String.valueOf(override.questionDoors().size()));
        for (int i = 0; i < override.questionDoors().size(); i++) {
            LevelLayoutOverride.QuestionDoorPlacement door = override.questionDoors().get(i);
            properties.setProperty("questionDoor." + i + ".column", String.valueOf(door.column()));
            properties.setProperty("questionDoor." + i + ".row", String.valueOf(door.row()));
            properties.setProperty("questionDoor." + i + ".side", door.side().name());
            properties.setProperty("questionDoor." + i + ".prompt", door.prompt());
            properties.setProperty("questionDoor." + i + ".code", door.code());
            properties.setProperty("questionDoor." + i + ".choices", String.join("\u001f", door.choices()));
            properties.setProperty("questionDoor." + i + ".correctAnswer", door.correctAnswer());
            properties.setProperty("questionDoor." + i + ".reward", door.reward());
        }

        properties.setProperty("token.count", String.valueOf(override.tokens().size()));
        for (int i = 0; i < override.tokens().size(); i++) {
            LevelLayoutOverride.TokenPlacement token = override.tokens().get(i);
            properties.setProperty("token." + i + ".value", token.value());
            properties.setProperty("token." + i + ".type", token.type().name());
            properties.setProperty("token." + i + ".kind", token.kind().name());
            properties.setProperty("token." + i + ".column", String.valueOf(token.column()));
            properties.setProperty("token." + i + ".row", String.valueOf(token.row()));
            properties.setProperty("token." + i + ".chestOrder", String.valueOf(token.chestOrder()));
        }
        return properties;
    }

    private Optional<LevelLayoutOverride> loadFromPath(Path file) {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            int levelNumber = intProperty(properties, "levelNumber", parseLevelNumber(file));
            return Optional.of(fromProperties(levelNumber, properties));
        } catch (IOException | RuntimeException exception) {
            System.err.println("Could not load level layout override " + file + ": " + exception.getMessage());
            return Optional.empty();
        }
    }

    private int parseLevelNumber(Path file) {
        String name = file.getFileName().toString();
        String number = name.substring(FILE_PREFIX.length(), name.length() - FILE_SUFFIX.length());
        return Integer.parseInt(number);
    }

    private LevelLayoutOverride fromProperties(int requestedLevelNumber, Properties properties) {
        int levelNumber = intProperty(properties, "levelNumber", requestedLevelNumber);
        int columns = intProperty(properties, "columns", RoomLayoutBuilder.GRID_COLUMNS);
        int rows = intProperty(properties, "rows", RoomLayoutBuilder.GRID_ROWS);
        int[][] cells = decodeCells(properties.getProperty("cells", ""), rows, columns);

        List<LevelLayoutOverride.ChestPlacement> chests = new ArrayList<>();
        int chestCount = intProperty(properties, "chest.count", 0);
        for (int i = 0; i < chestCount; i++) {
            chests.add(new LevelLayoutOverride.ChestPlacement(
                    intProperty(properties, "chest." + i + ".column", 0),
                    intProperty(properties, "chest." + i + ".row", 0),
                    Boolean.parseBoolean(properties.getProperty("chest." + i + ".lockedRoomOnly", "false"))
            ));
        }

        List<LevelLayoutOverride.QuestionDoorPlacement> questionDoors = new ArrayList<>();
        int questionDoorCount = intProperty(properties, "questionDoor.count", 0);
        for (int i = 0; i < questionDoorCount; i++) {
            String sideName = properties.getProperty("questionDoor." + i + ".side", GridRoomBuilder.Side.RIGHT.name());
            questionDoors.add(new LevelLayoutOverride.QuestionDoorPlacement(
                    intProperty(properties, "questionDoor." + i + ".column", 0),
                    intProperty(properties, "questionDoor." + i + ".row", 0),
                    GridRoomBuilder.Side.valueOf(sideName),
                    properties.getProperty("questionDoor." + i + ".prompt", ""),
                    properties.getProperty("questionDoor." + i + ".code", ""),
                    decodeChoices(properties.getProperty("questionDoor." + i + ".choices", "")),
                    properties.getProperty("questionDoor." + i + ".correctAnswer", ""),
                    properties.getProperty("questionDoor." + i + ".reward", "")
            ));
        }

        List<LevelLayoutOverride.TokenPlacement> tokens = new ArrayList<>();
        int tokenCount = intProperty(properties, "token.count", 0);
        for (int i = 0; i < tokenCount; i++) {
            tokens.add(new LevelLayoutOverride.TokenPlacement(
                    properties.getProperty("token." + i + ".value", ""),
                    TokenType.valueOf(properties.getProperty("token." + i + ".type", TokenType.CODE.name())),
                    LevelLayoutOverride.TokenPlacementKind.valueOf(properties.getProperty(
                            "token." + i + ".kind",
                            LevelLayoutOverride.TokenPlacementKind.VISIBLE.name()
                    )),
                    intProperty(properties, "token." + i + ".column", 0),
                    intProperty(properties, "token." + i + ".row", 0),
                    intProperty(properties, "token." + i + ".chestOrder", i)
            ));
        }

        return new LevelLayoutOverride(
                levelNumber,
                columns,
                rows,
                cells,
                chests,
                questionDoors,
                nullableProperty(properties, "goal.title"),
                nullableProperty(properties, "goal.instructions"),
                nullableProperty(properties, "goal.helper"),
                decodeAcceptedAnswers(properties.getProperty("acceptedAnswers", "")),
                Boolean.parseBoolean(properties.getProperty("tokens.overridden", "false")),
                tokens
        );
    }

    private String encodeCells(int[][] cells) {
        StringBuilder encoded = new StringBuilder();
        for (int row = 0; row < cells.length; row++) {
            if (row > 0) {
                encoded.append(';');
            }
            for (int column = 0; column < cells[row].length; column++) {
                if (column > 0) {
                    encoded.append(',');
                }
                encoded.append(cells[row][column]);
            }
        }
        return encoded.toString();
    }

    private int[][] decodeCells(String encoded, int rows, int columns) {
        int[][] cells = new int[rows][columns];
        if (encoded == null || encoded.isBlank()) {
            return cells;
        }

        String[] rowValues = encoded.split(";");
        for (int row = 0; row < Math.min(rows, rowValues.length); row++) {
            String[] columnValues = rowValues[row].split(",");
            for (int column = 0; column < Math.min(columns, columnValues.length); column++) {
                cells[row][column] = Integer.parseInt(columnValues[column].trim());
            }
        }
        return cells;
    }

    private List<String> decodeChoices(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return List.of();
        }
        return List.of(encoded.split("\u001f", -1));
    }

    private List<String> decodeAcceptedAnswers(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return List.of();
        }
        return List.of(encoded.split("\u001e", -1));
    }

    private String nullableProperty(Properties properties, String key) {
        return properties.containsKey(key) ? properties.getProperty(key) : null;
    }

    private int intProperty(Properties properties, String key, int defaultValue) {
        return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
    }
}
