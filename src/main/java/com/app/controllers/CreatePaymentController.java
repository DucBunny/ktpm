package com.app.controllers;

import com.app.models.Residents;
import com.app.models.Revenues;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class CreatePaymentController {
    @FXML
    private Button loadRevenueButton;
    @FXML
    private Button loadResidentButton;
    @FXML
    private ComboBox<Revenues> revenueNameComboBox;
    @FXML
    private ComboBox<Residents> residentNameComboBox;
    @FXML
    private DatePicker paymentDatePicker;
    @FXML
    private TextField amountField;
    @FXML
    private TextField noteField;
    @FXML
    private Button saveButton;

    public void initialize() {
        loadRevenueName();
        loadResidentName();
        setupSaveButton();

        loadRevenueButton.setOnAction(e -> {
            revenueNameComboBox.show();
        });

        loadResidentButton.setOnAction(e -> {
            residentNameComboBox.show();
        });
        paymentDatePicker.setValue(LocalDate.now()); // mặc định là hôm nay
    }

    private void loadRevenueName() {
        revenueNameComboBox.getItems().clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT id, name FROM revenue_items WHERE status = 'active'";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                revenueNameComboBox.getItems().add(
                        new Revenues(rs.getInt("id"), rs.getString("name"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadResidentName() {
        residentNameComboBox.getItems().clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT id, full_name, room_number FROM residents";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                residentNameComboBox.getItems().add(
                        new Residents(rs.getInt("id"), rs.getString("full_name"), rs.getString("room_number"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            Residents selectedResident = residentNameComboBox.getValue();
            Revenues selectedRevenue = revenueNameComboBox.getValue();
            String note = noteField.getText().trim();
            String amountStr = amountField.getText().trim();
            LocalDate paymentDate = paymentDatePicker.getValue();

            if (selectedResident == null || selectedRevenue == null || amountStr.isEmpty() || paymentDate == null) {
                try {
                    CustomAlert.showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            try {
                BigDecimal amount = new BigDecimal(amountStr);

                int residentId = selectedResident.getId();
                String roomNumber = selectedResident.getRoomNumber();
                int revenueItemId = selectedRevenue.getId();

                insertPayment(residentId, roomNumber, revenueItemId, amount, paymentDate, note);
                CustomAlert.showSuccessAlert("Lưu thanh toán thành công!", true, 1);

                handleSave();
            } catch (NumberFormatException ex) {
                try {
                    CustomAlert.showErrorAlert("Số tiền không hợp lệ.");
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
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

    private void insertPayment(int residentId, String roomNumber, int revenueItemId,
                               BigDecimal amount, LocalDate paymentDate, String note) throws SQLException {
        String sql = "INSERT INTO payments (resident_id, room_number, revenue_item_id, amount, payment_date, note) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, residentId);
            stmt.setString(2, roomNumber);
            stmt.setInt(3, revenueItemId);
            stmt.setBigDecimal(4, amount);
            stmt.setDate(5, Date.valueOf(paymentDate));
            stmt.setString(6, note.isEmpty() ? null : note);

            stmt.executeUpdate();
        }
    }

    private void handleSave() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
