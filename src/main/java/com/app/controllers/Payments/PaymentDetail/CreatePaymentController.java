package com.app.controllers.Payments.PaymentDetail;

import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

public class CreatePaymentController {
    private PaymentReloadCallback callback;

    @FXML
    private ComboBox<ComboBoxOption> periodBox;
    @FXML
    private ComboBox<ComboBoxOption> roomBox;
    @FXML
    private TextField amountField;
    @FXML
    private TextArea noteArea;

    @FXML
    private AnchorPane roomAnchorPane;
    @FXML
    private AnchorPane amountAnchorPane;

    @FXML
    private Button saveButton;

    public void setCallback(PaymentReloadCallback callback) {
        this.callback = callback;
    }

    private String preselectedPeriod;

    public void setPreselectedPeriod(String period) {
        this.preselectedPeriod = period;
    }

    private final ObservableList<ComboBoxOption> roomSuggestions = FXCollections.observableArrayList();
    private final ObservableList<ComboBoxOption> allRooms = FXCollections.observableArrayList();

    private final ObservableList<ComboBoxOption> periodSuggestions = FXCollections.observableArrayList();
    private final ObservableList<ComboBoxOption> allPeriods = FXCollections.observableArrayList();

    public void initialize() {
        roomAnchorPane.setVisible(false);
        roomAnchorPane.setManaged(false);
        amountAnchorPane.setVisible(false);
        amountAnchorPane.setManaged(false);

        initPeriodBox();

        setupPeriodBoxSearch();
        setupRoomBoxSearch();
        setupSaveButton();
    }

    // Tự động chọn đợt thu trong page Payments (khi đã có data)
    public void applyPreselectedPeriod() {
        if (preselectedPeriod != null) {
            for (ComboBoxOption option : allPeriods) {
                if (option.getValue().equals(preselectedPeriod)) {
                    periodBox.setValue(option);
                    periodBox.getEditor().setText(option.getLabel());

                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.setHeight(418.5);
                    roomAnchorPane.setVisible(true);
                    roomAnchorPane.setManaged(true);
                    loadRoomsForSelectedPeriod(preselectedPeriod);

                    periodBox.setMouseTransparent(true);
                    periodBox.setFocusTraversable(false);
                    periodBox.hide();
                    break;
                }
            }
        }
    }

    private void initPeriodBox() {
        periodBox.getItems().clear();
        allPeriods.clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT name FROM collection_periods ORDER BY name ASC";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String period = rs.getString("name");
                ComboBoxOption option = new ComboBoxOption(period, period);
                allPeriods.add(option);
                periodSuggestions.add(option);
            }
            periodBox.setItems(periodSuggestions);

            periodBox.setConverter(new StringConverter<ComboBoxOption>() {
                @Override
                public String toString(ComboBoxOption option) {
                    return option != null ? option.getLabel() : "";
                }

                @Override
                public ComboBoxOption fromString(String string) {
                    if (string == null || string.trim().isEmpty()) {
                        return null;
                    }

                    // Tìm mục khớp với văn bản nhập
                    String lowerInput = string.trim().toLowerCase();
                    for (ComboBoxOption period : allPeriods) {
                        if (period.getLabel().toLowerCase().equals(lowerInput)) {
                            return period;
                        }
                    }

                    return null; // Không cho phép String tự do
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải danh sách đợt thu từ CSDL.");
        }
    }

    private void loadRoomsForSelectedPeriod(String period) {
        roomBox.getItems().clear();
        allRooms.clear();
        roomSuggestions.clear();
        if (period == null || period.isEmpty())
            return;
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = """
                    SELECT DISTINCT r.floor, r.room_number
                    FROM rooms r
                    JOIN collection_items ci ON r.room_number = ci.room_number
                    JOIN collection_periods cp ON ci.collection_period_id = cp.id
                    WHERE cp.name = ?
                    ORDER BY r.floor ASC, r.room_number ASC;
                    """;
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, period);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String roomNumber = rs.getString("room_number");
                ComboBoxOption option = new ComboBoxOption("Phòng " + roomNumber, roomNumber);
                allRooms.add(option);
                roomSuggestions.add(option);
            }
            roomBox.setItems(roomSuggestions);

            roomBox.setConverter(new StringConverter<ComboBoxOption>() {
                @Override
                public String toString(ComboBoxOption option) {
                    return option != null ? option.getLabel() : "";
                }

                @Override
                public ComboBoxOption fromString(String string) {
                    if (string == null || string.trim().isEmpty()) {
                        return null;
                    }

                    // Tìm mục khớp với văn bản nhập
                    String lowerInput = string.trim().toLowerCase();
                    for (ComboBoxOption room : allRooms) {
                        if (room.getLabel().toLowerCase().equals(lowerInput)) {
                            return room;
                        }
                    }

                    return null; // Không cho phép String tự do
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải danh sách phòng từ CSDL.");
        }
    }

    private void setupPeriodBoxSearch() {
        periodBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                String inputText = newValue.trim();
                ObservableList<ComboBoxOption> filteredItems = FXCollections.observableArrayList();
                if (inputText.isEmpty()) {
                    filteredItems.addAll(allPeriods);
                } else {
                    String lowerInput = inputText.toLowerCase();
                    for (ComboBoxOption period : allPeriods) {
                        if (period.getLabel().toLowerCase().contains(lowerInput) ||
                                period.getValue().toLowerCase().contains(lowerInput)) {
                            filteredItems.add(period);
                        }
                    }
                }
                periodSuggestions.setAll(filteredItems);

                // Hiển thị dropdown nếu có văn bản và có mục khớp
                if (!inputText.isEmpty() && !filteredItems.isEmpty()) {
                    periodBox.show();
                } else {
                    periodBox.hide();
                }
            }
        });

        // Xử lý khi chọn mục (hiện chọn phòng)
        periodBox.setOnAction(e -> {
            ComboBoxOption selected = periodBox.getValue();
            Stage stage = (Stage) saveButton.getScene().getWindow();
            if (selected != null) {
                periodBox.setValue(selected);
                periodBox.getEditor().setText(selected.getLabel());

                // Khi chọn đợt thu, gọi hàm loadRoomsForSelectedPeriod
                loadRoomsForSelectedPeriod(selected.getValue());

                // Reset roomBox selection
                roomBox.setValue(null);
                roomBox.getEditor().setText("");

                // height (FXML) + title bar (mặc định cao 35.5px)
                stage.setHeight(418.5);
                roomAnchorPane.setVisible(true);
                roomAnchorPane.setManaged(true);
            } else {
                stage.setHeight(348.5);
                roomAnchorPane.setVisible(false);
                roomAnchorPane.setManaged(false);
            }
        });
    }

    private void setupRoomBoxSearch() {
        roomBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                String inputText = newValue.trim();
                ObservableList<ComboBoxOption> filteredItems = FXCollections.observableArrayList();
                if (inputText.isEmpty()) {
                    filteredItems.addAll(allRooms);
                } else {
                    String lowerInput = inputText.toLowerCase();
                    for (ComboBoxOption room : allRooms) {
                        if (room.getLabel().toLowerCase().contains(lowerInput) ||
                                room.getValue().toLowerCase().contains(lowerInput)) {
                            filteredItems.add(room);
                        }
                    }
                }
                roomSuggestions.setAll(filteredItems);

                // Hiển thị dropdown nếu có văn bản và có mục khớp
                if (!inputText.isEmpty() && !filteredItems.isEmpty()) {
                    roomBox.show();
                } else {
                    roomBox.hide();
                }
            }
        });

        // Xử lý khi chọn mục (hiện số tiền)
        roomBox.setOnAction(e -> {
            ComboBoxOption selected = roomBox.getValue();
            Stage stage = (Stage) saveButton.getScene().getWindow();
            if (selected != null) {
                roomBox.setValue(selected);
                roomBox.getEditor().setText(selected.getLabel());

                stage.setHeight(603.5);
                amountAnchorPane.setVisible(true);
                amountAnchorPane.setManaged(true);
            } else {
                stage.setHeight(418.5);
                amountAnchorPane.setVisible(false);
                amountAnchorPane.setManaged(false);
                amountField.clear();
            }
        });
    }

    private boolean areRequiredFieldsEmpty() {
        return Stream.of(
                amountField.getText()
        ).anyMatch(s -> s == null || s.trim().isEmpty()) ||
                Stream.of(
                        periodBox.getValue(),
                        roomBox.getValue()
                ).anyMatch(Objects::isNull);
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            if (areRequiredFieldsEmpty()) {
                showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            String period = periodBox.getValue().getValue();
            String roomNumber = roomBox.getValue().getValue();
            String noteInput = noteArea.getText().trim();
            StringBuilder note = new StringBuilder("Thanh toán phí " + period.toLowerCase());
            String amount = amountField.getText().trim();

            double paidAmount;
            try {
                paidAmount = Double.parseDouble(amount);
                if (paidAmount <= 0) {
                    showErrorAlert("Số tiền thanh toán phải lớn hơn 0.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showErrorAlert("Số tiền không hợp lệ. Vui lòng nhập số.");
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Lấy collection_period_id
                    int collectionPeriodId;
                    String getPeriodSql = "SELECT id FROM collection_periods WHERE name = ?";
                    try (PreparedStatement periodStmt = connection.prepareStatement(getPeriodSql)) {
                        periodStmt.setString(1, period);
                        ResultSet rs = periodStmt.executeQuery();
                        if (!rs.next()) {
                            showErrorAlert("Không tìm thấy đợt thu trong hệ thống.");
                            connection.rollback();
                            return;
                        }
                        collectionPeriodId = rs.getInt("id");
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

                    // Kiểm tra tổng nợ từ các đợt thu trước
                    double totalPreviousDebt = 0.0;
                    String checkPreviousDebtSql = """
                            SELECT COALESCE(SUM(p.debt_amount), 0) AS total_debt
                            FROM payments p
                            JOIN collection_periods cp ON p.collection_period_id = cp.id
                            WHERE p.room_number = ? AND cp.id != ?
                            """;
                    try (PreparedStatement debtStmt = connection.prepareStatement(checkPreviousDebtSql)) {
                        debtStmt.setString(1, roomNumber);
                        debtStmt.setInt(2, collectionPeriodId);
                        ResultSet rs = debtStmt.executeQuery();
                        if (rs.next()) {
                            totalPreviousDebt = rs.getDouble("total_debt");
                        }
                    }

                    // Cảnh báo nếu có nợ trước đó
                    if (totalPreviousDebt > 0) {
                        boolean proceed = CustomAlert.showConfirmAlert(
                                "Căn hộ còn nợ " + String.format("%.2f", totalPreviousDebt) + " VND từ các thanh toán trước. Bạn có muốn tiếp tục?",
                                "Cảnh báo nợ"
                        );
                        if (!proceed) {
                            connection.rollback();
                            return;
                        }
                    }

                    // Lấy và sử dụng excess_amount từ các thanh toán trước
                    double remainingTotal = totalAmount;
                    double totalUsedExcess = 0.0;
                    String getExcessSql = """
                            SELECT p.id, p.excess_amount, cp.name
                            FROM payments p
                            JOIN collection_periods cp ON p.collection_period_id = cp.id
                            WHERE p.room_number = ? AND p.excess_amount > 0
                            ORDER BY p.payment_date ASC;
                            """;
                    try (PreparedStatement excessStmt = connection.prepareStatement(getExcessSql)) {
                        excessStmt.setString(1, roomNumber);
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

                    // Tính debt_amount và excess_amount
                    double effectivePaidAmount = paidAmount + totalUsedExcess;
                    double debtAmount = 0.0;
                    double excessAmount = 0.0;
                    if (effectivePaidAmount < totalAmount) {
                        debtAmount = totalAmount - effectivePaidAmount;
                    } else if (effectivePaidAmount > totalAmount) {
                        excessAmount = effectivePaidAmount - totalAmount;
                    }

                    // Thêm thanh toán mới
                    String insertSql = """
                            INSERT INTO payments (room_number, collection_period_id, paid_amount, debt_amount, excess_amount, payment_date, note)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """;
                    try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                        stmt.setString(1, roomNumber);
                        stmt.setInt(2, collectionPeriodId);
                        stmt.setDouble(3, effectivePaidAmount);
                        stmt.setDouble(4, debtAmount);
                        stmt.setDouble(5, excessAmount);
                        stmt.setDate(6, Date.valueOf(LocalDate.now()));
                        stmt.setString(7, noteInput.isEmpty() ? note.toString() : note.append("\n").append(noteInput).toString());

                        stmt.executeUpdate();
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Thêm thanh toán thành công", true, 0.7);
                    if (callback != null) { // gọi callback
                        callback.onPaymentCrud();
                    }
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
