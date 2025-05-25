package com.app.controllers;

import com.app.utils.DatabaseConnection;
import com.app.utils.HashPassword;
import com.app.utils.StageManager;
import com.app.utils.SuccessPopup;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class CreateAccountController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button createButton;

    @FXML
    private void handleSignUp() {
        String email = emailField.getText().trim();
        String username = userNameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Xác nhận mật khẩu không hợp lệ!");
            return;
        }

        String hashedPassword = HashPassword.hash(password);

        // Xóa thông báo cũ nếu hợp lệ
        errorLabel.setText("");

        try (Connection connect = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = connect.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);

            if (stmt.executeUpdate() > 0) {
                SuccessPopup.showSuccessPopup(StageManager.getPrimaryStage(), "Đã thêm tài khoản thành công!");
                handleSave();
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            errorLabel.setText("Email đã tồn tại!");
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Lỗi kết nối Database!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleSave() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }
}
