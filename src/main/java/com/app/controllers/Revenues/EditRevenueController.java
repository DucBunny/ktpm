package com.app.controllers.Revenues;

import com.app.models.Revenues;
import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.application.Platform;
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

public class EditRevenueController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField codeField;
    @FXML
    private TextField unitPriceField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<ComboBoxOption> statusBox;
    @FXML
    private ComboBox<ComboBoxOption> categoryBox;

    @FXML
    private AnchorPane unitPriceAnchorPane;

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

        categoryBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && saveButton.getScene() != null) {
                Stage stage = (Stage) saveButton.getScene().getWindow();
                boolean isVoluntary = "voluntary".equals(newVal.getValue());
                unitPriceAnchorPane.setVisible(!isVoluntary);
                unitPriceAnchorPane.setManaged(!isVoluntary);
                if (isVoluntary) {
                    unitPriceField.clear();
                    stage.setHeight(673.5);
                } else {
                    stage.setHeight(743.5);
                }
            }
        });

        // Hiển thị dữ liệu nếu có
        if (revenueToEdit != null) {
            nameField.setText(revenueToEdit.getName());
            codeField.setText(revenueToEdit.getCode());
            descriptionArea.setText(revenueToEdit.getDescription() != null ? revenueToEdit.getDescription() : "");
            setComboBoxValue(categoryBox, revenueToEdit.getCategory(), true);
            setComboBoxValue(statusBox, revenueToEdit.getStatus(), true);

            // Gán giá trị unitPriceField
            String unitPrice = revenueToEdit.getUnitPrice();
            unitPriceField.setText((unitPrice != null && !unitPrice.trim().isEmpty()) ? unitPrice : "");

            // Gọi lại UI update cho category hiện tại
            Platform.runLater(() -> {
                ComboBoxOption cat = categoryBox.getValue();
                Stage stage = (Stage) saveButton.getScene().getWindow();
                boolean isVoluntary = cat != null && "voluntary".equals(cat.getValue());
                unitPriceAnchorPane.setVisible(!isVoluntary);
                unitPriceAnchorPane.setManaged(!isVoluntary);
                if (isVoluntary) {
                    unitPriceField.clear();
                    stage.setHeight(673.5);
                } else {
                    stage.setHeight(743.5);
                }
            });
        }

        // Đặt chiều cao Stage nếu chưa có dữ liệu
        Platform.runLater(() -> {
            if (saveButton.getScene() != null && revenueToEdit == null) {
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.setHeight(673.5);
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
        String code = codeField.getText();
        ComboBoxOption category = categoryBox.getValue();
        ComboBoxOption status = statusBox.getValue();

        if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty() || category == null || status == null) {
            return true;
        }

        if ("mandatory".equals(category.getValue())) {
            String unitPrice = unitPriceField.getText();
            return unitPrice == null || unitPrice.trim().isEmpty();
        }

        return false;
    }

    private boolean isValidUnitPrice(String unitPrice) {
        if (unitPrice == null || unitPrice.trim().isEmpty()) {
            return false;
        }
        try {
            double price = Double.parseDouble(unitPrice);
            return price > 0;
        } catch (NumberFormatException e) {
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
            String description = descriptionArea.getText().trim();
            String category = categoryBox.getValue().getValue();
            String status = statusBox.getValue().getValue();
            String unitPrice = category.equals("mandatory") ? unitPriceField.getText().trim() : "1";

            // Kiểm tra định dạng unitPrice
            if (category.equals("mandatory") && !isValidUnitPrice(unitPrice)) {
                showErrorAlert("Đơn giá không hợp lệ. Vui lòng nhập số lớn hơn 0.");
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Kiểm tra tên khoản thu trùng lặp
                    String checkSql = "SELECT id FROM revenue_items WHERE code = ? AND id != ?";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                        checkStmt.setString(1, code);
                        checkStmt.setInt(2, revenueToEdit.getId());
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            showErrorAlert("Mã khoản thu đã tồn tại trong hệ thống.");
                            connection.rollback();
                            return;
                        }
                    }

                    // Cập nhật khoản thu
                    String sql = "UPDATE revenue_items SET name = ?, unit_price = ?, description = ?, status = ?, category = ?, code = ? WHERE id = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, name);
                        stmt.setDouble(2, Double.parseDouble(unitPrice));
                        stmt.setString(3, description);
                        stmt.setString(4, status);
                        stmt.setString(5, category);
                        stmt.setString(6, code);
                        stmt.setInt(7, revenueToEdit.getId());

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
