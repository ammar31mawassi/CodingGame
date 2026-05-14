package com.codeescape.ui;

import com.codeescape.engine.MedalRank;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public final class MedalBadgeView {
    private MedalBadgeView() {
    }

    public static HBox create(MedalRank medalRank) {
        HBox badge = new HBox(8, createIcon(medalRank, false), createLabel(medalRank.getDisplayName()));
        badge.setAlignment(Pos.CENTER);
        badge.getStyleClass().addAll("medal-badge", "medal-badge-" + medalRank.getStyleSuffix());
        return badge;
    }

    public static HBox createCount(MedalRank medalRank, long count) {
        HBox badge = new HBox(5, createIcon(medalRank, true), createLabel(String.valueOf(count)));
        badge.setAlignment(Pos.CENTER);
        badge.getStyleClass().addAll("medal-badge", "medal-badge-compact", "medal-badge-" + medalRank.getStyleSuffix());
        return badge;
    }

    public static HBox createLevelBadge(String levelId, MedalRank medalRank) {
        HBox badge = new HBox(7, createLabel(levelId), createIcon(medalRank, true), createLabel(medalRank.getDisplayName()));
        badge.setAlignment(Pos.CENTER);
        badge.getStyleClass().addAll("medal-badge", "medal-badge-level", "medal-badge-" + medalRank.getStyleSuffix());
        return badge;
    }

    public static HBox createEmptyLevelBadge(String levelId) {
        HBox badge = new HBox(7, createLabel(levelId), createLabel("No medal"));
        badge.setAlignment(Pos.CENTER);
        badge.getStyleClass().addAll("medal-badge", "medal-badge-empty");
        return badge;
    }

    private static Node createIcon(MedalRank medalRank, boolean compact) {
        double radius = compact ? 10 : 13;
        double ribbonWidth = compact ? 6 : 8;
        double ribbonHeight = compact ? 10 : 13;
        double ribbonY = compact ? 10 : 13;

        Rectangle leftRibbon = new Rectangle(ribbonWidth, ribbonHeight);
        leftRibbon.setTranslateX(-ribbonWidth / 1.7);
        leftRibbon.setTranslateY(ribbonY);
        leftRibbon.getStyleClass().addAll("medal-ribbon", "medal-ribbon-left");

        Rectangle rightRibbon = new Rectangle(ribbonWidth, ribbonHeight);
        rightRibbon.setTranslateX(ribbonWidth / 1.7);
        rightRibbon.setTranslateY(ribbonY);
        rightRibbon.getStyleClass().addAll("medal-ribbon", "medal-ribbon-right");

        Circle disc = new Circle(radius);
        disc.getStyleClass().addAll("medal-disc", "medal-disc-" + medalRank.getStyleSuffix());

        Label symbol = new Label(medalRank.getShortLabel());
        symbol.getStyleClass().add("medal-symbol");

        StackPane icon = new StackPane(leftRibbon, rightRibbon, disc, symbol);
        double size = compact ? 28 : 34;
        double height = compact ? 32 : 38;
        icon.setMinSize(size, height);
        icon.setPrefSize(size, height);
        icon.setMaxSize(size, height);
        icon.getStyleClass().add("medal-icon");
        return icon;
    }

    private static Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("medal-label");
        return label;
    }
}
