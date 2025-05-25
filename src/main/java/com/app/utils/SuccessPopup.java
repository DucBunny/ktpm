package com.app.utils;

import com.app.controllers.AlertController;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class SuccessPopup {
    public static void showSuccessPopup(Stage owner, String message) throws IOException {
        FXMLLoader loader = new FXMLLoader(SuccessPopup.class.getResource("/fxml/alert.fxml"));
        Parent root = loader.load();

        AlertController controller = loader.getController();
        controller.setMessage(message);

        Stage popup = new Stage(StageStyle.TRANSPARENT);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.initOwner(owner);
        popup.setScene(scene);
        popup.setAlwaysOnTop(true);

        popup.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> popup.close());
        delay.play();
    }
}
