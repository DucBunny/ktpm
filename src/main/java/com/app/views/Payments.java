package com.app.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class Payments extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Payments/PaymentDetail/payments.fxml"));

        // Tạo Scene và gán stylesheet
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Payments.class.getResource("/styles/Payments/payments.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT); // Cho phép bo góc

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
