package com.app.controllers.Rooms;

import com.app.controllers.HomePageController;
import com.app.controllers.Payments.CollectionPeriods.CollectionPeriodsController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Revenues.RevenuesController;
import com.app.models.Rooms;
import com.app.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class RoomsController {
    private String role;
    private String username;

    //    Header
    @FXML
    private Label roleLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private MenuItem MenuItem_SignUp;

    //    Body
    @FXML
    private GridPane roomsGrid;
    @FXML
    private TextField roomField;
    @FXML
    private ComboBox<ComboBoxOption> floorBox;
    @FXML
    private ComboBox<ComboBoxOption> statusBox;

    private final ObservableList<Rooms> roomList = FXCollections.observableArrayList();

    private final ObservableList<Rooms> filteredRoomList = FXCollections.observableArrayList();

    @FXML
    public void initialize(String role, String username) throws IOException {
        this.role = role;
        this.username = username;

        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
        } else if (Objects.equals(role, "accountant")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Kế toán.");
        }

        nameLabel.setText("Xin chào, " + username);

        initFloorBox();
        initStatusBox();
        loadRoomsFromDatabase();

        filteredRoomList.addAll(roomList);
        populateRoomsGrid();

        setupSearchAndFilter();
    }

    private void initFloorBox() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT DISTINCT floor FROM rooms ORDER BY floor ASC";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<ComboBoxOption> allFloors = FXCollections.observableArrayList();

            while (rs.next()) {
                int floor = rs.getInt("floor");
                ComboBoxOption option = new ComboBoxOption("Tầng " + floor, String.valueOf(floor));
                allFloors.add(option);
            }
            allFloors.addFirst(new ComboBoxOption("Tất cả", "Tất cả"));
            floorBox.setItems(allFloors);
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải dữ liệu tầng từ CSDL.");
        }
    }

    private void initStatusBox() {
        statusBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Tất cả", "Tất cả"),
                new ComboBoxOption("Đang ở", "occupied"),
                new ComboBoxOption("Trống", "available")
        ));
    }

    // Header Burton -----------------------------------------------------------
    public void changeToHomePage(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/home-page.fxml"
                , "/styles/home-page.css", event, true);

        HomePageController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToResidents(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Residents/residents.fxml", "/styles/Residents/residents.css",
                event, true);

        ResidentsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToRevenues(Event event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Revenues/revenues.fxml", "/styles/Revenues/revenues.css",
                event, true);

        RevenuesController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToPayments(Event event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Payments/collection-periods.fxml", "/styles/Payments/CollectionPeriods/collection-periods.css",
                event, true);

        CollectionPeriodsController controller = loader.getController();
        controller.initialize(role, username);
    }

    // Pop-up Button Cài đặt ---------------------------------------------------
    public void changeToSignUp() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/create-account.fxml",
                "/styles/sign-in-create-account.css", owner);
    }

    public void changeToSignIn(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/sign-in.fxml", "/styles/sign-in-create-account.css",
                event, false);
    }

    // Body --------------------------------------------------------------------
    public void openRoomDetailScene(Event event, String roomNumber) throws IOException {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Rooms/room-detail.fxml", "/styles/Rooms/room-detail.css",
                event, true);

        RoomDetailController controller = loader.getController();
        controller.initialize(role, username, roomNumber);
    }

    public void loadRoomsFromDatabase() {
        roomList.clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM rooms ORDER BY floor ASC, room_number ASC";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Rooms room = new Rooms(
                        rs.getString("room_number"),
                        rs.getInt("floor"),
                        rs.getFloat("area"),
                        rs.getString("status")
                );
                roomList.add(room);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải danh sách phòng từ CSDL.");
        }
    }

    private void setupSearchAndFilter() {
        // Lọc khi nhập tên phòng
        roomField.textProperty().addListener((observable, oldValue, newValue) -> filterRooms());

        // Lọc khi chọn tầng
        floorBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> filterRooms());

        // Lọc khi chọn trạng thái
        statusBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> filterRooms());
    }

    private void filterRooms() {
        String roomName = roomField.getText().trim().toLowerCase();
        String selectedFloor = (floorBox.getValue() != null) ? (floorBox.getValue().getValue()) : null;
        String selectedStatus = (statusBox.getValue() != null) ? (statusBox.getValue().getValue()) : null;

        filteredRoomList.clear();

        for (Rooms room : roomList) {
            boolean matches = true;

            // Lọc theo tên phòng (số phòng)
            if (!roomName.isEmpty() && !room.getRoomNumber().toLowerCase().contains(roomName)) {
                matches = false;
            }

            // Lọc theo tầng - bỏ qua nếu chọn "Tất cả" hoặc chưa chọn gì
            if (selectedFloor != null && !selectedFloor.equals("Tất cả") && !(room.getFloor() == Integer.parseInt(selectedFloor))) {
                matches = false;
            }

            // Lọc theo trạng thái - bỏ qua nếu chọn "Tất cả" hoặc chưa chọn gì
            if (selectedStatus != null && !selectedStatus.equals("Tất cả") && !room.getStatus().equals(selectedStatus)) {
                matches = false;
            }

            if (matches) {
                filteredRoomList.add(room);
            }
        }

        // Cập nhật lại GridPane
        populateRoomsGrid();
    }

    // Hiển thị grid child
    private void populateRoomsGrid() {
        roomsGrid.getChildren().clear();
        roomsGrid.getRowConstraints().clear();
        int column = 0, row = 0;

        for (Rooms room : filteredRoomList) {
            // Tạo VBox cho mỗi phòng
            VBox roomBox = new VBox(20);
            roomBox.setAlignment(Pos.CENTER);
            roomBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 5, 0.1, 0, 2);");
            roomBox.setPrefHeight(188.0);
            roomBox.setPrefWidth(410.0);
            roomBox.setOnMouseEntered(e -> {
                roomBox.setStyle("-fx-translate-y: -1px; -fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 5, 0.1, 0, 2);");
            });
            roomBox.setOnMouseExited(e -> {
                roomBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 5, 0.1, 0, 2);");
            });

            // Tạo Label cho tên phòng
            Label roomLabel = new Label("Phòng " + room.getRoomNumber());
            roomLabel.setAlignment(Pos.CENTER);
            roomLabel.setPrefWidth(350.0);
            roomLabel.setPrefHeight(70.0);
            roomLabel.setStyle("-fx-background-color: #586995; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 36");
            roomLabel.setTextFill(Color.WHITE);

            // Tạo HBox chứa trạng thái và nút chi tiết
            HBox statusBox = new HBox(20);
            statusBox.setAlignment(Pos.CENTER);
            statusBox.setPrefHeight(40.0);
            statusBox.setPrefWidth(410.0);

            Label statusLabel = new Label("Trạng thái: " + (room.getStatus().equals("occupied") ? "Đang ở" : "Trống"));
            statusLabel.setAlignment(Pos.CENTER);
            statusLabel.setPrefHeight(40.0);
            statusLabel.setPrefWidth(210.0);
            statusLabel.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-border-color: #586995; -fx-border-radius: 10; -fx-font-size: 18");

            Button detailButton = new Button("Chi tiết");
            detailButton.setAlignment(Pos.CENTER);
            detailButton.setPrefHeight(40.0);
            detailButton.setPrefWidth(120.0);
            detailButton.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-border-color: #586995; -fx-border-radius: 10; -fx-cursor: hand; -fx-font-size: 18");
            detailButton.setOnMouseEntered(e -> {
                detailButton.setStyle(" -fx-translate-y: -0.5px; -fx-background-color: #E0E0E0; -fx-background-radius: 10; -fx-border-color: #586995; -fx-border-radius: 10; -fx-cursor: hand; -fx-font-size: 18;");
            });
            detailButton.setOnMouseExited(e -> {
                detailButton.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-border-color: #586995; -fx-border-radius: 10; -fx-cursor: hand; -fx-font-size: 18;");
            });
            detailButton.setOnMousePressed(e -> {
                detailButton.setStyle("-fx-translate-y: 1px; -fx-background-color: #D6D6D6; -fx-background-radius: 10; -fx-border-color: #586995; -fx-border-radius: 10; -fx-cursor: hand; -fx-font-size: 18;");
            });
            detailButton.setOnAction(e -> {
                try {
                    openRoomDetailScene(e, room.getRoomNumber());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            statusBox.getChildren().addAll(statusLabel, detailButton);
            roomBox.getChildren().addAll(roomLabel, statusBox);

            // Thêm roomBox vào GridPane
            roomsGrid.add(roomBox, column, row);
            column++;
            if (column == 3) { // 3 cột mỗi hàng
                column = 0;
                row++;
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