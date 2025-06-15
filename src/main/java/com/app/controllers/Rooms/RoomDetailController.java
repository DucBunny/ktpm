package com.app.controllers.Rooms;

import com.app.controllers.HeaderUtils.HeaderController;
import com.app.controllers.Residents.EditResidentController;
import com.app.controllers.Residents.ResidentDetailController;
import com.app.models.Residents;
import com.app.utils.*;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class RoomDetailController {
    private double elasticity;      // co giãn (nếu ẩn cột)

    //    Header
    @FXML
    private VBox headerPane; // Tiêm nút gốc của header.fxml
    @FXML
    private HeaderController headerPaneController; // Tiêm controller của header.fxml

    // Body
    private String roomNumber;

    @FXML
    private Button btnCreateResident;

    @FXML
    private VBox ownerBox;
    @FXML
    private Label ownerNameField;
    @FXML
    private Label roomNumberLabel;
    @FXML
    private Label roomAreaLabel;
    @FXML
    private Label roomStatusLabel;

    @FXML
    private TableView<Residents> tableResidents;
    @FXML
    private TableColumn<Residents, String> nameResidents;
    @FXML
    private TableColumn<Residents, LocalDate> dateOfBirthResidents;
    @FXML
    private TableColumn<Residents, String> genderResidents;
    @FXML
    private TableColumn<Residents, String> phoneResidents;
    @FXML
    private TableColumn<Residents, String> idCardNumberResidents;
    @FXML
    private TableColumn<Residents, String> relationshipResidents;
    @FXML
    private TableColumn<Residents, String> residenceStatusResidents;
    @FXML
    private TableColumn<Residents, Void> actionResidents;

    private final ObservableList<Residents> residentsList = FXCollections.observableArrayList();

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void initializeHeader() {
        if (headerPaneController != null) {
            headerPaneController.setUserInfo(UserSession.getEmail(), UserSession.getRole(), UserSession.getUsername());
            try {
                headerPaneController.initialize();
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("headerPaneController is null in HomePageController!");
        }
    }

    public void initialize(String role) {
        if (StageManager.getPrimaryStage().getScene() != null) {
            StageManager.getPrimaryStage().getScene().getRoot().getProperties().put("controller", this);
        }

        if (Objects.equals(role, "admin")) {
            btnCreateResident.setVisible(true);
            actionResidents.setVisible(true);
            elasticity = 1;
            addActionButtonsToTable();
        } else if (Objects.equals(role, "accountant")) {
            elasticity = (double) 10 / 9;       // ẩn cột action 10%
        }

        tableResidents.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Thiết lập lắng nghe khi có thay đổi kích thước hoặc dữ liệu
        tableResidents.widthProperty().addListener((obs, oldVal, newVal) -> adjustColumnWidths());
        residentsList.addListener((ListChangeListener<Residents>) c -> adjustColumnWidths());

        nameResidents.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        dateOfBirthResidents.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        genderResidents.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneResidents.setCellValueFactory(new PropertyValueFactory<>("phone"));
        idCardNumberResidents.setCellValueFactory(new PropertyValueFactory<>("idCardNumber"));
        relationshipResidents.setCellValueFactory(new PropertyValueFactory<>("relationshipToOwner"));
        residenceStatusResidents.setCellValueFactory(new PropertyValueFactory<>("residenceStatus"));

        // Định dạng lại ngày sinh
        dateOfBirthResidents.setCellFactory(DateFormat.forLocalDate());

        // Nháy chuột vào row sẽ mở chi tiết cư dân
        tableResidents.setRowFactory(tv -> {
            TableRow<Residents> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 2 && row.getItem() != null) {
                    try {
                        openResidentDetailScene(row.getItem());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row;
        });

        loadRoomData();
    }

    private void adjustColumnWidths() {
        // Lấy số dòng dữ liệu (item count)
        int rowCount = residentsList.size();
        // Lấy chiều cao 1 hàng
        double rowHeight = 20;
        // Trừ chiều cao header
        double headerHeight = 30;
        // Tổng chiều cao dữ liệu
        double totalRowsHeight = rowCount * rowHeight;
        // Chiều cao hiển thị thực tế
        double tableContentHeight = tableResidents.getHeight() - headerHeight;

        // Nếu tổng chiều cao dữ liệu > chiều cao table -> có scroll bar
        double padding = (totalRowsHeight > tableContentHeight) ? 15 : 0;
        double tableWidth = tableResidents.getWidth() - padding;

        nameResidents.setPrefWidth(tableWidth * 0.2 * elasticity);
        dateOfBirthResidents.setPrefWidth(tableWidth * 0.1 * elasticity);
        genderResidents.setPrefWidth(tableWidth * 0.1 * elasticity);
        phoneResidents.setPrefWidth(tableWidth * 0.15 * elasticity);
        idCardNumberResidents.setPrefWidth(tableWidth * 0.15 * elasticity);
        relationshipResidents.setPrefWidth(tableWidth * 0.1 * elasticity);
        residenceStatusResidents.setPrefWidth(tableWidth * 0.1 * elasticity);
        actionResidents.setPrefWidth(tableWidth * 0.1 * elasticity);
    }

    // Body --------------------------------------------------------------------
    public void handleCreateResident() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/Residents/create-resident.fxml", "/styles/Residents/crud-resident.css", owner);

        //  Reload lại bảng
        residentsList.clear();
        loadResidentsData();
    }

    public void openVehicleScene() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/Rooms/vehicles.fxml", "/styles/Rooms/vehicles.css", owner);

        VehiclesController controller = loader.getController();
        controller.initialize(UserSession.getRole(), roomNumber);
    }

    public void openResidentDetailScene(Residents resident) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        ResidentDetailController.setResidentDetail(resident); // Hàm static để tạm giữ dữ liệu
        SceneNavigator.showPopupScene("/fxml/Residents/resident-detail.fxml", "/styles/Residents/crud-resident.css", owner);
    }

    public void loadRoomData() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM rooms WHERE room_number = '" + roomNumber + "'";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                String roomNumber = resultSet.getString("room_number");
                String roomArea = resultSet.getString("area");
                String roomStatus = resultSet.getString("status");

                // Gán lên các Label trong giao diện
                roomNumberLabel.setText(roomNumber);
                roomAreaLabel.setText(roomArea + " m");
                roomStatusLabel.setText(roomStatus.equals("occupied") ? "Đang ở" : "Trống");


                if (roomStatus.equals("occupied")) {
                    ownerBox.setVisible(true);
                    loadResidentsData();
                }
            } else {
                CustomAlert.showErrorAlert("Không tìm thấy căn hộ " + roomNumber + "từ CSDL.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải dữ liệu căn hộ từ CSDL.");
        }
    }

    public void loadResidentsData() {
        residentsList.clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT * FROM residents WHERE room_number = '" + roomNumber + "'";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String relationshipRaw = resultSet.getString("relationship_to_owner");
                String relationshipDisplay = switch (relationshipRaw) {
                    case "owner" -> "Chủ hộ";
                    case "spouse" -> "Vợ/Chồng";
                    case "parent" -> "Cha/Mẹ";
                    case "child" -> "Con cái";
                    default -> "Khác";
                };

                Residents resident = new Residents(
                        resultSet.getInt("id"),
                        resultSet.getString("full_name"),
                        resultSet.getDate("date_of_birth").toLocalDate(),
                        resultSet.getString("gender").equals("male") ? "Nam" : "Nữ",
                        resultSet.getString("phone"),
                        resultSet.getString("id_card_number"),
                        relationshipDisplay,
                        resultSet.getString("residence_status").equals("permanent") ? "Thường trú" : "Tạm trú"
                );

                resident.setRoomNumber(resultSet.getString("room_number"));
                resident.setPlaceOfBirth(resultSet.getString("place_of_birth"));
                resident.setOccupation(resultSet.getString("occupation"));
                resident.setHometown(resultSet.getString("hometown"));
                resident.setEthnicity(resultSet.getString("ethnicity"));
                resident.setStatus(resultSet.getString("status").equals("living") ? "Đang ở" : "Đã rời");

                residentsList.add(resident);

                if (resultSet.getString("relationship_to_owner").equals("owner")) {
                    ownerNameField.setText(resultSet.getString("full_name"));
                }
            }

            tableResidents.setItems(residentsList);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải dữ liệu cư dân từ CSDL.");
        }
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<Residents, Void>, TableCell<Residents, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Residents, Void> call(final TableColumn<Residents, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");

                    {
                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 40; -fx-font-size: 12");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            Residents data = getTableView().getItems().get(getIndex());
                            try {
                                handleEditResidents(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 40; -fx-font-size: 12");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            Residents data = getTableView().getItems().get(getIndex());
                            try {
                                handleDeleteResidents(data);
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

        actionResidents.setCellFactory(cellFactory);
    }

    private void handleEditResidents(Residents residents) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        EditResidentController.setResidentToEdit(residents); // Hàm static để tạm giữ dữ liệu
        SceneNavigator.showPopupScene("/fxml/Residents/edit-resident.fxml", "/styles/Residents/crud-resident.css", owner);

        // Sau khi sửa, làm mới lại bảng dữ liệu:
        residentsList.clear();
        loadResidentsData();
    }

    private void handleDeleteResidents(Residents residents) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn xóa cư dân này?", residents.getFullName());
        if (result) {
            try {
                Connection connection = DatabaseConnection.getConnection();
                String sql = "DELETE FROM residents WHERE id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, residents.getId());

                if (stmt.executeUpdate() > 0) {
                    tableResidents.getItems().remove(residents);
                    System.out.println("Đã xóa cư dân: " + residents.getFullName());
                    CustomAlert.showSuccessAlert("Đã xóa cư dân thành công", true, 0.7);
                } else {
                    CustomAlert.showErrorAlert("Không tìm thấy cư dân để xóa.");
                }
            } catch (Exception e) {
                e.printStackTrace();
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
