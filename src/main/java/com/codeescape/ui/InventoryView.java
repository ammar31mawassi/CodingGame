package com.codeescape.ui;

import com.codeescape.model.Inventory;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InventoryView {
    private Inventory inventory;
    private final HBox tokenBox = new HBox(8);

    public InventoryView(Inventory inventory) {
        this.inventory = inventory;
    }

    public Parent createView() {
        VBox root = new VBox(8, new Label("Inventory"), tokenBox);
        refresh();
        return root;
    }

    public void refresh() {
        tokenBox.getChildren().setAll(
                inventory.getTokens().stream()
                        .map(token -> new Label(token.getValue()))
                        .toList()
        );
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        refresh();
    }
}
