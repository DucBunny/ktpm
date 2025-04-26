package com.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/sign-in.fxml"));

        // Tạo Scene và gán stylesheet
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/sign-in.css").toExternalForm());

        // Thiết lập cửa sổ chính
        primaryStage.setTitle("Sign In");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Khóa kích thước cửa sổ
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
