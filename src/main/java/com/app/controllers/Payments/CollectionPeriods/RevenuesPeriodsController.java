package com.app.controllers.Payments.CollectionPeriods;

import com.app.models.CollectionItems;
import com.app.models.Revenues;
import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RevenuesPeriodsController {
    private int collectionPeriodId;
    private String collectionPeriodName;

    @FXML
    private TextField periodField;
    @FXML
    private ComboBox<ComboBoxOption> roomBox;
    @FXML
    private CheckBox applyAllRoomsCheckBox;
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
    private TableColumn<CollectionItems, String> roomNumber;

    @FXML
    private Button saveButton;

    private final List<Revenues> revenueItems = new ArrayList<>();
    private final ObservableList<ComboBoxOption> roomSuggestions = FXCollections.observableArrayList();
    private final ObservableList<ComboBoxOption> allRooms = FXCollections.observableArrayList();
    private final ObservableList<CollectionItems> setRevenuesTableList = FXCollections.observableArrayList();

    public void setCollectionPeriod(int collectionPeriodId, String collectionPeriodName) {
        this.collectionPeriodId = collectionPeriodId;
        this.collectionPeriodName = collectionPeriodName;
    }

    public void initialize() {
        periodField.setText(collectionPeriodName);
        initRoomBox();
        loadRevenueItems();

        setupRoomBoxSearch();
        setupSaveButton();

        // Vô hiệu hóa chọn căn hộ khi áp dụng tất cả
        applyAllRoomsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            roomBox.setDisable(newVal);
            if (newVal)
                roomBox.getSelectionModel().clearSelection();
            updateTableView(); // Cập nhật bảng khi thay đổi trạng thái checkbox
        });

        setRevenuesTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Thiết lập lắng nghe khi có thay đổi kích thước hoặc dữ liệu
        setRevenuesTable.widthProperty().addListener((obs, oldVal, newVal) -> adjustColumnWidths());
        setRevenuesTableList.addListener((ListChangeListener<CollectionItems>) c -> adjustColumnWidths());

        revenueName.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityUnit.setCellValueFactory(new PropertyValueFactory<>("quantityUnit"));
        category.setCellValueFactory(new PropertyValueFactory<>("category"));
        roomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

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

        revenueName.setPrefWidth(tableWidth * 0.3);
        quantity.setPrefWidth(tableWidth * 0.2);
        quantityUnit.setPrefWidth(tableWidth * 0.2);
        category.setPrefWidth(tableWidth * 0.2);
        roomNumber.setPrefWidth(tableWidth * 0.1);
    }

    private void initRoomBox() {
        roomBox.getItems().clear();
        allRooms.clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT floor, room_number FROM rooms ORDER BY floor ASC, room_number ASC ";
            PreparedStatement stmt = connection.prepareStatement(sql);
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

        // Xử lý khi chọn mục
        roomBox.setOnAction(e -> {
            ComboBoxOption selected = roomBox.getValue();
            if (selected != null) {
                roomBox.setValue(selected);
                roomBox.getEditor().setText(selected.getLabel());
            }
            updateTableView(); // Cập nhật bảng khi chọn phòng
        });
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
        setRevenuesTableList.clear();

        List<String> selectedRooms;
        if (applyAllRoomsCheckBox.isSelected()) {
            selectedRooms = allRooms.stream().map(ComboBoxOption::getValue).toList();
        } else {
            selectedRooms = roomBox.getValue() != null ? List.of(roomBox.getValue().getValue()) : List.of();
        }

        for (String roomNumber : selectedRooms) {
            for (String selectedName : revenueListView.getSelectionModel().getSelectedItems()) {
                Revenues item = revenueItems.stream()
                        .filter(r -> r.getName().equals(selectedName))
                        .findFirst()
                        .orElse(null);

                if (item != null) {
                    CollectionItems collectionItem = new CollectionItems(item, roomNumber);
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
                    setRevenuesTableList.add(collectionItem);
                }
            }
        }
        setRevenuesTable.refresh();
    }

    private boolean areRequiredFieldsEmpty() {
        return periodField.getText().isEmpty() ||
                (!applyAllRoomsCheckBox.isSelected() && roomBox.getValue() == null) ||
                revenueListView.getSelectionModel().getSelectedItems().isEmpty();
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            if (areRequiredFieldsEmpty()) {
                showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            // Kiểm tra quantity của tất cả các khoản thu
            if (!applyAllRoomsCheckBox.isSelected()) {
                for (CollectionItems item : setRevenuesTableList) {
                    if (item.getQuantity() <= 0) {
                        showErrorAlert("Vui lòng nhập đầy đủ số lượng: \n" + item.getName());
                        return;
                    }
                }
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
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