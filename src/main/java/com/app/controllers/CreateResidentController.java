package com.app.controllers;

import com.app.utils.ComboBoxOption;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;

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
    private Button saveButton;

    @FXML
    public void initialize() {
        // Giới tính
        genderBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Nam", "male"),
                new ComboBoxOption("Nữ", "female"),
                new ComboBoxOption("Khác", "other")
        ));

        loadRoomNumbers();

    }

//    Load các số phòng hiện có
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

    public void handleCreateResident(ActionEvent event) {
        String fullName = fullNameField.getText();
        String phone = phoneField.getText();
        String citizenId = citizenIdField.getText();
        ComboBoxOption genderOption = genderBox.getValue();
        ComboBoxOption roomOption = roomBox.getValue();
        Date dateOfBirth = Date.valueOf(dateOfBirthField.getValue());

        // Kiểm tra dữ liệu bắt buộc
        if (fullName.isEmpty() || dateOfBirthField.getValue() == null ||
                genderOption == null || phone.isEmpty() ||
                citizenId.isEmpty() || roomOption == null) {
            System.out.println("Vui lòng điền đầy đủ thông tin.");
            return;
        }

        String gender = genderBox.getValue().getValue(); // "male", "female", "other"
        String roomNumber = roomBox.getValue().getValue();

        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "INSERT INTO residents (full_name, date_of_birth, gender, phone, citizen_id, room_number) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setDate(2, dateOfBirth);
            stmt.setString(3, gender);
            stmt.setString(4, phone);
            stmt.setString(5, citizenId);
            stmt.setString(6, roomNumber);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Cư dân đã được thêm thành công.");
            } else {
                System.out.println("Không thể thêm cư dân.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleSave(ActionEvent event) {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

}
