package com.app.utils;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {
    // Chuyển scene (event)
    public static FXMLLoader switchScene(String fxmlPath, String cssPath, Event event
            , boolean maximized) throws IOException {
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

        stage.setScene(scene);

        if (maximized) {
            // Lấy kích thước màn hình khả dụng (không bị taskbar che)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Set kích thước và vị trí stage theo màn hình
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            stage.setMaximized(false); // Tắt maximize nếu có
        } else {
            stage.sizeToScene();
            stage.setMaximized(false);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - stage.getWidth()) / 2;
            double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - stage.getHeight()) / 2;

            stage.setX(centerX);
            stage.setY(centerY);
        }

        return loader;
    }

    // Chuyển scene (dùng node)
    public static FXMLLoader switchScene(String fxmlPath, String cssPath, Node nodeInScene) throws IOException {
        Stage stage = (Stage) nodeInScene.getScene().getWindow();  // Lấy stage từ node

        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        if (cssPath != null) {
            scene.getStylesheets().add(SceneNavigator.class.getResource(cssPath).toExternalForm());
        }

        stage.setScene(scene);
        stage.show();

        return loader;
    }

    // Hiển thị scene trong một cửa sổ mới (Stage mới), dạng popup => ko return
    public static void showPopupScene(String fxmlPath, String cssPath, Stage owner) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        if (cssPath != null) {
            scene.getStylesheets().add(SceneNavigator.class.getResource(cssPath).toExternalForm());
        }

        Stage popupStage = new Stage();
        popupStage.setScene(scene);
        popupStage.setResizable(false);

        if (owner != null) {
            popupStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            popupStage.initOwner(owner);
        }

        popupStage.showAndWait(); // Dùng showAndWait => tự reload được bảng
    }

    // Hiển thị scene trong một cửa sổ mới (Stage mới), dạng popup => FXML Loader
    public static FXMLLoader showPopupSceneFXML(String fxmlPath, String cssPath, Stage owner) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        if (cssPath != null) {
            scene.getStylesheets().add(SceneNavigator.class.getResource(cssPath).toExternalForm());
        }

        Stage popupStage = new Stage();
        popupStage.setScene(scene);
        popupStage.setResizable(false);

        if (owner != null) {
            popupStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            popupStage.initOwner(owner);
        }

        popupStage.show(); // Dùng show => không tự reload bảng => Dùng interface Callback

        return loader;
    }
}
