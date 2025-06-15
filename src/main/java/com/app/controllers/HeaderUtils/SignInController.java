package com.app.controllers.HeaderUtils;

import com.app.controllers.Homepage.HomePageController;
import com.app.utils.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignInController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            //            errorLabel.setText("");
            showErrorAlert("Vui lòng nhập email và mật khẩu!");
            return;
        }

        try (Connection connect = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE email = ?";
            PreparedStatement stmt = connect.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (HashPassword.checkPassword(password, hashedPassword)) {
                    String role = rs.getString("role");
                    String username = rs.getString("username");
                    try {
                        UserSession.setUserInfo(email, role, username);
                        changeToHomePage(event, role);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //                    errorLabel.setText("Sai mật khẩu!");
                    showErrorAlert("Sai mật khẩu!");
                }
            } else {
                //                errorLabel.setText("Email không tồn tại!");
                showErrorAlert("Email không tồn tại!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            //            errorLabel.setText("Lỗi kết nối CSDL!");
            showErrorAlert("Lỗi kết nối CSDL!");
        }
    }

    public void changeToHomePage(ActionEvent event, String role) throws Exception {
        UserSession.setCurrentPage("home");
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Homepage/home-page.fxml"
                , "/styles/home-page.css", event, true);
        HomePageController homeController = loader.getController();
        homeController.initializeHeader();
        homeController.initialize(role);
    }

    // Utils -------------------------------------------------------------------
    private void showErrorAlert(String message) {
        try {
            CustomAlert.showErrorAlert(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
