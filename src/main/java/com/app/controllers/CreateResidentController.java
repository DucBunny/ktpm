package com.app.controllers;

import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

public class CreateResidentController {
    // Left
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField placeOfBirthField;
    @FXML
    private TextField occupationField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<ComboBoxOption> genderBox;
    @FXML
    private ComboBox<ComboBoxOption> roomBox;

    // Right
    @FXML
    private ComboBox<ComboBoxOption> dayBox;
    @FXML
    private ComboBox<ComboBoxOption> monthBox;
    @FXML
    private Spinner<Integer> yearSpinner;
    @FXML
    private TextField hometownField;
    @FXML
    private TextField ethnicityField;
    @FXML
    private TextField citizenIdField;
    @FXML
    private ComboBox<ComboBoxOption> residenceStatusBox;
    @FXML
    private ComboBox<ComboBoxOption> relationshipBox;

    @FXML
    private Button saveButton;

    public void initialize() {
        initGenderBox();
        initRoomBox();

        initDayBox();
        initMonthBox();
        initYearSpinner();
        initResidenceStatusBox();
        initRelationshipBox();

        setupSaveButton();
    }

    private void initGenderBox() {
        genderBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Nam", "male"),
                new ComboBoxOption("Nữ", "female"),
                new ComboBoxOption("Khác", "other")
        ));
    }

    private void initRoomBox() {
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
            try {
                CustomAlert.showErrorAlert("Lỗi khi tải danh sách phòng.");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    private void initDayBox() {
        ObservableList<ComboBoxOption> days = FXCollections.observableArrayList();
        for (int i = 1; i <= 31; i++) {
            String value = String.format("%02d", i);
            days.add(new ComboBoxOption(String.valueOf(i), value));
        }
        dayBox.setItems(days);
    }

    private void initMonthBox() {
        ObservableList<ComboBoxOption> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            String value = String.format("%02d", i);
            months.add(new ComboBoxOption(String.valueOf(i), value));
        }
        monthBox.setItems(months);
    }

    private void initYearSpinner() {
        SpinnerValueFactory<Integer> yearFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, LocalDate.now().getYear(), LocalDate.now().getYear());
        yearSpinner.setValueFactory(yearFactory);
    }

    private void initResidenceStatusBox() {
        residenceStatusBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Thường trú", "permanent"),
                new ComboBoxOption("Tạm trú", "temporary")
        ));
    }

    private void initRelationshipBox() {
        relationshipBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Chủ hộ", "owner"),
                new ComboBoxOption("Vợ/Chồng", "spouse"),
                new ComboBoxOption("Cha/Mẹ", "parent"),
                new ComboBoxOption("Con cái", "child"),
                new ComboBoxOption("Khác", "other")
        ));
    }

    private boolean areRequiredFieldsEmpty() {
        return Stream.of(
                fullNameField.getText(),
                placeOfBirthField.getText(),
                occupationField.getText(),
                phoneField.getText(),
                citizenIdField.getText(),
                hometownField.getText(),
                ethnicityField.getText()
        ).anyMatch(s -> s == null || s.trim().isEmpty()) ||
                Stream.of(
                        genderBox.getValue(),
                        roomBox.getValue(),
                        dayBox.getValue(),
                        monthBox.getValue(),
                        yearSpinner.getValue(),
                        residenceStatusBox.getValue(),
                        relationshipBox.getValue()
                ).anyMatch(Objects::isNull);
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("\\d{10,11}");
    }

    private boolean isValidCitizenId(String citizenId) {
        return citizenId != null && citizenId.matches("\\d{12}");
    }

    private boolean isValidDateOfBirth(int day, int month, int year) {
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            if (areRequiredFieldsEmpty()) {
                try {
                    CustomAlert.showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            String fullName = fullNameField.getText().trim();
            String placeOfBirth = placeOfBirthField.getText().trim();
            String occupation = occupationField.getText().trim();
            String phone = phoneField.getText().trim();
            String gender = genderBox.getValue().getValue();
            String roomNumber = roomBox.getValue().getValue();

            int day = Integer.parseInt(dayBox.getValue().getValue());
            int month = Integer.parseInt(monthBox.getValue().getValue());
            int year = yearSpinner.getValue();
            String hometown = hometownField.getText().trim();
            String ethnicity = ethnicityField.getText().trim();
            String citizenId = citizenIdField.getText().trim();
            String residenceStatus = residenceStatusBox.getValue().getValue();
            String relationship = relationshipBox.getValue().getValue();

            // Kiểm tra định dạng số điện thoại
            if (!isValidPhoneNumber(phone)) {
                try {
                    CustomAlert.showErrorAlert("Số điện thoại phải chứa 10-11 chữ số.");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            // Kiểm tra định dạng CCCD
            if (!isValidCitizenId(citizenId)) {
                try {
                    CustomAlert.showErrorAlert("Số CCCD phải chứa đúng 12 chữ số.");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            // Kiểm tra ngày sinh hợp lệ
            if (!isValidDateOfBirth(day, month, year)) {
                try {
                    CustomAlert.showErrorAlert("Ngày sinh không hợp lệ.");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    String sql = "INSERT INTO residents (full_name, date_of_birth, place_of_birth, ethnicity, occupation, hometown, id_card_number, residence_status, phone, gender, relationship_to_owner, room_number, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, fullName);
                    stmt.setDate(2, Date.valueOf(LocalDate.of(year, month, day)));
                    stmt.setString(3, placeOfBirth);
                    stmt.setString(4, ethnicity);
                    stmt.setString(5, occupation);
                    stmt.setString(6, hometown);
                    stmt.setString(7, citizenId);
                    stmt.setString(8, residenceStatus);
                    stmt.setString(9, phone);
                    stmt.setString(10, gender);
                    stmt.setString(11, relationship);
                    stmt.setString(12, roomNumber);
                    stmt.setString(13, "living");

                    stmt.executeUpdate();

                    // Cập nhật trạng thái phòng nếu là chủ hộ
                    if (relationship.equals("owner")) {
                        String updateSql = "UPDATE rooms SET status = 'occupied' WHERE room_number = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                            updateStmt.setString(1, roomNumber);
                            updateStmt.executeUpdate();
                        }
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Thêm cư dân thành công!", true, 1);
                    handleSave();
                } catch (SQLException ex) {
                    connection.rollback();
                    ex.printStackTrace();
                    try {
                        CustomAlert.showErrorAlert("Lỗi SQL: Không thể lưu dữ liệu.");
                    } catch (IOException exc) {
                        throw new RuntimeException(exc);
                    }
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                try {
                    CustomAlert.showErrorAlert("Lỗi SQL, Không thể lưu dữ liệu.");
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void handleSave() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
