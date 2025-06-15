package com.app.controllers.Payments.PaymentDetail;

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
import java.sql.*;
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
            amountField.setText(paymentToEdit.getPaidAmount());
            noteArea.setText(paymentToEdit.getNote() == null ? "" : paymentToEdit.getNote());

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
                amountField.getText()
        ).anyMatch(s -> s == null || s.trim().isEmpty()) || Stream.of(
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

            String noteInput = noteArea.getText().trim();
            StringBuilder note = new StringBuilder("Thanh toán phí " + paymentToEdit.getCollectionPeriod().toLowerCase());
            String amount = amountField.getText().trim();

            double paidAmount;
            try {
                paidAmount = Double.parseDouble(amount);
                if (paidAmount < 0) {
                    showErrorAlert("Số tiền thanh toán không được âm.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showErrorAlert("Số tiền không hợp lệ. Vui lòng nhập số.");
                return;
            }

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
                    // Lấy collection_period_id và start_date
                    int collectionPeriodId;
                    LocalDate periodStartDate;
                    String getPeriodSql = "SELECT id, start_date FROM collection_periods WHERE name = ?";
                    try (PreparedStatement periodStmt = connection.prepareStatement(getPeriodSql)) {
                        periodStmt.setString(1, paymentToEdit.getCollectionPeriod());
                        ResultSet rs = periodStmt.executeQuery();
                        if (!rs.next()) {
                            showErrorAlert("Không tìm thấy đợt thu trong hệ thống.");
                            connection.rollback();
                            return;
                        }
                        collectionPeriodId = rs.getInt("id");
                        periodStartDate = rs.getDate("start_date").toLocalDate();
                    }

                    String roomNumber = paymentToEdit.getRoomNumber().replace("Phòng ", "");

                    // Kiểm tra tổng nợ từ các đợt thu trước
                    double totalPreviousDebt = 0.0;
                    String checkPreviousDebtSql = """
                            SELECT COALESCE(SUM(p.debt_amount), 0) AS total_debt
                            FROM payments p
                            JOIN collection_periods cp ON p.collection_period_id = cp.id
                            WHERE p.room_number = ? AND cp.start_date < ?
                            """;
                    try (PreparedStatement debtStmt = connection.prepareStatement(checkPreviousDebtSql)) {
                        debtStmt.setString(1, roomNumber);
                        debtStmt.setDate(2, Date.valueOf(periodStartDate));
                        ResultSet rs = debtStmt.executeQuery();
                        if (rs.next()) {
                            totalPreviousDebt = rs.getDouble("total_debt");
                        }
                    }

                    // Cảnh báo nếu có nợ trước đó
                    if (totalPreviousDebt > 0) {
                        boolean proceed = CustomAlert.showConfirmAlert(
                                "Căn hộ còn nợ " + String.format("%.2f", totalPreviousDebt) + " VND từ các đợt thu trước. Bạn có muốn tiếp tục?",
                                "Cảnh báo nợ"
                        );
                        if (!proceed) {
                            connection.rollback();
                            return;
                        }
                    }

                    // Lấy total_amount
                    double totalAmount = 0.0;
                    String checkSql = """
                            SELECT COALESCE(SUM(ci.total_amount), 0) AS total_amount
                            FROM collection_items ci
                            WHERE ci.room_number = ? AND ci.collection_period_id = ?
                            GROUP BY ci.room_number;
                            """;
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                        checkStmt.setString(1, roomNumber);
                        checkStmt.setInt(2, collectionPeriodId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            totalAmount = rs.getDouble("total_amount");
                        }
                    }

                    // Khôi phục excess_amount của bản ghi hiện tại (nếu có)
                    double previousExcess = 0.0;
                    String getCurrentExcessSql = """
                            SELECT excess_amount
                            FROM payments
                            WHERE id = ?;
                            """;
                    try (PreparedStatement currentExcessStmt = connection.prepareStatement(getCurrentExcessSql)) {
                        currentExcessStmt.setInt(1, paymentToEdit.getId());
                        ResultSet rs = currentExcessStmt.executeQuery();
                        if (rs.next()) {
                            previousExcess = rs.getDouble("excess_amount");
                        }
                    }

                    // Lấy và sử dụng excess_amount từ các thanh toán khác
                    double remainingTotal = totalAmount;
                    double totalUsedExcess = previousExcess;
                    String getExcessSql = """
                            SELECT p.id, p.excess_amount, cp.name
                            FROM payments p
                            JOIN collection_periods cp ON p.collection_period_id = cp.id
                            WHERE p.room_number = ? AND p.excess_amount > 0 AND p.id != ?
                            ORDER BY p.payment_date ASC;
                            """;
                    try (PreparedStatement excessStmt = connection.prepareStatement(getExcessSql)) {
                        excessStmt.setString(1, roomNumber);
                        excessStmt.setInt(2, paymentToEdit.getId());
                        ResultSet rs = excessStmt.executeQuery();
                        while (rs.next() && remainingTotal > 0) {
                            int paymentId = rs.getInt("id");
                            double excess = rs.getDouble("excess_amount");
                            String oldPeriodName = rs.getString("name");
                            double usedExcess = Math.min(excess, remainingTotal);

                            // Cập nhật excess_amount
                            String updateExcessSql = """
                                    UPDATE payments
                                    SET excess_amount = excess_amount - ?
                                    WHERE id = ?;
                                    """;
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateExcessSql)) {
                                updateStmt.setDouble(1, usedExcess);
                                updateStmt.setInt(2, paymentId);
                                updateStmt.executeUpdate();
                            }

                            remainingTotal -= usedExcess;
                            totalUsedExcess += usedExcess;

                            // Thêm ghi chú
                            if (usedExcess > 0) {
                                note.append("\nSử dụng ").append(String.format("%.2f", usedExcess)).append(" VND tiền thừa từ đợt thu ").append(oldPeriodName);
                            }
                        }
                    }

                    // Xử lý nợ trước đó nếu có
                    double effectivePaidAmount = paidAmount + totalUsedExcess;
                    double remainingPaidAmount = effectivePaidAmount;
                    if (totalPreviousDebt > 0 && remainingPaidAmount > totalAmount) {
                        double debtPayment = Math.min(totalPreviousDebt, remainingPaidAmount - totalAmount);
                        String getDebtSql = """
                                SELECT p.id, p.debt_amount, cp.name
                                FROM payments p
                                JOIN collection_periods cp ON p.collection_period_id = cp.id
                                WHERE p.room_number = ? AND p.debt_amount > 0 AND cp.start_date < ?
                                ORDER BY cp.start_date ASC
                                LIMIT 1;
                                """;
                        try (PreparedStatement debtStmt = connection.prepareStatement(getDebtSql)) {
                            debtStmt.setString(1, roomNumber);
                            debtStmt.setDate(2, Date.valueOf(periodStartDate));
                            ResultSet rs = debtStmt.executeQuery();
                            if (rs.next()) {
                                int oldPaymentId = rs.getInt("id");
                                String oldPeriodName = rs.getString("name");

                                // Cập nhật debt_amount
                                String updateDebtSql = """
                                        UPDATE payments
                                        SET debt_amount = debt_amount - ?
                                        WHERE id = ?;
                                        """;
                                try (PreparedStatement updateStmt = connection.prepareStatement(updateDebtSql)) {
                                    updateStmt.setDouble(1, debtPayment);
                                    updateStmt.setInt(2, oldPaymentId);
                                    updateStmt.executeUpdate();
                                }

                                // Thêm bản ghi thanh toán nợ
                                String insertDebtPaymentSql = """
                                        INSERT INTO payments (room_number, collection_period_id, paid_amount, debt_amount, excess_amount, payment_date, note)
                                        VALUES (?, ?, ?, 0, 0, ?, ?)
                                        """;
                                try (PreparedStatement insertStmt = connection.prepareStatement(insertDebtPaymentSql)) {
                                    insertStmt.setString(1, roomNumber);
                                    insertStmt.setInt(2, collectionPeriodId);
                                    insertStmt.setDouble(3, debtPayment);
                                    insertStmt.setDate(4, Date.valueOf(selectedDate));
                                    insertStmt.setString(5, "Thanh toán phí " + paymentToEdit.getCollectionPeriod().toLowerCase() + "\n" +
                                            "Thanh toán nợ " + String.format("%.2f", debtPayment) + " VND từ đợt thu " + oldPeriodName);
                                    insertStmt.executeUpdate();
                                }

                                remainingPaidAmount -= debtPayment;
                            }
                        }
                    }

                    // Tính debt_amount và excess_amount
                    double debtAmount = 0.0;
                    double excessAmount = 0.0;
                    if (remainingPaidAmount < totalAmount) {
                        debtAmount = totalAmount - remainingPaidAmount;
                    } else if (remainingPaidAmount > totalAmount) {
                        excessAmount = remainingPaidAmount - totalAmount;
                    }

                    // Cập nhật thanh toán
                    String updateSql = """
                            UPDATE payments
                            SET paid_amount = ?, debt_amount = ?, excess_amount = ?, payment_date = ?, note = ?
                            WHERE id = ?
                            """;
                    try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                        stmt.setDouble(1, effectivePaidAmount);
                        stmt.setDouble(2, debtAmount);
                        stmt.setDouble(3, excessAmount);
                        stmt.setDate(4, Date.valueOf(selectedDate));
                        stmt.setString(5, noteInput.isEmpty() ? note.toString() : note.append("\n").append(noteInput).toString());
                        stmt.setInt(6, paymentToEdit.getId());

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
