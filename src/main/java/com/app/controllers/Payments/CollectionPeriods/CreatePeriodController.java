package com.app.controllers.Payments.CollectionPeriods;

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

public class CreatePeriodController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField codeField;
    @FXML
    private ComboBox<ComboBoxOption> startDayBox;
    @FXML
    private ComboBox<ComboBoxOption> startMonthBox;
    @FXML
    private Spinner<Integer> startYearSpinner;
    @FXML
    private ComboBox<ComboBoxOption> endDayBox;
    @FXML
    private ComboBox<ComboBoxOption> endMonthBox;
    @FXML
    private Spinner<Integer> endYearSpinner;
    @FXML
    private ComboBox<ComboBoxOption> typeBox;

    @FXML
    private Button saveButton;

    public void initialize() {
        initTypeBox();

        initDayBox();
        initMonthBox();
        initYearSpinner();

        setupSaveButton();
    }

    private void initTypeBox() {
        typeBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Theo tháng", "monthly"),
                new ComboBoxOption("Theo quý", "quarterly"),
                new ComboBoxOption("Theo năm", "yearly")
        ));
    }

    private void initDayBox() {
        ObservableList<ComboBoxOption> days = FXCollections.observableArrayList();
        for (int i = 1; i <= 31; i++) {
            String value = String.format("%02d", i);
            days.add(new ComboBoxOption(String.valueOf(i), value));
        }
        startDayBox.setItems(days);
        endDayBox.setItems(days);
    }

    private void initMonthBox() {
        ObservableList<ComboBoxOption> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            String value = String.format("%02d", i);
            months.add(new ComboBoxOption(String.valueOf(i), value));
        }
        startMonthBox.setItems(months);
        endMonthBox.setItems(months);
    }

    private void initYearSpinner() {
        SpinnerValueFactory<Integer> yearFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, LocalDate.now().getYear(), LocalDate.now().getYear());
        startYearSpinner.setValueFactory(yearFactory);
        endYearSpinner.setValueFactory(yearFactory);
    }

    private boolean areRequiredFieldsEmpty() {
        return Stream.of(
                nameField.getText(),
                codeField.getText()
        ).anyMatch(s -> s == null || s.trim().isEmpty()) ||
                Stream.of(
                        typeBox.getValue(),
                        startDayBox.getValue(),
                        startMonthBox.getValue(),
                        startYearSpinner.getValue(),
                        endDayBox.getValue(),
                        endMonthBox.getValue(),
                        endYearSpinner.getValue()
                ).anyMatch(Objects::isNull);
    }

    private boolean isValidDate(int day, int month, int year) {
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

            String name = nameField.getText().trim();
            String code = codeField.getText().trim();
            String type = typeBox.getValue().getValue();

            int startDay = Integer.parseInt(startDayBox.getValue().getValue());
            int startMonth = Integer.parseInt(startMonthBox.getValue().getValue());
            int startYear = startYearSpinner.getValue();
            int endDay = Integer.parseInt(endDayBox.getValue().getValue());
            int endMonth = Integer.parseInt(endMonthBox.getValue().getValue());
            int endYear = endYearSpinner.getValue();

            // Kiểm tra ngày hợp lệ
            if (!isValidDate(startDay, startMonth, startYear)) {
                showErrorAlert("Ngày bắt đầu không hợp lệ.");
                return;
            }

            if (!isValidDate(endDay, endMonth, endYear)) {
                showErrorAlert("Ngày kết thúc không hợp lệ.");
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Kiểm tra mã đợt thu trùng lặp
                    String checkSql = "SELECT id FROM collection_periods WHERE code = ?";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                        checkStmt.setString(1, code);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            showErrorAlert("Mã đợt thu đã tồn tại trong hệ thống.");
                            connection.rollback();
                            return;
                        }
                    }

                    // Thêm đợt thu
                    String sql = "INSERT INTO collection_periods (name, start_date, end_date, type, code) " +
                            "VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, name);
                        stmt.setDate(2, Date.valueOf(LocalDate.of(startYear, startMonth, startDay)));
                        stmt.setDate(3, Date.valueOf(LocalDate.of(endYear, endMonth, endDay)));
                        stmt.setString(4, type);
                        stmt.setString(5, code);

                        stmt.executeUpdate();
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Thêm đợt thu thành công", true, 0.7);
                    handleSave();
                } catch (SQLException ex) {
                    connection.rollback();
                    if (ex.getSQLState().equals("23000") && ex.getMessage().contains("unique_code")) {
                        showErrorAlert("Mã đợt thu đã tồn tại trong hệ thống.");
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
