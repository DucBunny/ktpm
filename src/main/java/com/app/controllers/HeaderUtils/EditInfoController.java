package com.app.controllers.HeaderUtils;

import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditInfoController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField roleField;

    @FXML
    private Button saveButton;

    private UserReloadCallback callback;

    public void setCallback(UserReloadCallback callback) {
        this.callback = callback;
    }

    public void initialize() {
        emailField.setText(UserSession.getEmail());
        userNameField.setText(UserSession.getUsername());
        roleField.setText(UserSession.getRole().equals("admin") ? "Quản trị viên" : "Kế toán");

        setupSaveButton();
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            String userName = userNameField.getText().trim();

            if (userName.isEmpty()) {
                showErrorAlert("Vui lòng nhập tên người dùng.");
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Cập nhật người dùng
                    String sql = "UPDATE users SET username = ? WHERE email = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, userName);
                        stmt.setString(2, UserSession.getEmail());

                        if (stmt.executeUpdate() == 0) {
                            showErrorAlert("Không tìm thấy người dùng để cập nhật.");
                            connection.rollback();
                            return;
                        }
                    }

                    // Cập nhật UserSession
                    UserSession.setUserInfo(UserSession.getEmail(), UserSession.getRole(), userName);

                    connection.commit();
                    CustomAlert.showSuccessAlert("Cập nhật thông tin người dùng thành công", true, 0.7);

                    if (callback != null) { // gọi callback
                        callback.onUserCrud();
                    }
                    handleSave();
                } catch (SQLException ex) {
                    connection.rollback();
                    ex.printStackTrace();
                    showErrorAlert("Lỗi SQL: " + ex.getMessage());
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
                showErrorAlert("Lỗi: " + ex.getMessage());
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
