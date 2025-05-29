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

import java.sql.*;

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
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            String fullName = fullNameField.getText();
            String phone = phoneField.getText();
            String citizenId = citizenIdField.getText();
            String gender = genderBox.getValue().getValue();
            String roomNumber = roomBox.getValue().getValue();
            String dob = dateOfBirthField.getValue().toString();

            String query = String.format(
                    "UPDATE residents SET full_name='%s', date_of_birth='%s', gender='%s', phone='%s', citizen_id='%s', room_number='%s' WHERE id=%d",
                    fullName, dob, gender, phone, citizenId, roomNumber, residentToEdit.getId()
            );

            stmt.executeUpdate(query);

            // Đóng cửa sổ
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
