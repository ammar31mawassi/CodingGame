package com.codeescape.ui;

import com.codeescape.model.Inventory;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class InventoryView {
    private Inventory inventory;
    private final FlowPane tokenBox = new FlowPane(8, 8);

    public InventoryView(Inventory inventory) {
        this.inventory = inventory;
    }

    public Parent createView() {
        Label title = new Label("Inventory:");
        title.getStyleClass().add("inventory-title");

        VBox root = new VBox(8, title, tokenBox);
        root.getStyleClass().add("inventory-panel");
        refresh();
        return root;
    }

    public void refresh() {
        tokenBox.getChildren().setAll(
                inventory.getTokens().stream()
                        .map(token -> {
                            Label label = new Label(token.getValue());
                            label.getStyleClass().add("inventory-token");
                            return label;
                        })
                        .toList()
        );
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        refresh();
    }
}
