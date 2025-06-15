package com.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/HeaderUtils/sign-in.fxml"));

        // Tạo Scene và gán stylesheet
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/sign-in-create-account.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT); // Cho phép bo góc

        // Thiết lập cửa sổ chính
        //        primaryStage.initStyle(StageStyle.TRANSPARENT); // Không khung viền
        primaryStage.setResizable(false); // Không resize
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
