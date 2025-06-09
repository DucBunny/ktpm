package com.app.controllers.Rooms;

import com.app.models.Vehicles;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VehiclesController {
    private String roomNumber;

    @FXML
    private TableView<Vehicles> tableVehicles;
    @FXML
    private TableColumn<Vehicles, String> typeVehicles;
    @FXML
    private TableColumn<Vehicles, String> plateNumberVehicles;
    @FXML
    private TableColumn<Vehicles, String> brandVehicles;
    @FXML
    private TableColumn<Vehicles, String> colorVehicles;
    @FXML
    private TableColumn<Vehicles, LocalDate> registrationDateVehicles;
    @FXML
    private TableColumn<Vehicles, String> noteVehicles;
    @FXML
    private TableColumn<Vehicles, String> isActiveVehicles;
    @FXML
    private TableColumn<Vehicles, Void> actionVehicles;

    private final ObservableList<Vehicles> vehiclesList = FXCollections.observableArrayList();

    @FXML
    public void initialize(String roomNumber) {
        this.roomNumber = roomNumber;

        tableVehicles.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Thiết lập lắng nghe khi có thay đổi kích thước hoặc dữ liệu
        tableVehicles.widthProperty().addListener((obs, oldVal, newVal) -> adjustColumnWidths());
        vehiclesList.addListener((ListChangeListener<Vehicles>) c -> adjustColumnWidths());

        typeVehicles.setCellValueFactory(new PropertyValueFactory<>("type"));
        plateNumberVehicles.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        brandVehicles.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colorVehicles.setCellValueFactory(new PropertyValueFactory<>("color"));
        registrationDateVehicles.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        noteVehicles.setCellValueFactory(new PropertyValueFactory<>("note"));
        isActiveVehicles.setCellValueFactory(new PropertyValueFactory<>("isActive"));

        // Định dạng lại ngày sinh
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        registrationDateVehicles.setCellFactory(column -> new TableCell<Vehicles, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        // Nháy chuột vào row sẽ mở chi tiết cư dân
        tableVehicles.setRowFactory(tv -> {
            TableRow<Vehicles> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 2 && row.getItem() != null) {
                    try {
                        openVehicleDetailScene(row.getItem());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row;
        });

        loadVehiclesFromDatabase();
        addActionButtonsToTable();
    }

    private void adjustColumnWidths() {
        // Lấy số dòng dữ liệu (item count)
        int rowCount = vehiclesList.size();
        // Lấy chiều cao 1 hàng
        double rowHeight = 20;
        // Trừ chiều cao header
        double headerHeight = 30;
        // Tổng chiều cao dữ liệu
        double totalRowsHeight = rowCount * rowHeight;
        // Chiều cao hiển thị thực tế
        double tableContentHeight = tableVehicles.getHeight() - headerHeight;

        // Nếu tổng chiều cao dữ liệu > chiều cao table -> có scroll bar
        double padding = (totalRowsHeight > tableContentHeight) ? 18 : 0;
        double tableWidth = tableVehicles.getWidth() - padding;

        typeVehicles.setPrefWidth(tableWidth * 0.2);
        plateNumberVehicles.setPrefWidth(tableWidth * 0.1);
        brandVehicles.setPrefWidth(tableWidth * 0.1);
        colorVehicles.setPrefWidth(tableWidth * 0.1);
        registrationDateVehicles.setPrefWidth(tableWidth * 0.15);
        noteVehicles.setPrefWidth(tableWidth * 0.15);
        isActiveVehicles.setPrefWidth(tableWidth * 0.1);
        actionVehicles.setPrefWidth(tableWidth * 0.1);
    }

    // Body --------------------------------------------------------------------
    public void handleCreateVehicle() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/Rooms/create-vehicle.fxml", "/styles/Rooms/crud-vehicle.css", owner);

        CreateVehicleController controller = loader.getController();
        controller.initialize(roomNumber);

        //  Reload lại bảng
        controller.setCallback(() -> {
            // Chỉ reload khi thực sự thêm mới thành công!
            vehiclesList.clear();
            loadVehiclesFromDatabase();
        });
    }

    public void openVehicleDetailScene(Vehicles vehicle) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        VehicleDetailController.setVehicleDetail(vehicle); // Hàm static để tạm giữ dữ liệu
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/Rooms/vehicle-detail.fxml", "/styles/Rooms/crud-vehicle.css", owner);

        VehicleDetailController controller = loader.getController();
        controller.initialize(roomNumber);
    }

    public void loadVehiclesFromDatabase() {
        vehiclesList.clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT * FROM vehicles WHERE room_number = '" + roomNumber + "' ORDER BY is_active DESC, registration_date DESC";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Vehicles vehicle = new Vehicles(
                        resultSet.getInt("id"),
                        resultSet.getString("type").equals("motorbike") ? "Xe máy" : "Ô tô",
                        resultSet.getString("plate_number"),
                        resultSet.getString("brand"),
                        resultSet.getString("color"),
                        resultSet.getDate("registration_date").toLocalDate(),
                        resultSet.getBoolean("is_active") ? "Hoạt động" : "Đã hủy",
                        resultSet.getString("note")
                );

                vehiclesList.add(vehicle);
            }

            tableVehicles.setItems(vehiclesList);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CustomAlert.showErrorAlert("Không thể tải dữ liệu phương tiện từ CSDL.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<Vehicles, Void>, TableCell<Vehicles, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Vehicles, Void> call(final TableColumn<Vehicles, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");

                    {
                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 40; -fx-font-size: 12");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            Vehicles data = getTableView().getItems().get(getIndex());
                            try {
                                handleEdit(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 40; -fx-font-size: 12");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            Vehicles data = getTableView().getItems().get(getIndex());
                            try {
                                handleDelete(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                    private final HBox pane = new HBox(10, btnEdit, btnDelete);

                    {
                        pane.setAlignment(Pos.CENTER);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };

        actionVehicles.setCellFactory(cellFactory);
    }

    private void handleEdit(Vehicles vehicle) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        EditVehicleController.setVehicleToEdit(vehicle); // Hàm static để tạm giữ dữ liệu
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/Rooms/edit-vehicle.fxml", "/styles/Rooms/crud-vehicle.css", owner);

        EditVehicleController controller = loader.getController();
        controller.initialize(roomNumber);

        // Sau khi sửa, làm mới lại bảng dữ liệu:
        controller.setCallback(() -> {
            // Chỉ reload khi thực sự thêm mới thành công!
            vehiclesList.clear();
            loadVehiclesFromDatabase();
        });
    }

    private void handleDelete(Vehicles Vehicles) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn xóa phương tiện này?", Vehicles.getPlateNumber());
        if (result) {
            try {
                Connection connection = DatabaseConnection.getConnection();
                String sql = "DELETE FROM Vehicles WHERE id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, Vehicles.getId());

                if (stmt.executeUpdate() > 0) {
                    tableVehicles.getItems().remove(Vehicles);
                    System.out.println("Đã xóa phương tiện: " + Vehicles.getPlateNumber());
                    CustomAlert.showSuccessAlert("Đã xóa phương tiện thành công", true, 0.7);
                } else {
                    CustomAlert.showErrorAlert("Không tìm thấy phương tiện để xóa.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
