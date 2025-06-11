package com.app.controllers.Revenues;

import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateRevenueController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField codeField;
    @FXML
    private TextField unitPriceField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<ComboBoxOption> categoryBox;

    @FXML
    private AnchorPane unitPriceAnchorPane;

    @FXML
    private Button saveButton;

    public void initialize() {
        initCategoryBox();

        // Ẩn unitPriceField mặc định
        unitPriceAnchorPane.setVisible(false);
        unitPriceAnchorPane.setManaged(false);

        categoryBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            if (newVal != null && newVal.getValue().equals("voluntary")) {
                // height (FXML) + title bar (mặc định cao 35.5px)
                stage.setHeight(533.5);
                unitPriceAnchorPane.setVisible(false);
                unitPriceAnchorPane.setManaged(false); // để layout co lại
                unitPriceField.clear();
            } else {
                stage.setHeight(603.5);
                unitPriceAnchorPane.setVisible(true);
                unitPriceAnchorPane.setManaged(true);
            }
        });

        setupSaveButton();
    }

    private void initCategoryBox() {
        categoryBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Bắt buộc", "mandatory"),
                new ComboBoxOption("Tự nguyện", "voluntary")
        ));
    }

    private boolean areRequiredFieldsEmpty() {
        String name = nameField.getText();
        String code = codeField.getText();
        ComboBoxOption category = categoryBox.getValue();

        if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty() || category == null) {
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
            String code = codeField.getText().trim();
            String description = descriptionArea.getText().trim();
            String category = categoryBox.getValue().getValue();
            String unitPrice = category.equals("mandatory") ? unitPriceField.getText().trim() : "1";

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Kiểm tra mã khoản thu trùng lặp
                    String checkSql = "SELECT id FROM revenue_items WHERE code = ?";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                        checkStmt.setString(1, code);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            showErrorAlert("Mã khoản thu đã tồn tại trong hệ thống.");
                            connection.rollback();
                            return;
                        }
                    }

                    // Thêm khoản thu
                    String sql = "INSERT INTO revenue_items (name, unit_price, description, category, status, code) " +
                            "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, name);
                        stmt.setDouble(2, Double.parseDouble(unitPrice));
                        stmt.setString(3, description);
                        stmt.setString(4, category);
                        stmt.setString(5, "active");
                        stmt.setString(6, code);

                        stmt.executeUpdate();
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Thêm khoản thu thành công", true, 0.7);
                    handleSave();
                } catch (SQLException ex) {
                    connection.rollback();
                    if (ex.getSQLState().equals("23000") && ex.getMessage().contains("unique_code")) {
                        showErrorAlert("Mã khoản thu đã tồn tại trong hệ thống.");
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
