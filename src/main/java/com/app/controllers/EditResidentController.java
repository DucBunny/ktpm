package com.app.controllers;

import com.app.models.Residents;
import com.app.utils.ComboBoxOption;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditResidentController {
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

    private static Residents residentToEdit;

    public static void setResidentToEdit(Residents resident) {
        residentToEdit = resident;
    }

    @FXML
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

        // Load danh sách phòng
        roomBox.setItems(FXCollections.observableArrayList());
        loadRoomNumbers();

        // Set dữ liệu nếu có
        if (residentToEdit != null) {
            fullNameField.setText(residentToEdit.getName());
            dateOfBirthField.setValue(residentToEdit.getDateOfBirth());
            phoneField.setText(residentToEdit.getPhone());
            citizenIdField.setText(residentToEdit.getCitizenId());

            String dbGender = residentToEdit.getGender().trim();
            for (ComboBoxOption option : genderBox.getItems()) {
                if (option.getLabel().equalsIgnoreCase(dbGender)) {
                    genderBox.setValue(option);
                    break;
                }
            }

            String dbRoom = residentToEdit.getRoomNumber().trim();
            for (ComboBoxOption option : roomBox.getItems()) {
                if (option.getValue().equalsIgnoreCase(dbRoom)) {
                    roomBox.setValue(option);
                    break;
                }
            }

            String dbRelationship = residentToEdit.getRelationshipToOwner().trim();
            for (ComboBoxOption option : relationshipBox.getItems()) {
                if (option.getLabel().equalsIgnoreCase(dbRelationship)) {
                    relationshipBox.setValue(option);
                    break;
                }
            }
        }
    }

    private void loadRoomNumbers() {
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

    @FXML
    private void handleSave() {
        try {
            Connection connection = DatabaseConnection.getConnection();

            String fullName = fullNameField.getText();
            String phone = phoneField.getText();
            String citizenId = citizenIdField.getText();
            String gender = genderBox.getValue().getValue();
            String roomNumber = roomBox.getValue().getValue();
            String dob = dateOfBirthField.getValue().toString();
            String relationshipToOwner = relationshipBox.getValue().getValue();

            String sql = "UPDATE residents SET full_name=?, date_of_birth=?, gender=?, phone=?, citizen_id=?, room_number=?, relationship_to_owner=? WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setString(2, dob);
            stmt.setString(3, gender);
            stmt.setString(4, phone);
            stmt.setString(5, citizenId);
            stmt.setString(6, roomNumber);
            stmt.setString(7, relationshipToOwner);
            stmt.setInt(8, residentToEdit.getId());

            stmt.executeUpdate();
            
            // Đóng cửa sổ
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
