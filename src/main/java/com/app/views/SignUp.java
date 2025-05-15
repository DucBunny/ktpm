package com.app.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class SignUp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/sign-up.fxml"));

        // Tạo Scene và gán stylesheet
        Scene scene = new Scene(root);
        scene.getStylesheets().add(SignUp.class.getResource("/styles/sign-in-sign-up.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT); // Cho phép bo góc

        // Thiết lập cửa sổ chính
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
