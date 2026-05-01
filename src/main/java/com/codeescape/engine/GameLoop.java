package com.codeescape.engine;

import javafx.animation.AnimationTimer;

public class GameLoop {
    private AnimationTimer timer;
    private final Runnable updateAction;

    public GameLoop(Runnable updateAction) {
        this.updateAction = updateAction;
    }

    public void start() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void update() {
        updateAction.run();
    }
}
