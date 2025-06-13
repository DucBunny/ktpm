package com.app.controllers;

import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditInfoController {
    private UserReloadCallback callback;

    @FXML
    private TextField emailField;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField roleField;

    @FXML
    private Button saveButton;

    public void setCallback(UserReloadCallback callback) {
        this.callback = callback;
    }

    private static String userEmail;

    public static void setUserToEdit(String email) {
        userEmail = email;
    }

    public void initialize() {
        loadInfoFromDatabase(userEmail);

        setupSaveButton();
    }

    public void loadInfoFromDatabase(String userEmail) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM users WHERE email = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                emailField.setText(rs.getString("email"));
                userNameField.setText(rs.getString("username"));
                roleField.setText(rs.getString("role").equals("admin") ? "Quản trị viên" : "Kế toán");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                CustomAlert.showErrorAlert("Không thể tải dữ liệu nguời dùng từ CSDL.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            if (userNameField.getText() == null || userNameField.getText().isEmpty()) {
                showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            String email = emailField.getText().trim();
            String userName = userNameField.getText().trim();

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Cập nhật người dùng
                    String sql = "UPDATE users SET username = ? WHERE email = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, userName);
                        stmt.setString(2, email);

                        if (stmt.executeUpdate() == 0) {
                            showErrorAlert("Không tìm thấy người dùng để cập nhật.");
                            connection.rollback();
                            return;
                        }
                    }

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
