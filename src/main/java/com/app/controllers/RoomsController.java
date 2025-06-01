package com.app.controllers;

import com.app.models.Rooms;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
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
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    private TextField roomNameField;
    @FXML
    private ComboBox<String> floorComboBox;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private ComboBox<String> ownerComboBox;

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
            roleLabel.setText("Bạn đang đăng nhập với quyền Thu ngân.");
        }

        nameLabel.setText("Xin chào, " + username);

        // Khởi tạo ComboBox
        initializeComboBoxes();

        // Tải dữ liệu phòng từ database
        loadRoomsFromDatabase();

        // Hiển thị danh sách phòng lên GridPane
        filteredRoomList.addAll(roomList);
        populateRoomsGrid();

        // Thêm sự kiện lọc khi người dùng nhập hoặc chọn giá trị
        setupSearchAndFilter();
    }

    private void initializeComboBoxes() throws IOException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT DISTINCT floor FROM rooms")) {
            ObservableList<String> floors = FXCollections.observableArrayList();
            while (resultSet.next()) {
                floors.add(resultSet.getString("floor"));
            }
            floors.add("Tất cả");
            floorComboBox.setItems(floors);

        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.showErrorAlert("Không thể tải danh sách tầng.");
        }

        // Điền dữ liệu cho ComboBox trạng thái
        statusComboBox.setItems(FXCollections.observableArrayList("Đang ở", "Trống", "Đang sửa", "Tất cả"));

        // Điền dữ liệu cho ComboBox chủ hộ (Lấy dữ liệu từ bảng residents)
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT DISTINCT full_name FROM residents WHERE relationship_to_owner = 'owner'"
             )) {
            ObservableList<String> owners = FXCollections.observableArrayList();
            while (resultSet.next()) {
                owners.add(resultSet.getString("full_name"));
            }
            owners.add("Tất cả");
            ownerComboBox.setItems(owners);
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.showErrorAlert("Không thể tải danh sách chủ hộ.");
        }
    }

    //    Lấy ra danh sách các phòng hiện có
    private void loadRoomsFromDatabase() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM rooms");

            roomList.clear();
            while (resultSet.next()) {
                Rooms room = new Rooms(
                        resultSet.getInt("room_number"),
                        resultSet.getString("floor"),
                        resultSet.getString("area"),
                        resultSet.getString("status")
                );
                roomList.add(room);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSearchAndFilter() {
        // Lọc khi nhập tên phòng
        roomNameField.textProperty().addListener((observable, oldValue, newValue) -> filterRooms());

        // Lọc khi chọn tầng
        floorComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> filterRooms());

        // Lọc khi chọn trạng thái
        statusComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> filterRooms());

        // Lọc khi chọn chủ hộ
        ownerComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> filterRooms());
    }

    private void filterRooms() {
        String roomName = roomNameField.getText().trim().toLowerCase();
        String selectedFloor = floorComboBox.getValue();
        String selectedStatus = statusComboBox.getValue();
        String selectedOwner = ownerComboBox.getValue();

        filteredRoomList.clear();

        for (Rooms room : roomList) {
            boolean matches = true;

            // Lọc theo tên phòng (số phòng)
            if (!roomName.isEmpty() && !String.valueOf(room.getRoomNumber()).toLowerCase().contains(roomName)) {
                matches = false;
            }

            // Lọc theo tầng - bỏ qua nếu chọn "Tất cả" hoặc chưa chọn gì
            if (selectedFloor != null && !selectedFloor.equals("Tất cả") && !room.getFloor().equals(selectedFloor)) {
                matches = false;
            }

            // Lọc theo trạng thái - bỏ qua nếu chọn "Tất cả" hoặc chưa chọn gì
            if (selectedStatus != null && !selectedStatus.equals("Tất cả") && !room.getStatus().equals(selectedStatus)) {
                matches = false;
            }


            // Lọc theo chủ hộ (giả sử cần join với bảng owners để lấy thông tin)
            // Ở đây tôi giả định rằng Room không có thông tin chủ hộ, bạn có thể mở rộng nếu cần
            if (selectedOwner != null && !selectedOwner.equals("Tất cả")) {
                String roomOwner = getRoomOwner(room.getRoomNumber());
                if (roomOwner == null || !roomOwner.equals(selectedOwner)) {
                    matches = false;
                }
            }

            if (matches) {
                filteredRoomList.add(room);
            }
        }

        // Cập nhật lại GridPane
        populateRoomsGrid();
    }

    // Phương thức helper để lấy thông tin chủ hộ của phòng
    private String getRoomOwner(int roomNumber) {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT r.full_name FROM residents r " +
                             "WHERE r.room_number = " + roomNumber + " " +
                             "AND r.relationship_to_owner = 'owner'"
             )) {

            if (resultSet.next()) {
                return resultSet.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Không có chủ hộ hoặc lỗi truy vấn
    }

    //    Hiển thị grid child
    private void populateRoomsGrid() {
        roomsGrid.getChildren().clear(); // Xóa các phần tử hiện tại trong GridPane
        int column = 0;
        int row = 0;

        for (Rooms room : filteredRoomList) {
            // Tạo VBox cho mỗi phòng
            VBox roomBox = new VBox(20);
            roomBox.setAlignment(Pos.CENTER);
            roomBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 5, 0.1, 0, 2);");
            roomBox.setPrefHeight(180.0);

            // Tạo Label cho tên phòng
            Label roomLabel = new Label("P" + room.getRoomNumber());
            roomLabel.setAlignment(Pos.CENTER);
            roomLabel.setPrefWidth(350.0);
            roomLabel.setStyle("-fx-background-color: #586995; -fx-background-radius: 15;");
            roomLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            roomLabel.setFont(new Font(48.0));

            // Tạo HBox chứa trạng thái và nút chi tiết
            HBox statusBox = new HBox(20);
            statusBox.setAlignment(Pos.CENTER);
            statusBox.setPrefHeight(40.0);
            statusBox.setPrefWidth(410.0);

            Label statusLabel = new Label("Trạng thái: " + room.getStatus());
            statusLabel.setAlignment(Pos.CENTER);
            statusLabel.setPrefHeight(40.0);
            statusLabel.setPrefWidth(210.0);
            statusLabel.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-border-color: #586995; -fx-border-radius: 10;");
            statusLabel.setFont(new Font(18.0));

            Button detailButton = new Button("Chi tiết");
            detailButton.setAlignment(Pos.CENTER);
            detailButton.setPrefHeight(40.0);
            detailButton.setPrefWidth(120.0);
            detailButton.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-border-color: #586995; -fx-border-radius: 10;");
            detailButton.setFont(new Font(18.0));
            detailButton.setOnAction(event -> {
                //                try {
                //                    showRoomDetails(room);
                //                } catch (IOException e) {
                //                    throw new RuntimeException(e);
                //                }
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

    //    private void showRoomDetails(Room room) throws IOException {
    //        // Hiển thị chi tiết phòng
    //        CustomAlert.showAlert(Alert.AlertType.INFORMATION, "Chi tiết căn hộ",
    //                "Số phòng: P" + room.getRoomNumber() +
    //                        "\nTầng: " + room.getFloor() +
    //                        "\nDiện tích: " + room.getArea() +
    //                        "\nTrạng thái: " + room.getStatus());
    //    }

    //    Header Buton ---------------------------------------------------------
    public void changeToHomePage(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/home-page.fxml"
                , "/styles/home-page.css", event, true);

        HomePageController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToResidents(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/residents.fxml", "/styles/residents.css",
                event, true);

        ResidentsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToRevenues(Event event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/revenues.fxml", "/styles/revenues.css",
                event, true);

        RevenuesController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToPayments(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/payments.fxml", "/styles/payments.css",
                event, true);

        PaymentsController controller = loader.getController();
        controller.initialize(role, username);
    }

    //  Pop-up Button Cài đặt --------------------------------------------------
    public void changeToSignUp() {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-account.fxml",
                    "/styles/sign-in-create-account.css", owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToSignIn(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/sign-in.fxml", "/styles/sign-in-create-account.css",
                event, false);
    }
}