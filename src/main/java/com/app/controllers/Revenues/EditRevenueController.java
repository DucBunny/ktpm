package com.app.controllers.Revenues;

import com.app.models.Revenues;
import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditRevenueController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField unitPriceField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<ComboBoxOption> statusBox;
    @FXML
    private ComboBox<ComboBoxOption> categoryBox;
    @FXML
    private Button saveButton;

    private static Revenues revenueToEdit;

    public static void setRevenueToEdit(Revenues revenue) {
        revenueToEdit = revenue;
    }

    @FXML
    public void initialize() {
        initCategoryBox();
        initStatusBox();

        // Hiển thị dữ liệu nếu có
        if (revenueToEdit != null) {
            nameField.setText(revenueToEdit.getName());
            unitPriceField.setText(revenueToEdit.getUnitPrice());
            descriptionArea.setText(revenueToEdit.getDescription());
            setComboBoxValue(categoryBox, revenueToEdit.getCategory(), true);
            setComboBoxValue(statusBox, revenueToEdit.getStatus(), true);
        }

        setupSaveButton();
    }

    private void initCategoryBox() {
        categoryBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Bắt buộc", "mandatory"),
                new ComboBoxOption("Tự nguyện", "voluntary")
        ));
    }

    private void initStatusBox() {
        statusBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Mở", "active"),
                new ComboBoxOption("Đóng", "inactive")
        ));
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
        String name = nameField.getText();
        ComboBoxOption category = categoryBox.getValue();

        if (name == null || name.trim().isEmpty() || category == null) {
            return true;
        }

        if ("mandatory".equals(category.getValue())) {
            String unitPrice = unitPriceField.getText();
            return unitPrice == null || unitPrice.trim().isEmpty();
        }

        return false;
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            if (areRequiredFieldsEmpty()) {
                showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            String category = categoryBox.getValue().getValue();
            String status = statusBox.getValue().getValue();
            String unitPrice = category.equals("mandatory") ? unitPriceField.getText().trim() : "1";

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Kiểm tra tên khoản thu trùng lặp
                    String checkSql = "SELECT id FROM revenue_items WHERE name = ? AND id != ?";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                        checkStmt.setString(1, name);
                        checkStmt.setInt(2, revenueToEdit.getId());
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            showErrorAlert("Tên khoản thu đã tồn tại trong hệ thống.");
                            connection.rollback();
                            return;
                        }
                    }

                    // Cập nhật khoản thu
                    String sql = "UPDATE revenue_items SET name = ?, unit_price = ?, description = ?, status = ?, category = ? WHERE id = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, name);
                        stmt.setDouble(2, Double.parseDouble(unitPrice));
                        stmt.setString(3, description);
                        stmt.setString(4, status);
                        stmt.setString(5, category);
                        stmt.setInt(6, revenueToEdit.getId());

                        if (stmt.executeUpdate() == 0) {
                            showErrorAlert("Không tìm thấy khoản thu để cập nhật.");
                            connection.rollback();
                            return;
                        }
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Cập nhật khoản thu thành công", true, 0.7);
                    handleSave();
                } catch (SQLException ex) {
                    connection.rollback();
                    if (ex.getSQLState().equals("23000") && ex.getMessage().contains("unique_name")) {
                        showErrorAlert("Tên khoản thu đã tồn tại trong hệ thống.");
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
