package com.app.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class HomePage extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/home-page.fxml"));

        // Tạo Scene và gán stylesheet
        Scene scene = new Scene(root);
        //        scene.getStylesheets().add(getClass().getResource("/styles/sign-in.css").toExternalForm());


        // Thiết lập cửa sổ chính
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        double actualWidth = screenBounds.getWidth();
        double actualHeight = screenBounds.getHeight();

        if (actualWidth < 1440 || actualHeight < 1024) {
            primaryStage.setWidth(actualWidth);
            primaryStage.setHeight(actualHeight);
        } else {
            primaryStage.setWidth(1440);
            primaryStage.setHeight(1024);
        }

        primaryStage.setTitle("Home Page");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Khóa kích thước cửa sổ
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
