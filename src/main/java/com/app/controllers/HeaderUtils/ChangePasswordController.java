package com.app.controllers.HeaderUtils;

import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.HashPassword;
import com.app.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private PasswordField oldPasswordField2;
    @FXML
    private PasswordField newPasswordField2;
    @FXML
    private PasswordField confirmPasswordField2;

    @FXML
    private ImageView eyeIconOld;
    @FXML
    private ImageView eyeIconNew;
    @FXML
    private ImageView eyeIconConfirm;

    @FXML
    private Button saveButton;

    // Trạng thái hiện tại của password
    private boolean isPasswordOldVisible = false;
    private boolean isPasswordNewVisible = false;
    private boolean isConfirmPasswordVisible = false;

    Image show = new Image(getClass().getResourceAsStream("/images/eye-solid.png"));
    Image hide = new Image(getClass().getResourceAsStream("/images/eye-slash-solid.png"));

    public void initialize() {
        handleChangePassword();
        eyeIconOld.setImage(hide);
        eyeIconNew.setImage(hide);
        eyeIconConfirm.setImage(hide);
    }

    @FXML
    private void togglePasswordOld() {
        if (isPasswordOldVisible) {
            eyeIconOld.setImage(hide);
            oldPasswordField2.setText(oldPasswordField.getText());
            oldPasswordField2.setVisible(true);
            oldPasswordField.setVisible(false);
            isPasswordOldVisible = false;
        } else {
            eyeIconOld.setImage(show);
            oldPasswordField.setText(oldPasswordField2.getText());
            oldPasswordField.setVisible(true);
            oldPasswordField2.setVisible(false);
            isPasswordOldVisible = true;
        }
    }

    @FXML
    private void togglePasswordNew() {
        if (isPasswordNewVisible) {
            eyeIconNew.setImage(hide);
            newPasswordField2.setText(newPasswordField.getText());
            newPasswordField2.setVisible(true);
            newPasswordField.setVisible(false);
            isPasswordNewVisible = false;
        } else {
            eyeIconNew.setImage(show);
            newPasswordField.setText(newPasswordField2.getText());
            newPasswordField.setVisible(true);
            newPasswordField2.setVisible(false);
            isPasswordNewVisible = true;
        }
    }

    @FXML
    private void toggleConfirmPassword() {
        if (isConfirmPasswordVisible) {
            eyeIconConfirm.setImage(hide);
            confirmPasswordField2.setText(confirmPasswordField.getText());
            confirmPasswordField2.setVisible(true);
            confirmPasswordField.setVisible(false);
            isConfirmPasswordVisible = false;
        } else {
            eyeIconConfirm.setImage(show);
            confirmPasswordField.setText(confirmPasswordField2.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField2.setVisible(false);
            isConfirmPasswordVisible = true;
        }
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
