package com.app.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class HomePage extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/home-page.fxml"));

        // Tạo Scene và gán stylesheet
        Scene scene = new Scene(root);
        scene.getStylesheets().add(CreateAccount.class.getResource("/styles/home-page.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT); // Cho phép bo góc

        //        primaryStage.initStyle(StageStyle.TRANSPARENT); // Không khung viền
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        //        primaryStage.setResizable(false); // Khóa kích thước cửa sổ
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
