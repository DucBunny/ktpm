package com.app.controllers;

import com.app.utils.DatabaseConnection;
import com.app.utils.HashPassword;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.TextField;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChangePasswordController {
    private static String userEmail;
    @FXML
    private TextField oldPass;
    @FXML
    private TextField newPass;
    @FXML
    private TextField reNewPass;

    public static void setUserEmail(String email) {
        userEmail = email;
    }

    public void initialize() {

    }

    public void changePassword() {
        String oldPasswordInput = oldPass.getText();
        String newPasswordInput = newPass.getText();
        String reNewPasswordInput = reNewPass.getText();

        if (oldPasswordInput.isEmpty() || newPasswordInput.isEmpty() || reNewPasswordInput.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        if (!newPasswordInput.equals(reNewPasswordInput)) {
            JOptionPane.showMessageDialog(null, "Mật khẩu mới không khớp.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Lấy hashed password từ database
            String checkQuery = "SELECT password FROM users WHERE email = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, userEmail);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String hashedPasswordInDB = rs.getString("password");

                // So sánh mật khẩu cũ người dùng nhập với hash trong DB
                if (!HashPassword.checkPassword(oldPasswordInput, hashedPasswordInDB)) {
                    JOptionPane.showMessageDialog(null, "Mật khẩu cũ không chính xác.");
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Không tìm thấy người dùng.");
                return;
            }

            // Băm mật khẩu mới trước khi lưu
            String newHashedPassword = HashPassword.hash(newPasswordInput);

            // Cập nhật mật khẩu mới vào DB
            String updateQuery = "UPDATE users SET password = ? WHERE email = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, newHashedPassword);
            updateStmt.setString(2, userEmail);
            int rowsUpdated = updateStmt.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Đổi mật khẩu thành công.");
            } else {
                JOptionPane.showMessageDialog(null, "Cập nhật mật khẩu thất bại.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi khi đổi mật khẩu.");
        }
    }

    public void saveChangePassword(ActionEvent event) {
        changePassword();
    }
}
