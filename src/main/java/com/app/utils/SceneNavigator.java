package com.app.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {
    // Mouse Event
    public static void switchScene(String fxmlPath, String cssPath, MouseEvent event, boolean transparent, boolean maximized) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        if (cssPath != null) {
            scene.getStylesheets().add(SceneNavigator.class.getResource(cssPath).toExternalForm());
        }

        // Bo viền
        if (transparent) {
            scene.setFill(Color.TRANSPARENT);
        }

        stage.setScene(scene);
        stage.setMaximized(maximized);
    }

    // Action Event
    public static void switchScene(String fxmlPath, String cssPath, ActionEvent event, boolean transparent, boolean maximized) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        if (cssPath != null) {
            scene.getStylesheets().add(SceneNavigator.class.getResource(cssPath).toExternalForm());
        }

        // Bo viền
        if (transparent) {
            scene.setFill(Color.TRANSPARENT);
        }

        stage.setScene(scene);
        stage.setMaximized(maximized);
    }
}
