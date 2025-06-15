package com.app.controllers.HeaderUtils;

import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.HashPassword;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
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
    private TextField textPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField confirmTextPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button createButton;

    @FXML
    private ImageView eyeIcon;

    @FXML
    private ImageView eyeIconConfirm;

    // Trạng thái hiện tại của password
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @FXML
    private void togglePassword() {
        if (isPasswordVisible) {
            passwordField.setText(textPasswordField.getText());
            passwordField.setVisible(true);
            textPasswordField.setVisible(false);
            isPasswordVisible = false;
        } else {
            textPasswordField.setText(passwordField.getText());
            textPasswordField.setVisible(true);
            passwordField.setVisible(false);
            isPasswordVisible = true;
        }
    }

    @FXML
    private void toggleConfirmPassword() {
        if (isConfirmPasswordVisible) {
            confirmPasswordField.setText(confirmTextPasswordField.getText());
            confirmPasswordField.setVisible(true);
            confirmTextPasswordField.setVisible(false);
            isConfirmPasswordVisible = false;
        } else {
            confirmTextPasswordField.setText(confirmPasswordField.getText());
            confirmTextPasswordField.setVisible(true);
            confirmPasswordField.setVisible(false);
            isConfirmPasswordVisible = true;
        }
    }

    @FXML
    private void handleSignUp() {
        String email = emailField.getText().trim();
        String username = userNameField.getText().trim();

        // Lấy mật khẩu từ trường hiện tại đang hiển thị
        String password = passwordField.isVisible() ? passwordField.getText().trim() : textPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.isVisible() ? confirmPasswordField.getText().trim() : confirmTextPasswordField.getText().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Xác nhận mật khẩu không hợp lệ!");
            return;
        }

        String hashedPassword = HashPassword.hash(password);
        errorLabel.setText("");

        try (Connection connect = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = connect.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);

            if (stmt.executeUpdate() > 0) {
                CustomAlert.showSuccessAlert("Tạo tài khoản thành công", true, 0.7);
                handleSave();
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            errorLabel.setText("Email đã tồn tại!");
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Lỗi kết nối CSDL!");
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
