package com.app.controllers;

import com.app.utils.DatabaseConnection;
import com.app.utils.HashPassword;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Tạo tài khoản thành công!");
                clear();
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            errorLabel.setText("Email đã tồn tại!");
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Lỗi kết nối Database!");
        }
    }

    private void clear() {
        emailField.clear();
        userNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        errorLabel.setText("");
    }
}
