package com.app.controllers;

import com.app.models.Revenues;
import com.app.utils.ComboBoxOption;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Statement;

public class EditRevenueController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField valueField;
    @FXML
    private TextField descriptionField;
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
        // Khởi tạo lựa chọn cho category
        categoryBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Bắt buộc", "mandatory"),
                new ComboBoxOption("Tự nguyện", "voluntary")
        ));

        // Khởi tạo lựa chọn cho status
        statusBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Mở", "active"),
                new ComboBoxOption("Đóng", "inactive")
        ));

        // Hiển thị dữ liệu nếu có
        if (revenueToEdit != null) {
            nameField.setText(revenueToEdit.getName());
            valueField.setText(revenueToEdit.getValue());
            descriptionField.setText(revenueToEdit.getDescription());

            String dbCategory = revenueToEdit.getCategory().trim();
            for (ComboBoxOption option : categoryBox.getItems()) {
                if (option.getLabel().equalsIgnoreCase(dbCategory)) {
                    categoryBox.setValue(option);
                    break;
                }
            }

            String dbStatus = revenueToEdit.getStatus().trim();
            for (ComboBoxOption option : statusBox.getItems()) {
                if (option.getLabel().equalsIgnoreCase(dbStatus)) {
                    statusBox.setValue(option);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            String name = nameField.getText();
            String value = valueField.getText();
            String description = descriptionField.getText();
            String statusValue = statusBox.getValue().getValue();
            String categoryValue = categoryBox.getValue().getValue();

            String query = String.format(
                    "UPDATE revenue_items SET name='%s', unit_price='%s', description='%s', status='%s', category='%s' WHERE id=%d",
                    name, value, description, statusValue, categoryValue, revenueToEdit.getId()
            );

            stmt.executeUpdate(query);

            // Đóng cửa sổ
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
