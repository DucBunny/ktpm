package com.app.controllers;

import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class CreateRevenueController {
    @FXML
    private TextField nameRevenueField;
    @FXML
    private TextField descriptionField;
    @FXML
    private ComboBox<ComboBoxOption> categoryField;
    @FXML
    private TextField unitPriceField;
    @FXML
    private Button createButton;

    public void initialize() {
        categoryField.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Bắt buộc", "mandatory"),
                new ComboBoxOption("Tự nguyện", "voluntary")
        ));
        categoryField.setValue(categoryField.getItems().getFirst()); // chọn mặc định
    }

    @FXML
    private void handleCreateRevenues() {
        String nameRevenue = nameRevenueField.getText().trim();
        String description = descriptionField.getText().trim();
        String unitPriceString = unitPriceField.getText().trim();
        String category = categoryField.getValue().getValue();

        if (nameRevenue.isEmpty()) {
            nameRevenueField.setStyle("-fx-prompt-text-fill: red; -fx-border-color: red");
            nameRevenueField.clear();
            nameRevenueField.setPromptText("Vui lòng nhập tên khoản thu!");
            return;
        }

        long unitPrice;
        if (unitPriceString.isEmpty()) {
            if (category.equals("mandatory")) {
                unitPriceField.setStyle("-fx-prompt-text-fill: red; -fx-border-color: red");
                unitPriceField.setPromptText("Bắt buộc phải nhập!");
                return;
            }

            unitPrice = 1;
        } else {
            try {
                unitPrice = Long.parseLong(unitPriceString);
                if (unitPrice < 0) {
                    unitPriceField.setStyle("-fx-prompt-text-fill: red; -fx-border-color: red");
                    unitPriceField.clear();
                    unitPriceField.setPromptText("Vui lòng nhập số dương!");
                    return;
                }
            } catch (NumberFormatException e) {
                unitPriceField.setStyle("-fx-prompt-text-fill: red; -fx-border-color: red");
                unitPriceField.clear();
                unitPriceField.setPromptText("Vui lòng chỉ nhập số!");
                return;
            }
        }

        try (Connection connect = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO revenue_items (name, description, category, unit_price) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connect.prepareStatement(sql);
            stmt.setString(1, nameRevenue);
            stmt.setString(2, description);
            stmt.setString(3, category);
            stmt.setLong(4, unitPrice);

            if (stmt.executeUpdate() > 0) {
                CustomAlert.showSuccessAlert("Thêm khoản thu thành công", true, 0.7);
                handleSave();
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            nameRevenueField.setStyle("-fx-prompt-text-fill: red; -fx-border-color: red");
            nameRevenueField.clear();
            nameRevenueField.setPromptText("Tên khoản thu đã tồn tại!");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleSave() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }
}
