package com.app.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class CreatePayment extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Payments/PaymentDetail/create-payment.fxml"));

        // Tạo Scene và gán stylesheet
        Scene scene = new Scene(root);
        scene.getStylesheets().add(CreateAccount.class.getResource("/styles/Payments/crud-payment.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT); // Cho phép bo góc

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
