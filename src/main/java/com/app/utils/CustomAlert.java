package com.app.utils;

import com.app.controllers.CustomAlertController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class CustomAlert {
    public static void showSuccessAlert(String title, boolean autoClose, int seconds) throws IOException {
        FXMLLoader loader = new FXMLLoader(CustomAlert.class.getResource("/fxml/custom-alert.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        CustomAlertController controller = loader.getController();

        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setResizable(false);

        controller.setAlertStage(stage);

        controller.setSuccessTitle(title);
        controller.setSuccessMode(true);

        if (autoClose)
            controller.enableAutoClose(seconds);

        controller.setupMode();
        controller.startAutoClose();
        stage.showAndWait();
    }

    public static void showErrorAlert(String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(CustomAlert.class.getResource("/fxml/custom-alert.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        CustomAlertController controller = loader.getController();

        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setResizable(false);

        controller.setAlertStage(stage);

        controller.setErrorTitle(title);
        controller.setErrorMode(true);

        controller.setupMode();
        stage.showAndWait();
    }

    public static boolean showConfirmAlert(String title, String message) throws IOException {
        FXMLLoader loader = new FXMLLoader(CustomAlert.class.getResource("/fxml/custom-alert.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        CustomAlertController controller = loader.getController();

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setResizable(false);

        controller.setAlertStage(stage);
        controller.setTitle(title);
        controller.setMessage(message);
        controller.setConfirmMode(true);

        controller.setupMode();
        stage.showAndWait();

        return controller.getUserChoice();
    }
}