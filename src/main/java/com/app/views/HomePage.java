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
        scene.getStylesheets().add(SignUp.class.getResource("/styles/home-page.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT); // Cho phép bo góc
        
        // Thiết lập cửa sổ chính
        //        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        //
        //        double actualWidth = screenBounds.getWidth();
        //        double actualHeight = screenBounds.getHeight();
        //
        //        if (actualWidth < 1440 || actualHeight < 1024) {
        //            primaryStage.setWidth(actualWidth);
        //            primaryStage.setHeight(actualHeight);
        //        } else {
        //            primaryStage.setWidth(1440);
        //            primaryStage.setHeight(1024);
        //        }

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
