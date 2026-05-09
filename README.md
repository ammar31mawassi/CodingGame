# Code Escape

Code Escape is a JavaFX educational puzzle game for practicing Java basics through escape-room levels. Players move around rooms, collect code tokens, inspect their inventory, find the level goal, and use the terminal to submit Java code that unlocks the exit door.

## Current Status

The project now has a playable MVP with:

- JavaFX app navigation, main menu, level select, level completion, and game-finished screens.
- Keyboard movement with collision checks for walls, doors, tokens, chests, and challenge doors.
- Eight learning levels covering variables, strings, if-statements, if-else print statements, classes, constructors, and objects.
- Goal and Helper tokens that reveal puzzle instructions and hints.
- Inventory UI for collected code tokens and token descriptions.
- Terminal puzzle solving through token buttons or typed Java code.
- Bug/life tracking for wrong answers, with restart after three mistakes.
- Normal and Hard modes; Hard mode adds a limited light radius around the player.
- One saved progress slot using checkpoint saves between sessions, with Continue Game and locked future levels.
- Escape-key pause menu with resume, main-menu return, and a delayed thank-you exit.
- Maze, chest, challenge-door, and programmable-object gameplay for later levels.
- Unit tests for validators, maze generation, progress saving, level loading, and room reward behavior.

## Run

Use the Maven wrapper with JDK 17 available.

On macOS/Linux:

```bash
./mvnw javafx:run
```

On Windows:

```bash
.\mvnw.cmd javafx:run
```

## Test

On macOS/Linux:

```bash
./mvnw test
```

On Windows:

```bash
.\mvnw.cmd test
```

If Maven reports that `JAVA_HOME` is missing, set it to a JDK 17 installation before running the wrapper.

## Project Layout

- `src/main/java/com/codeescape/app`: JavaFX application flow and scene switching.
- `src/main/java/com/codeescape/engine`: game state, level loading, collision checks, and maze generation.
- `src/main/java/com/codeescape/model`: game objects such as player, room, token, chest, door, puzzle, and inventory.
- `src/main/java/com/codeescape/ui`: JavaFX views for gameplay, menus, inventory, terminal, and end screens.
- `src/main/java/com/codeescape/validation`: Java code validators used by puzzle levels.
- `src/main/resources`: CSS and image assets.
- `src/test/java`: JUnit tests for core behavior.

## Next Ideas

- Add sound effects and animation polish.
