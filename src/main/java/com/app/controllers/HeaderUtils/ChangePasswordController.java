package com.app.controllers.HeaderUtils;

import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.HashPassword;
import com.app.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChangePasswordController {
    @FXML
    private TextField oldPasswordField;
    @FXML
    private TextField newPasswordField;
    @FXML
    private TextField confirmPasswordField;

    @FXML
    private Button saveButton;

    public void initialize() {
        handleChangePassword();
    }

    public void handleChangePassword() {
        saveButton.setOnAction(e -> {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showErrorAlert("Mật khẩu mới không khớp.");
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                // Lấy hashed password từ database
                String checkQuery = "SELECT password FROM users WHERE email = ?";
                PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                checkStmt.setString(1, UserSession.getEmail());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String hashedPasswordInDB = rs.getString("password");

                    // So sánh mật khẩu cũ người dùng nhập với hash trong DB
                    if (!HashPassword.checkPassword(oldPassword, hashedPasswordInDB)) {
                        showErrorAlert("Mật khẩu cũ không chính xác.");
                        return;
                    }
                } else {
                    showErrorAlert("Không tìm thấy người dùng.");
                    return;
                }

                // Băm mật khẩu mới trước khi lưu
                String newHashedPassword = HashPassword.hash(newPassword);

                // Cập nhật mật khẩu mới vào DB
                String updateQuery = "UPDATE users SET password = ? WHERE email = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setString(1, newHashedPassword);
                updateStmt.setString(2, UserSession.getEmail());
                if (updateStmt.executeUpdate() > 0) {
                    CustomAlert.showSuccessAlert("Đổi mật khẩu thành công", true, 0.7);
                    handleSave();
                } else {
                    showErrorAlert("Cập nhật mật khẩu thất bại.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorAlert("Đã xảy ra lỗi khi đổi mật khẩu.");
            }
        });
    }

    private void handleSave() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
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
