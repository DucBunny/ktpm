package com.app.controllers;

import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class CreateResidentController {
    @FXML
    private TextField fullNameField;
    @FXML
    private DatePicker dateOfBirthField;
    @FXML
    private ComboBox<ComboBoxOption> genderBox;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField citizenIdField;
    @FXML
    private ComboBox<ComboBoxOption> roomBox;
    @FXML
    private ComboBox<ComboBoxOption> relationshipBox;
    @FXML
    private Button saveButton;

    public void initialize() {
        // Giới tính
        genderBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Nam", "male"),
                new ComboBoxOption("Nữ", "female"),
                new ComboBoxOption("Khác", "other")
        ));

        // Vai trò
        relationshipBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Chủ hộ", "owner"),
                new ComboBoxOption("Vợ/Chồng", "spouse"),
                new ComboBoxOption("Cha/Mẹ", "parent"),
                new ComboBoxOption("Con cái", "child"),
                new ComboBoxOption("Khác", "other")
        ));

        loadRoomNumbers();
        setupSaveButton();
    }

    private void loadRoomNumbers() {
        roomBox.getItems().clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT room_number FROM rooms";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String roomNumber = rs.getString("room_number");
                roomBox.getItems().add(new ComboBoxOption("Phòng " + roomNumber, roomNumber));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            String fullName = fullNameField.getText();
            String phone = phoneField.getText();
            String citizenId = citizenIdField.getText();
            LocalDate dob = dateOfBirthField.getValue();
            ComboBoxOption genderOption = genderBox.getValue();
            ComboBoxOption roomOption = roomBox.getValue();
            ComboBoxOption relationshipOption = relationshipBox.getValue();

            if (fullName == null || fullName.isEmpty() ||
                    phone == null || phone.isEmpty() ||
                    citizenId == null || citizenId.isEmpty() ||
                    dob == null || genderOption == null || roomOption == null || relationshipOption == null) {
                showAlert("Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO residents (full_name, date_of_birth, gender, phone, citizen_id, room_number, relationship_to_owner) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, fullName);
                stmt.setDate(2, java.sql.Date.valueOf(dob));
                stmt.setString(3, genderOption.getValue());
                stmt.setString(4, phone);
                stmt.setString(5, citizenId);
                stmt.setString(6, roomOption.getValue());
                stmt.setString(7, relationshipOption.getValue());

                int rowsInserted = stmt.executeUpdate();

                if (rowsInserted > 0) {
                    CustomAlert.showSuccessAlert("Thêm cư dân thành công!", true, 1);
                    handleSave();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Lỗi SQL", "Không thể lưu dữ liệu.");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleSave() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
