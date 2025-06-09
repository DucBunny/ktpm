package com.app.controllers.Payments;

import com.app.models.PaymentDetail;
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

public class EditPaymentController {
    @FXML
    private ComboBox<ComboBoxOption> fullNameBox;
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<ComboBoxOption> nameRevenueBox;
    @FXML
    private TextField noteField;
    @FXML
    private DatePicker paymentDate;
    @FXML
    private ComboBox<ComboBoxOption> roomBox;
    @FXML
    private Button saveButton;

    private static PaymentDetail paymentToEdit;

    public static void setPaymentToEdit(PaymentDetail payment) {
        paymentToEdit = payment;
    }

    @FXML
    public void initialize() {
        // Load tên người nộp
        fullNameBox.setItems(FXCollections.observableArrayList());
        loadFullNameResident();

        // Load tên khoản thu
        nameRevenueBox.setItems(FXCollections.observableArrayList());
        loadNameRevenue();

        // Load danh sách phòng
        roomBox.setItems(FXCollections.observableArrayList());
        loadRoomNumbers();

        // Set dữ liệu nếu có
        if (paymentToEdit != null) {
            amountField.setText(paymentToEdit.getAmount());
            noteField.setText(paymentToEdit.getNote());
            paymentDate.setValue(paymentToEdit.getPaymentDate());

            String dbResidentName = paymentToEdit.getResidentName().trim();
            for (ComboBoxOption option : fullNameBox.getItems()) {
                if (option.getValue().equalsIgnoreCase(dbResidentName)) {
                    fullNameBox.setValue(option);
                    break;
                }
            }

            String dbRevenueName = paymentToEdit.getRevenueItem().trim();
            for (ComboBoxOption option : nameRevenueBox.getItems()) {
                if (option.getValue().equalsIgnoreCase(dbRevenueName)) {
                    nameRevenueBox.setValue(option);
                    break;
                }
            }

            String dbRoom = paymentToEdit.getRoomNumber().trim();
            for (ComboBoxOption option : roomBox.getItems()) {
                if (option.getValue().equalsIgnoreCase(dbRoom)) {
                    roomBox.setValue(option);
                    break;
                }
            }
        }
    }

    private void loadFullNameResident() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT full_name FROM residents";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String residentName = rs.getString("full_name");
                fullNameBox.getItems().add(new ComboBoxOption(residentName, residentName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadNameRevenue() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT name FROM revenue_items";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String revenueName = rs.getString("name");
                nameRevenueBox.getItems().add(new ComboBoxOption(revenueName, revenueName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

            String fullNameResident = fullNameBox.getValue().getValue();
            String roomNumber = roomBox.getValue().getValue();
            String nameRevenue = nameRevenueBox.getValue().getValue();
            String amount = amountField.getText();
            String note = noteField.getText();
            String payDate = paymentDate.getValue().toString();

            // Truy vấn lấy resident_id từ full_name
            int residentId;
            String sqlResident = "SELECT id FROM residents WHERE full_name = ?";
            PreparedStatement stmtResident = conn.prepareStatement(sqlResident);
            stmtResident.setString(1, fullNameResident);
            ResultSet rsResident = stmtResident.executeQuery();
            if (rsResident.next()) {
                residentId = rsResident.getInt("id");
            } else {
                throw new SQLException("Không tìm thấy cư dân!");
            }

            // Truy vấn lấy revenue_item_id từ name
            int revenueItemId;
            String sqlRevenue = "SELECT id FROM revenue_items WHERE name = ?";
            PreparedStatement stmtRevenue = conn.prepareStatement(sqlRevenue);
            stmtRevenue.setString(1, nameRevenue);
            ResultSet rsRevenue = stmtRevenue.executeQuery();
            if (rsRevenue.next()) {
                revenueItemId = rsRevenue.getInt("id");
            } else {
                throw new SQLException("Không tìm thấy khoản thu!");
            }

            String updateSql = """
                        UPDATE payments
                        SET resident_id = ?, room_number = ?, revenue_item_id = ?, amount = ?, payment_date = ?, note = ?
                        WHERE id = ?
                    """;
            PreparedStatement stmtUpdate = conn.prepareStatement(updateSql);
            stmtUpdate.setInt(1, residentId);
            stmtUpdate.setString(2, roomNumber);
            stmtUpdate.setInt(3, revenueItemId);
            stmtUpdate.setBigDecimal(4, new java.math.BigDecimal(amount));
            stmtUpdate.setDate(5, Date.valueOf(payDate));
            stmtUpdate.setString(6, note);
            stmtUpdate.setInt(7, paymentToEdit.getId());

            stmtUpdate.executeUpdate();

            // Đóng cửa sổ
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
