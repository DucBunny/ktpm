package com.app.utils;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {
    public static FXMLLoader switchScene(String fxmlPath, String cssPath, Event event,
                                         boolean transparent, boolean maximized) throws IOException {
        Stage stage;
        Object source = event.getSource();

        if (source instanceof Node) {
            stage = (Stage) ((Node) source).getScene().getWindow();
        } else {
            stage = StageManager.getPrimaryStage();
        }

        // Cập nhật Stage hiện tại
        StageManager.setPrimaryStage(stage);

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

        // Cập nhật kích thước mặc định nếu không maximize
        if (!maximized) {
            stage.sizeToScene();
        }

        return loader;
    }

    // Hiển thị scene trong một cửa sổ mới (Stage mới), dạng popup
    public static void showPopupScene(String fxmlPath, String cssPath, boolean transparent, Stage owner) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        if (cssPath != null) {
            scene.getStylesheets().add(SceneNavigator.class.getResource(cssPath).toExternalForm());
        }

        if (transparent) {
            scene.setFill(Color.TRANSPARENT);
        }

        Stage popupStage = new Stage();
        popupStage.setScene(scene);
        popupStage.setResizable(false);


        if (owner != null) {
            popupStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            popupStage.initOwner(owner);
        }

        popupStage.show(); // hoặc .showAndWait() nếu muốn chặn cửa sổ cha
    }
}
