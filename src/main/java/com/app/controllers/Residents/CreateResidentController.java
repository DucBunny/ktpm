package com.app.controllers.Residents;

import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

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
    private TextField idCardNumberField;
    @FXML
    private ComboBox<ComboBoxOption> residenceStatusBox;
    @FXML
    private ComboBox<ComboBoxOption> relationshipBox;

    @FXML
    private Button saveButton;

    private final ObservableList<ComboBoxOption> roomSuggestions = FXCollections.observableArrayList();
    private final ObservableList<ComboBoxOption> allRooms = FXCollections.observableArrayList();

    public void initialize() {
        initGenderBox();
        initRoomBox();

        initDayBox();
        initMonthBox();
        initYearSpinner();
        initResidenceStatusBox();
        initRelationshipBox();

        setupRoomBoxSearch();
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
        allRooms.clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT floor, room_number FROM rooms ORDER BY floor ASC, room_number ASC ";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String roomNumber = rs.getString("room_number");
                ComboBoxOption option = new ComboBoxOption("Phòng " + roomNumber, roomNumber);
                allRooms.add(option);
                roomSuggestions.add(option);
            }
            roomBox.setItems(roomSuggestions);

            roomBox.setConverter(new StringConverter<ComboBoxOption>() {
                @Override
                public String toString(ComboBoxOption option) {
                    return option != null ? option.getLabel() : "";
                }

                @Override
                public ComboBoxOption fromString(String string) {
                    if (string == null || string.trim().isEmpty()) {
                        return null;
                    }

                    // Tìm mục khớp với văn bản nhập
                    String lowerInput = string.trim().toLowerCase();
                    for (ComboBoxOption room : allRooms) {
                        if (room.getLabel().toLowerCase().equals(lowerInput)) {
                            return room;
                        }
                    }

                    return null; // Không cho phép String tự do
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải danh sách phòng từ CSDL.");
        }
    }

    private void setupRoomBoxSearch() {
        roomBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                String inputText = newValue.trim();
                ObservableList<ComboBoxOption> filteredItems = FXCollections.observableArrayList();
                if (inputText.isEmpty()) {
                    filteredItems.addAll(allRooms);
                } else {
                    String lowerInput = inputText.toLowerCase();
                    for (ComboBoxOption room : allRooms) {
                        if (room.getLabel().toLowerCase().contains(lowerInput) ||
                                room.getValue().toLowerCase().contains(lowerInput)) {
                            filteredItems.add(room);
                        }
                    }
                }
                roomSuggestions.setAll(filteredItems);

                // Hiển thị dropdown nếu có văn bản và có mục khớp
                if (!inputText.isEmpty() && !filteredItems.isEmpty()) {
                    roomBox.show();
                } else {
                    roomBox.hide();
                }
            }
        });

        // Xử lý khi chọn mục
        roomBox.setOnAction(e -> {
            ComboBoxOption selected = roomBox.getValue();
            if (selected != null) {
                roomBox.setValue(selected);
                roomBox.getEditor().setText(selected.getLabel());
            }
        });
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
                // occupationField.getText(),
                // phoneField.getText(),
                idCardNumberField.getText(),
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
        return phone.isEmpty() || phone.matches("\\d{10,11}");
    }

    private boolean isValidIdCardNumber(String idCardNumber) {
        return idCardNumber != null && idCardNumber.matches("\\d{12}");
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
                showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
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
            String idCardNumber = idCardNumberField.getText().trim();
            String residenceStatus = residenceStatusBox.getValue().getValue();
            String relationship = relationshipBox.getValue().getValue();

            // Kiểm tra định dạng số điện thoại
            if (!isValidPhoneNumber(phone)) {
                showErrorAlert("Số điện thoại phải chứa 10-11 chữ số nếu được nhập.");
                return;
            }

            // Kiểm tra định dạng CCCD
            if (!isValidIdCardNumber(idCardNumber)) {
                showErrorAlert("Số CCCD phải chứa đúng 12 chữ số.");
                return;
            }

            // Kiểm tra ngày sinh hợp lệ
            if (!isValidDateOfBirth(day, month, year)) {
                showErrorAlert("Ngày sinh không hợp lệ.");
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Kiểm tra CCCD trùng lặp
                    String checkSql = "SELECT id FROM residents WHERE id_card_number = ?";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                        checkStmt.setString(1, idCardNumber);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            showErrorAlert("Số CCCD đã tồn tại trong hệ thống.");
                            connection.rollback();
                            return;
                        }
                    }

                    // Thêm cư dân
                    String sql = "INSERT INTO residents (full_name, date_of_birth, place_of_birth, ethnicity, occupation, hometown, id_card_number, residence_status, phone, gender, relationship_to_owner, room_number, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, fullName);
                        stmt.setDate(2, Date.valueOf(LocalDate.of(year, month, day)));
                        stmt.setString(3, placeOfBirth);
                        stmt.setString(4, ethnicity);
                        stmt.setString(5, occupation.isEmpty() ? null : occupation.trim());
                        stmt.setString(6, hometown);
                        stmt.setString(7, idCardNumber);
                        stmt.setString(8, residenceStatus);
                        stmt.setString(9, phone.isEmpty() ? null : phone.trim());
                        stmt.setString(10, gender);
                        stmt.setString(11, relationship);
                        stmt.setString(12, roomNumber);
                        stmt.setString(13, "living");

                        stmt.executeUpdate();
                    }

                    // Cập nhật trạng thái phòng nếu là chủ hộ
                    if (relationship.equals("owner")) {
                        String updateSql = "UPDATE rooms SET status = 'occupied' WHERE room_number = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                            updateStmt.setString(1, roomNumber);
                            updateStmt.executeUpdate();
                        }
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Thêm cư dân thành công", true, 0.7);
                    handleSave();
                } catch (SQLException ex) {
                    connection.rollback();
                    if (ex.getSQLState().equals("23000") && ex.getMessage().contains("unique_id_card_number")) {
                        showErrorAlert("Số CCCD đã tồn tại trong hệ thống.");
                    } else {
                        ex.printStackTrace();
                        showErrorAlert("Lỗi SQL: " + ex.getMessage());
                    }
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
