package com.app.controllers.Payments;

import com.app.models.Payments;
import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

public class EditPaymentController {
    @FXML
    private TextField periodField;
    @FXML
    private TextField roomField;
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<ComboBoxOption> dayBox;
    @FXML
    private ComboBox<ComboBoxOption> monthBox;
    @FXML
    private Spinner<Integer> yearSpinner;
    @FXML
    private TextArea noteArea;

    @FXML
    private Button saveButton;

    private static Payments paymentToEdit;

    public static void setPaymentToEdit(Payments payment) {
        paymentToEdit = payment;
    }

    @FXML
    public void initialize() {
        initDayBox();
        initMonthBox();
        initYearSpinner();

        // Set dữ liệu nếu có
        if (paymentToEdit != null) {
            periodField.setText(paymentToEdit.getCollectionPeriod());
            roomField.setText(paymentToEdit.getRoomNumber());
            amountField.setText(paymentToEdit.getAmount());
            noteArea.setText(paymentToEdit.getNote());

            // Gán giá trị ngày
            if (paymentToEdit.getPaymentDate() != null) {
                int startDay = paymentToEdit.getPaymentDate().getDayOfMonth();
                int startMonth = paymentToEdit.getPaymentDate().getMonthValue();
                int startYear = paymentToEdit.getPaymentDate().getYear();
                setComboBoxValue(dayBox, String.format("%02d", startDay), false);
                setComboBoxValue(monthBox, String.format("%02d", startMonth), false);
                yearSpinner.getValueFactory().setValue(startYear);
            }
        }

        setupSaveButton();
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
        SpinnerValueFactory<Integer> startYearFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, LocalDate.now().getYear() + 1, LocalDate.now().getYear());
        yearSpinner.setValueFactory(startYearFactory);
    }

    private void setComboBoxValue(ComboBox<ComboBoxOption> comboBox, String value, boolean compareWithLabel) {
        if (value != null && comboBox.getItems() != null) {
            for (ComboBoxOption option : comboBox.getItems()) {
                String compareValue = compareWithLabel ? option.getLabel() : option.getValue();
                if (compareValue != null && compareValue.equalsIgnoreCase(value.trim())) {
                    comboBox.setValue(option);
                    break;
                }
            }
        }
    }

    private boolean areRequiredFieldsEmpty() {
        return Stream.of(
                dayBox.getValue(),
                monthBox.getValue(),
                yearSpinner.getValue()
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

            String note = noteArea.getText().trim();

            int day = Integer.parseInt(dayBox.getValue().getValue());
            int month = Integer.parseInt(monthBox.getValue().getValue());
            int year = yearSpinner.getValue();

            // Kiểm tra ngày hợp lệ
            if (!isValidDate(day, month, year)) {
                showErrorAlert("Ngày đóng không hợp lệ.");
                return;
            }

            LocalDate selectedDate = LocalDate.of(year, month, day);
            if (selectedDate.isAfter(LocalDate.now())) {
                showErrorAlert("Ngày đóng không được vượt quá ngày hôm nay.");
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Cập nhật thanh toán
                    String sql = "UPDATE payments SET payment_date = ?, note = ? WHERE id = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setDate(1, Date.valueOf(LocalDate.of(year, month, day)));
                        stmt.setString(2, note);
                        stmt.setInt(3, paymentToEdit.getId());

                        if (stmt.executeUpdate() == 0) {
                            showErrorAlert("Không tìm thấy thanh toán để cập nhật.");
                            connection.rollback();
                            return;
                        }
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Cập nhật thanh toán thành công", true, 0.7);
                    handleSave();
                } catch (SQLException ex) {
                    connection.rollback();
                    ex.printStackTrace();
                    showErrorAlert("Lỗi SQL: " + ex.getMessage());
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
