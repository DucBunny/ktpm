package com.app.controllers;

import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserInfoController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField roleField;

    private static String userEmail;

    public static void setUserInfo(String email) {
        userEmail = email;
    }

    public void initialize() {
        loadInfoFromDatabase(userEmail);
    }

    public void changeToEditInfo() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        EditInfoController.setUserToEdit(userEmail); // Hàm static để tạm giữ dữ liệu
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/edit-info.fxml", "/styles/sign-in-create-account.css", owner);

        EditInfoController controller = loader.getController();

        // Sau khi sửa, làm mới lại bảng dữ liệu:
        controller.setCallback(() -> {
            // Chỉ reload khi thực sự thêm mới thành công!
            loadInfoFromDatabase(userEmail);
        });
    }

    public void loadInfoFromDatabase(String userEmail) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM users WHERE email = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, userEmail.trim());
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
}
