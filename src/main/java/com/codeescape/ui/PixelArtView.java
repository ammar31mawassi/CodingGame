package com.codeescape.ui;

import java.util.Map;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public final class PixelArtView {
    private PixelArtView() {
    }

    public static Pane terminalLogo(int scale) {
        return create(new String[]{
                "................",
                "..BBBBBBBBBBBB..",
                ".BFFFFFFFFFFFFB.",
                ".BFCCCCCCCCCCFB.",
                ".BFC..C..C...FB.",
                ".BF.C..C..C..FB.",
                ".BFCCCCCCCCCCFB.",
                ".BFFFFFFFFFFFFB.",
                "..BBBBBBBBBBBB..",
                "....DDDDDDDD....",
                "...DFFFFFFFFD...",
                "...DDDDDDDDDD..."
        }, palette(), scale);
    }

    public static Pane bug(int scale) {
        return create(new String[]{
                "...........",
                "..R.....R..",
                "...R...R...",
                "..RRRRRRR..",
                ".RWRRRRRWR.",
                ".RRRRRRRRR.",
                "R.RRRRRRR.R",
                "..R.RRR.R..",
                ".R.R...R.R.",
                "..........."
        }, palette(), scale);
    }

    private static Pane create(String[] pixels, Map<Character, String> colors, int scale) {
        int pixelSize = Math.max(2, scale);
        int rows = pixels.length;
        int columns = pixels[0].length();
        Pane pane = new Pane();
        pane.setMinSize(columns * pixelSize, rows * pixelSize);
        pane.setPrefSize(columns * pixelSize, rows * pixelSize);
        pane.setMaxSize(columns * pixelSize, rows * pixelSize);
        pane.setPickOnBounds(false);

        for (int y = 0; y < rows; y++) {
            String row = pixels[y];
            for (int x = 0; x < columns; x++) {
                char key = row.charAt(x);
                String color = colors.get(key);
                if (color == null) {
                    continue;
                }
                Rectangle rectangle = new Rectangle(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
                rectangle.setFill(Color.web(color));
                rectangle.setStrokeWidth(0);
                pane.getChildren().add(rectangle);
            }
        }

        pane.getStyleClass().add("pixel-art-sprite");
        return pane;
    }

    private static Map<Character, String> palette() {
        return Map.of(
                'B', "#263544",
                'F', "#f6d65b",
                'C', "#69d7ff",
                'D', "#7b5721",
                'R', "#d44a3a",
                'W', "#f1f1ec"
        );
    }
}
