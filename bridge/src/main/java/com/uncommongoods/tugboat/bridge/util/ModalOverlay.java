// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.util;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

/**
 * Shows modal content as a scrimmed overlay inside the window that already hosts the UI,
 * rather than in a second top-level Stage. A separate Stage has to be positioned by hand
 * and JavaFX centers an owned Stage on the screen, not on its owner, so it drifts away
 * from the window as soon as the window is moved. An overlay lives in the scene graph and
 * is laid out by the window, so it tracks moves and resizes for free.
 */
public final class ModalOverlay {

    private static final String SCRIM_STYLE = "-fx-background-color: rgba(0, 0, 0, 0.3);";

    private ModalOverlay() {
    }

    /**
     * Adds {@code content} to the scene root of {@code anchor}, behind a dimming scrim
     * that swallows mouse and keyboard input aimed at the UI underneath.
     *
     * @param anchor  any node already attached to the scene the overlay should cover
     * @param content the modal content, centered within the scrim
     * @return an idempotent action that dismisses the overlay
     */
    public static Runnable show(Node anchor, Node content) {
        Scene scene = anchor.getScene();
        if (scene == null || !(scene.getRoot() instanceof StackPane host)) {
            throw new IllegalStateException(
                "ModalOverlay requires the anchor's scene root to be a StackPane, but was: "
                    + (scene == null ? "no scene" : scene.getRoot().getClass().getName()));
        }

        StackPane scrim = new StackPane(content);
        scrim.getStyleClass().add("modal-overlay");
        scrim.setStyle(SCRIM_STYLE);

        // Keep focus traversal from walking out of the modal into the UI behind it. The
        // scrim's background already makes it opaque to the mouse, but not to the keyboard.
        EventHandler<KeyEvent> keyGuard = event -> {
            if (event.getTarget() instanceof Node target && isDescendant(target, scrim)) {
                return;
            }
            event.consume();
        };
        scene.addEventFilter(KeyEvent.ANY, keyGuard);

        Node previouslyFocused = scene.getFocusOwner();
        host.getChildren().add(scrim);
        Platform.runLater(() -> focusFirstTraversable(content));

        return new Runnable() {
            private boolean dismissed = false;

            @Override
            public void run() {
                if (dismissed) {
                    return;
                }
                dismissed = true;
                scene.removeEventFilter(KeyEvent.ANY, keyGuard);
                host.getChildren().remove(scrim);
                if (previouslyFocused != null) {
                    previouslyFocused.requestFocus();
                }
            }
        };
    }

    private static boolean isDescendant(Node node, Node ancestor) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current == ancestor) {
                return true;
            }
        }
        return false;
    }

    private static boolean focusFirstTraversable(Node node) {
        if (!node.isVisible() || node.isDisabled()) {
            return false;
        }
        if (node.isFocusTraversable()) {
            node.requestFocus();
            return true;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (focusFirstTraversable(child)) {
                    return true;
                }
            }
        }
        return false;
    }
}
