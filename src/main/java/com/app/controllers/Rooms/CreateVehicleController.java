package com.app.controllers.Rooms;

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

public class CreateVehicleController {
    private String roomNumber;
    private VehicleReloadCallback callback;

    @FXML
    private ComboBox<ComboBoxOption> typeBox;
    @FXML
    private TextField plateNumberField;
    @FXML
    private TextField brandField;
    @FXML
    private TextField colorField;
    @FXML
    private ComboBox<ComboBoxOption> dayBox;
    @FXML
    private ComboBox<ComboBoxOption> monthBox;
    @FXML
    private Spinner<Integer> yearSpinner;
    @FXML
    private TextArea noteField;

    @FXML
    private Button saveButton;

    public void setCallback(VehicleReloadCallback callback) {
        this.callback = callback;
    }

    public void initialize(String roomNumber) {
        this.roomNumber = roomNumber;

        initTypeBox();
        initDayBox();
        initMonthBox();
        initYearSpinner();

        setupSaveButton();
    }

    private void initTypeBox() {
        typeBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Xe máy", "motorbike"),
                new ComboBoxOption("Ô tô", "car")
        ));
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

    private boolean areRequiredFieldsEmpty() {
        return Stream.of(
                plateNumberField.getText(),
                brandField.getText(),
                colorField.getText()
        ).anyMatch(s -> s == null || s.trim().isEmpty()) ||
                Stream.of(
                        typeBox.getValue(),
                        dayBox.getValue(),
                        monthBox.getValue(),
                        yearSpinner.getValue()
                ).anyMatch(Objects::isNull);
    }

    private boolean isValidRegistrationDate(int day, int month, int year) {
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

            String type = typeBox.getValue().getValue();
            String plateNumber = plateNumberField.getText().trim();
            String brand = brandField.getText().trim();
            String color = colorField.getText().trim();
            String note = noteField.getText().trim();

            int day = Integer.parseInt(dayBox.getValue().getValue());
            int month = Integer.parseInt(monthBox.getValue().getValue());
            int year = yearSpinner.getValue();

            // Kiểm tra ngày đăng ký hợp lệ
            if (!isValidRegistrationDate(day, month, year)) {
                showErrorAlert("Ngày đăng ký không hợp lệ.");
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Kiểm tra biển số xe trùng lặp
                    String checkSql = "SELECT id FROM vehicles WHERE plate_number = ? AND is_active = 1";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                        checkStmt.setString(1, plateNumber);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            showErrorAlert("Biển số xe đã tồn tại trong hệ thống và đang được sử dụng.");
                            connection.rollback();
                            return;
                        }
                    }

                    // Thêm phương tiện
                    String sql = "INSERT INTO vehicles (room_number, plate_number, type, brand, color, registration_date, is_active, note) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, roomNumber);
                        stmt.setString(2, plateNumber);
                        stmt.setString(3, type);
                        stmt.setString(4, brand);
                        stmt.setString(5, color);
                        stmt.setDate(6, Date.valueOf(LocalDate.of(year, month, day)));
                        stmt.setInt(7, 1);
                        stmt.setString(8, note.isEmpty() ? null : note.trim());

                        stmt.executeUpdate();
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Thêm phương tiện thành công", true, 0.7);
                    if (callback != null) { // gọi callback
                        callback.onVehicleCrud();
                    }
                    handleSave();
                } catch (SQLException ex) {
                    connection.rollback();
                    if (ex.getSQLState().equals("23000") && ex.getMessage().contains("unique_plate_number")) {
                        showErrorAlert("Biển số xe đã tồn tại trong hệ thống và đang được sử dụng.");
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