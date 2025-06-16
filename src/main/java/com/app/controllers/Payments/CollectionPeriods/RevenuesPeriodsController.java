package com.app.controllers.Payments.CollectionPeriods;

import com.app.models.CollectionItems;
import com.app.models.Revenues;
import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.ExcelReader;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RevenuesPeriodsController {
    private int collectionPeriodId;
    private String collectionPeriodName;

    private CollectionPeriodsReloadCallback callback;

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
    private Button openFolderButton;

    @FXML
    private Button saveButton;

    private final Map<String, File> selectedFiles = new HashMap<>(); // Lưu file Excel được chọn
    private static final int ELECTRICITY_REVENUE_ID = 5; // ID của "Tiền điện"
    private static final int WATER_REVENUE_ID = 6; // ID của "Tiền nước"
    private static final int INTERNET_REVENUE_ID = 7; // ID của "Tiền internet"

    private final List<Revenues> revenueItems = new ArrayList<>();
    private final ObservableList<ComboBoxOption> roomSuggestions = FXCollections.observableArrayList();
    private final ObservableList<ComboBoxOption> allRooms = FXCollections.observableArrayList();
    private final ObservableList<CollectionItems> setRevenuesTableList = FXCollections.observableArrayList();

    public void setCallback(CollectionPeriodsReloadCallback callback) {
        this.callback = callback;
    }

    public void setCollectionPeriod(int collectionPeriodId, String collectionPeriodName) {
        this.collectionPeriodId = collectionPeriodId;
        this.collectionPeriodName = collectionPeriodName;
        periodField.setText(collectionPeriodName);

    }

    public void initialize() {
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
                revenueListView.getSelectionModel().getSelectedItems().isEmpty() ||
                setRevenuesTableList.isEmpty();
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
                    // Xóa các collection_items cũ cho các phòng được chọn
                    String deleteSql = "DELETE FROM collection_items WHERE collection_period_id = ? AND room_number = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                        List<String> selectedRooms = applyAllRoomsCheckBox.isSelected()
                                ? allRooms.stream().map(ComboBoxOption::getValue).toList()
                                : roomBox.getValue() != null ? List.of(roomBox.getValue().getValue()) : List.of();
                        for (String room : selectedRooms) {
                            deleteStmt.setInt(1, collectionPeriodId);
                            deleteStmt.setString(2, room);
                            deleteStmt.addBatch();
                        }
                        deleteStmt.executeBatch();
                    }

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

                        // Từ file Excel
                        List<CollectionItems> excelItems = new ArrayList<>();
                        File electricityFile = selectedFiles.get("electricity");
                        File waterFile = selectedFiles.get("water");
                        File internetFile = selectedFiles.get("internet");

                        if (electricityFile != null) {
                            excelItems.addAll(ExcelReader.readElectricityData(electricityFile.getAbsolutePath(), ELECTRICITY_REVENUE_ID));
                        }
                        if (waterFile != null) {
                            excelItems.addAll(ExcelReader.readWaterData(waterFile.getAbsolutePath(), WATER_REVENUE_ID));
                        }
                        if (internetFile != null) {
                            excelItems.addAll(ExcelReader.readInternetData(internetFile.getAbsolutePath(), INTERNET_REVENUE_ID));
                        }

                        List<String> selectedRooms = applyAllRoomsCheckBox.isSelected()
                                ? allRooms.stream().map(ComboBoxOption::getValue).toList()
                                : roomBox.getValue() != null ? List.of(roomBox.getValue().getValue()) : List.of();
                        for (CollectionItems item : excelItems) {
                            if (selectedRooms.contains(item.getRoomNumber())) {
                                stmt.setInt(1, collectionPeriodId);
                                stmt.setString(2, item.getRoomNumber());
                                stmt.setInt(3, item.getRevenueId());
                                stmt.setDouble(4, item.getQuantity());
                                stmt.setString(5, item.getQuantityUnit());
                                stmt.setDouble(6, item.getUnitPrice());
                                stmt.setDouble(7, item.getTotalAmount());
                                stmt.addBatch();
                            }
                        }

                        stmt.executeBatch(); // Chạy nhóm câu lệnh SQL
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Thiết lập khoản thu thành công", true, 0.7);
                    if (callback != null) { // gọi callback
                        callback.onPeriodCrud();
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

    // File --------------------------------------------------------------------
    @FXML
    private void handleOpenFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Chọn thư mục chứa file Excel");
        File selectedDirectory = directoryChooser.showDialog(openFolderButton.getScene().getWindow());
        if (selectedDirectory != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn file Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.XLSX")
            );
            if (!selectedDirectory.canRead()) {
                showErrorAlert("Không có quyền đọc thư mục: " + selectedDirectory.getAbsolutePath());
                return;
            }
            fileChooser.setInitialDirectory(selectedDirectory);
            File file = fileChooser.showOpenDialog(openFolderButton.getScene().getWindow());
            if (file != null) {
                try {
                    String fileName = file.getName().toLowerCase();
                    List<CollectionItems> excelItems = new ArrayList<>();
                    String fileKey = null;
                    int revenueId;
                    String revenueName = "";
                    if (fileName.contains("dien")) {
                        fileKey = "electricity";
                        revenueId = ELECTRICITY_REVENUE_ID;
                        excelItems.addAll(ExcelReader.readElectricityData(file.getAbsolutePath(), revenueId));
                        revenueName = revenueItems.stream()
                                .filter(r -> r.getId() == revenueId)
                                .map(Revenues::getName)
                                .findFirst()
                                .orElse("Tiền điện");
                    } else if (fileName.contains("nuoc")) {
                        fileKey = "water";
                        revenueId = WATER_REVENUE_ID;
                        excelItems.addAll(ExcelReader.readWaterData(file.getAbsolutePath(), revenueId));
                        revenueName = revenueItems.stream()
                                .filter(r -> r.getId() == revenueId)
                                .map(Revenues::getName)
                                .findFirst()
                                .orElse("Tiền nước");
                    } else if (fileName.contains("internet")) {
                        fileKey = "internet";
                        revenueId = INTERNET_REVENUE_ID;
                        excelItems.addAll(ExcelReader.readInternetData(file.getAbsolutePath(), revenueId));
                        revenueName = revenueItems.stream()
                                .filter(r -> r.getId() == revenueId)
                                .map(Revenues::getName)
                                .findFirst()
                                .orElse("Tiền internet");
                    } else {
                        revenueId = 0;
                        showErrorAlert("File không hợp lệ. Vui lòng chọn file có tên chứa 'dien', 'nuoc', hoặc 'internet'.");
                        return;
                    }

                    // Log dữ liệu từ excelItems
                    System.out.println("Dữ liệu từ file Excel (" + excelItems.size() + " dòng):");
                    for (CollectionItems item : excelItems) {
                        System.out.println("Room=" + item.getRoomNumber() + ", Quantity=" + item.getQuantity() +
                                ", TotalAmount=" + item.getTotalAmount());
                    }

                    if (excelItems.isEmpty()) {
                        showErrorAlert("Không tìm thấy dữ liệu hợp lệ trong file: " + fileName + ". Kiểm tra cấu trúc: Căn hộ (chuỗi), Số lượng (số), Đơn giá (số), Thành tiền (số).");
                        System.err.println("Dữ liệu Excel rỗng: " + fileName);
                        return;
                    }

                    selectedFiles.put(fileKey, file);

                    // Tự động chọn phòng từ file Excel
                    Set<String> excelRooms = excelItems.stream()
                            .map(CollectionItems::getRoomNumber)
                            .collect(Collectors.toSet());

                    // Kiểm tra phòng trong CSDL
                    Set<String> dbRooms = allRooms.stream().map(ComboBoxOption::getValue).collect(Collectors.toSet());

                    Set<String> invalidRooms = excelRooms.stream()
                            .filter(room -> !dbRooms.contains(room))
                            .collect(Collectors.toSet());
                    if (!invalidRooms.isEmpty()) {
                        showErrorAlert("Các phòng sau trong file Excel không tồn tại trong CSDL: " + invalidRooms);
                        System.err.println("Phòng không tồn tại trong CSDL: " + invalidRooms);
                        return;
                    }

                    // Không tự động chọn applyAllRoomsCheckBox, chỉ chọn phòng trong file
                    applyAllRoomsCheckBox.setSelected(false);
                    roomBox.getSelectionModel().clearSelection();

                    // Chọn khoản thu trong revenueListView
                    if (!revenueListView.getItems().contains(revenueName)) {
                        showErrorAlert("Khoản thu '" + revenueName + "' không tồn tại trong danh sách. Kiểm tra revenue_items trong CSDL.");
                        System.err.println("Khoản thu không tồn tại: " + revenueName);
                        return;
                    }
                    revenueListView.getSelectionModel().clearSelection();
                    revenueListView.getSelectionModel().select(revenueName);
                    System.out.println("Chọn khoản thu: " + revenueName);

                    // Cập nhật setRevenuesTable, chỉ thêm phòng từ file Excel
                    setRevenuesTableList.clear(); // Xóa dữ liệu cũ
                    for (CollectionItems excelItem : excelItems) {
                        // Kiểm tra trùng lặp
                        boolean exists = setRevenuesTableList.stream()
                                .anyMatch(item -> item.getRoomNumber().equals(excelItem.getRoomNumber()) &&
                                        item.getRevenueId() == excelItem.getRevenueId());
                        if (!exists) {
                            excelItem.setName(revenueName); // Đảm bảo tên khoản thu đúng
                            setRevenuesTableList.add(excelItem);
                            System.out.println("Thêm vào setRevenuesTable: room=" + excelItem.getRoomNumber() +
                                    ", revenueId=" + excelItem.getRevenueId() + ", name=" + excelItem.getName() +
                                    ", quantity=" + excelItem.getQuantity() + ", totalAmount=" + excelItem.getTotalAmount());
                        }
                    }
                    setRevenuesTable.refresh();
                    System.out.println("Đã chọn file " + fileKey + ": " + file.getAbsolutePath());
                    System.out.println("Tổng số mục trong setRevenuesTable: " + setRevenuesTableList.size());

                    //                    if (excelRooms.containsAll(allRooms.stream().map(ComboBoxOption::getValue).collect(Collectors.toSet()))) {
                    //                        applyAllRoomsCheckBox.setSelected(true);
                    //                        roomBox.getSelectionModel().clearSelection();
                    //                    } else {
                    //                        applyAllRoomsCheckBox.setSelected(false);
                    //                        ComboBoxOption selectedRoom = allRooms.stream()
                    //                                .filter(room -> excelRooms.contains(room.getValue()))
                    //                                .findFirst()
                    //                                .orElse(null);
                    //                        if (selectedRoom != null) {
                    //                            roomBox.setValue(selectedRoom);
                    //                        } else {
                    //                            roomBox.getSelectionModel().clearSelection();
                    //                        }
                    //                    }

                    // Chọn khoản thu trong revenueListView
                    //                    if (!revenueListView.getSelectionModel().getSelectedItems().contains(revenueName)) {
                    //                        revenueListView.getSelectionModel().select(revenueName);
                    //                    }
                    //
                    //                    // Cập nhật setRevenuesTable, tránh trùng lặp
                    //                    List<String> selectedRooms = applyAllRoomsCheckBox.isSelected()
                    //                            ? allRooms.stream().map(ComboBoxOption::getValue).toList()
                    //                            : roomBox.getValue() != null ? List.of(roomBox.getValue().getValue()) : List.of();
                    //                    for (CollectionItems excelItem : excelItems) {
                    //                        if (selectedRooms.contains(excelItem.getRoomNumber())) {
                    //                            // Kiểm tra trùng lặp
                    //                            boolean exists = setRevenuesTableList.stream()
                    //                                    .anyMatch(item -> item.getRoomNumber().equals(excelItem.getRoomNumber()) &&
                    //                                            item.getRevenueId() == excelItem.getRevenueId());
                    //                            if (!exists) {
                    //                                setRevenuesTableList.add(excelItem);
                    //                            }
                    //                        }
                    //                    }
                    //                    setRevenuesTable.refresh();
                    //                    System.out.println("Đã chọn file " + fileKey + ": " + file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorAlert("Lỗi khi đọc file: " + e.getMessage());
                }
            }
        }
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