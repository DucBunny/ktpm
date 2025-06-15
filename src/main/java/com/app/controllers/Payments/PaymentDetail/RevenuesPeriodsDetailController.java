package com.app.controllers.Payments.PaymentDetail;

import com.app.models.CollectionItems;
import com.app.models.Revenues;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RevenuesPeriodsDetailController {
    private String roomNumber;
    private String collectionPeriodName;
    private int collectionPeriodId;

    private PaymentReloadCallback callback;

    @FXML
    private TextField periodField;
    @FXML
    private TextField roomField;
    @FXML
    private ListView<String> revenueListView;
    @FXML
    private Button selectAllButton;
    @FXML
    private Button clearAllButton;
    @FXML
    private TableView<CollectionItems> setRevenuesTable;
    @FXML
    private TableColumn<CollectionItems, String> revenueName;
    @FXML
    private TableColumn<CollectionItems, Double> quantity;
    @FXML
    private TableColumn<CollectionItems, String> quantityUnit;
    @FXML
    private TableColumn<CollectionItems, String> category;

    @FXML
    private Button saveButton;

    public void setCallback(PaymentReloadCallback callback) {
        this.callback = callback;
    }

    private final List<Revenues> revenueItems = new ArrayList<>();
    private final ObservableList<CollectionItems> setRevenuesTableList = FXCollections.observableArrayList();

    public void setDetail(String roomNumber, String collectionPeriodName) {
        this.roomNumber = roomNumber.replace("Phòng ", "");
        this.collectionPeriodName = collectionPeriodName;
        periodField.setText(collectionPeriodName);
        roomField.setText(roomNumber);
        loadCollectionPeriodId();
        loadExistingCollectionItems(); // Tải các collection_items hiện có
    }

    public void initialize() {
        loadRevenueItems();
        setupSaveButton();

        setRevenuesTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Thiết lập lắng nghe khi có thay đổi kích thước hoặc dữ liệu
        setRevenuesTable.widthProperty().addListener((obs, oldVal, newVal) -> adjustColumnWidths());
        setRevenuesTableList.addListener((ListChangeListener<CollectionItems>) c -> adjustColumnWidths());

        revenueName.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityUnit.setCellValueFactory(new PropertyValueFactory<>("quantityUnit"));
        category.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Cho phép chỉnh sửa quantity
        quantity.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        quantity.setOnEditCommit(event -> {
            CollectionItems entry = event.getRowValue();
            double newQuantity = event.getNewValue();
            if (newQuantity >= 0) {
                entry.setQuantity(newQuantity);
                setRevenuesTable.refresh();
            } else {
                showErrorAlert("Số lượng phải lớn hơn hoặc bằng 0.");
            }
        });

        setRevenuesTable.setItems(setRevenuesTableList);

        selectAllButton.setOnAction(e -> {
            revenueListView.getSelectionModel().selectAll();
        });
        clearAllButton.setOnAction(e -> {
            revenueListView.getSelectionModel().clearSelection();
        });
    }

    private void adjustColumnWidths() {
        // Lấy số dòng dữ liệu (item count)
        int rowCount = setRevenuesTableList.size();
        // Lấy chiều cao 1 hàng
        double rowHeight = 20;
        // Trừ chiều cao header
        double headerHeight = 30;
        // Tổng chiều cao dữ liệu
        double totalRowsHeight = rowCount * rowHeight;
        // Chiều cao hiển thị thực tế
        double tableContentHeight = setRevenuesTable.getHeight() - headerHeight;

        // Nếu tổng chiều cao dữ liệu > chiều cao table -> có scroll bar
        double padding = (totalRowsHeight > tableContentHeight) ? 15 : 0;
        double tableWidth = setRevenuesTable.getWidth() - padding;

        revenueName.setPrefWidth(tableWidth * 0.4);
        quantity.setPrefWidth(tableWidth * 0.2);
        quantityUnit.setPrefWidth(tableWidth * 0.2);
        category.setPrefWidth(tableWidth * 0.2);
    }

    private void loadCollectionPeriodId() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT id FROM collection_periods WHERE name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, collectionPeriodName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    collectionPeriodId = rs.getInt("id");
                } else {
                    showErrorAlert("Không tìm thấy đợt thu trong hệ thống.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi khi tải thông tin đợt thu: " + e.getMessage());
        }
    }

    private void loadRevenueItems() {
        revenueItems.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, unit_price, quantity_unit, category FROM revenue_items WHERE status = 'active'";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                revenueItems.add(new Revenues(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("unit_price").equals("1.00") ? "" : rs.getString("unit_price"),
                        rs.getString("quantity_unit"),
                        rs.getString("category").equals("mandatory") ? "Bắt buộc" : "Tự nguyện"
                ));
            }
            revenueListView.setItems(FXCollections.observableArrayList(revenueItems.stream().map(Revenues::getName).toList()));
            revenueListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Lắng nghe thay đổi selection để cập nhật bảng tự động
            revenueListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) c -> updateTableView());
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải danh sách khoản thu từ CSDL.");
        }
    }

    private void loadExistingCollectionItems() {
        setRevenuesTableList.clear();
        revenueListView.getSelectionModel().clearSelection();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = """
                    SELECT ci.revenue_item_id, ci.quantity, ci.quantity_unit, ci.unit_price, ci.total_amount,
                           ri.name, ri.category
                    FROM collection_items ci
                    JOIN revenue_items ri ON ci.revenue_item_id = ri.id
                    WHERE ci.collection_period_id = ? AND ci.room_number = ?
                    """;
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, collectionPeriodId);
                stmt.setString(2, roomNumber);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Revenues revenue = new Revenues(
                            rs.getInt("revenue_item_id"),
                            rs.getString("name"),
                            rs.getString("unit_price"),
                            rs.getString("quantity_unit"),
                            rs.getString("category").equals("mandatory") ? "Bắt buộc" : "Tự nguyện"
                    );
                    CollectionItems collectionItem = new CollectionItems(revenue, roomNumber);
                    collectionItem.setQuantity(rs.getDouble("quantity"));
                    setRevenuesTableList.add(collectionItem);
                    revenueListView.getSelectionModel().select(revenue.getName());
                }
                setRevenuesTable.refresh();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi khi tải danh sách khoản thu hiện có: " + e.getMessage());
        }
    }

    private double getRoomArea(String roomNumber) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT area FROM rooms WHERE room_number = ?")) {
            stmt.setString(1, roomNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("area");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private int getResidentCount(String roomNumber) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT COUNT(*) FROM residents WHERE room_number = ? AND status = 'living'")) {
            stmt.setString(1, roomNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getVehicleCount(String roomNumber, String vehicleType) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT COUNT(*) FROM vehicles WHERE room_number = ? AND type = ? AND is_active = 1")) {
            stmt.setString(1, roomNumber);
            stmt.setString(2, vehicleType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateTableView() {
        ObservableList<CollectionItems> tempList = FXCollections.observableArrayList(setRevenuesTableList);

        setRevenuesTableList.clear();

        for (String selectedName : revenueListView.getSelectionModel().getSelectedItems()) {
            Revenues item = revenueItems.stream()
                    .filter(r -> r.getName().equals(selectedName))
                    .findFirst()
                    .orElse(null);

            if (item != null) {
                // Kiểm tra xem khoản thu đã có trong tempList chưa
                CollectionItems existingItem = tempList.stream()
                        .filter(ci -> ci.getName().equals(selectedName))
                        .findFirst()
                        .orElse(null);

                CollectionItems collectionItem = new CollectionItems(item, roomNumber);
                if (existingItem != null) {
                    // Giữ quantity đã chỉnh sửa
                    collectionItem.setQuantity(existingItem.getQuantity());
                } else {
                    // Gán quantity tự động cho các khoản thu cụ thể
                    switch (item.getQuantityUnit() == null ? "" : item.getQuantityUnit()) {
                        case "m2":
                            collectionItem.setQuantity(getRoomArea(roomNumber));
                            break;
                        case "totalResidents":
                            collectionItem.setQuantity(getResidentCount(roomNumber));
                            break;
                        case "motorbike":
                            collectionItem.setQuantity(getVehicleCount(roomNumber, "motorbike"));
                            break;
                        case "car":
                            collectionItem.setQuantity(getVehicleCount(roomNumber, "car"));
                            break;
                        default:
                            collectionItem.setQuantity(0.0); // Giá trị mặc định cho các khoản khác
                            break;
                    }
                }
                setRevenuesTableList.add(collectionItem);
            }
        }
        setRevenuesTable.refresh();
    }

    private boolean areRequiredFieldsEmpty() {
        return periodField.getText().isEmpty() ||
                roomNumber.isEmpty() ||
                revenueListView.getSelectionModel().getSelectedItems().isEmpty();
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            if (areRequiredFieldsEmpty()) {
                showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            // Kiểm tra quantity của tất cả các khoản thu
            for (CollectionItems item : setRevenuesTableList) {
                if (item.getQuantity() <= 0) {
                    showErrorAlert("Vui lòng nhập đầy đủ số lượng: \n" + item.getName());
                    return;
                }
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Xóa các collection_items cũ của phòng trong đợt thu này
                    String deleteSql = "DELETE FROM collection_items WHERE collection_period_id = ? AND room_number = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, collectionPeriodId);
                        deleteStmt.setString(2, roomNumber);
                        deleteStmt.executeUpdate();
                    }

                    // Thêm các collection_items mới
                    String sql = "INSERT INTO collection_items (collection_period_id, room_number, revenue_item_id, quantity, quantity_unit, unit_price, total_amount)" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        for (CollectionItems item : setRevenuesTableList) {
                            stmt.setInt(1, collectionPeriodId);
                            stmt.setString(2, item.getRoomNumber());
                            stmt.setInt(3, item.getRevenueId());
                            stmt.setDouble(4, item.getQuantity());
                            stmt.setString(5, item.getQuantityUnit());
                            stmt.setDouble(6, item.getUnitPrice());
                            stmt.setDouble(7, item.getTotalAmount());
                            stmt.addBatch(); // Thêm vào nhóm câu lệnh
                        }
                        stmt.executeBatch(); // Chạy nhóm câu lệnh SQL
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Thiết lập khoản thu thành công", true, 0.7);
                    if (callback != null) { // gọi callback
                        callback.onPaymentCrud();
                    }
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

    // Utils -------------------------------------------------------------------
    private void showErrorAlert(String message) {
        try {
            CustomAlert.showErrorAlert(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
