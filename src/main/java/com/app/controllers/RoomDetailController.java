package com.app.controllers;

import com.app.models.Residents;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class RoomDetailController {
    private String role;
    private String username;

    //    Header
    @FXML
    private Label roleLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private MenuItem MenuItem_SignUp;

    // Body
    private int roomname;

    @FXML
    private VBox ownerBox;
    @FXML
    private Button btnEditRoom;
    @FXML
    private Label ownerField;
    @FXML
    private Label roomName;     // fx:id="roomName"
    @FXML
    private Label roomArea;     // fx:id="roomArea"
    @FXML
    private Label roomStatus;   // fx:id="roomStatus"

    // ==== Bảng Danh sách cư dân ====
    @FXML
    private TableView<Residents> tableResidents;     // fx:id="tableResidents"
    @FXML
    private TableColumn<Residents, String> colHoTen; // fx:id="colHoTen"
    @FXML
    private TableColumn<Residents, String> colQuanHe; // fx:id="colQuanHe"
    @FXML
    private TableColumn<Residents, LocalDate> colNgaySinh; // fx:id="colNgaySinh"
    @FXML
    private TableColumn<Residents, String> colGioiTinh; // fx:id="colGioiTinh"
    @FXML
    private TableColumn<Residents, String> colCCCD;    // fx:id="colCCCD"
    @FXML
    private TableColumn<Residents, String> colSDT;     // fx:id="colSDT"
    @FXML
    private TableColumn<Residents, Void> colAction;

    private final ObservableList<Residents> residentList = FXCollections.observableArrayList();

    public void initialize(String role, String username, int roomname) {
        this.role = role;
        this.username = username;
        this.roomname = roomname;

        this.role = role;
        this.username = username;

        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
        } else if (Objects.equals(role, "accountant")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Kế toán.");
        }

        nameLabel.setText("Xin chào, " + username);

        colHoTen.setCellValueFactory(new PropertyValueFactory<>("name"));
        colQuanHe.setCellValueFactory(new PropertyValueFactory<>("relationshipToOwner"));
        colNgaySinh.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        colGioiTinh.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colCCCD.setCellValueFactory(new PropertyValueFactory<>("citizenId"));
        colSDT.setCellValueFactory(new PropertyValueFactory<>("phone"));

        double padding = 17;
        tableResidents.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colHoTen.prefWidthProperty().bind(
                tableResidents.widthProperty().subtract(padding).multiply(0.2)
        );
        colQuanHe.prefWidthProperty().bind(
                tableResidents.widthProperty().subtract(padding).multiply(0.12)
        );
        colNgaySinh.prefWidthProperty().bind(
                tableResidents.widthProperty().subtract(padding).multiply(0.15)
        );
        colGioiTinh.prefWidthProperty().bind(
                tableResidents.widthProperty().subtract(padding).multiply(0.12)
        );
        colCCCD.prefWidthProperty().bind(
                tableResidents.widthProperty().subtract(padding).multiply(0.13)
        );
        colSDT.prefWidthProperty().bind(
                tableResidents.widthProperty().subtract(padding).multiply(0.13)
        );
        colAction.prefWidthProperty().bind(
                tableResidents.widthProperty().subtract(padding).multiply(0.15)
        );

        // Định dạng lại ngày sinh
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colNgaySinh.setCellFactory(column -> new TableCell<Residents, LocalDate>() {
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

        loadRoomData();
        addActionButtonsToTable();
    }

    public void loadRoomData() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM rooms WHERE room_number = '" + roomname + "'");

            if (resultSet.next()) {
                String soNha = resultSet.getString("room_number");
                String maCan = resultSet.getString("floor");
                String dienTich = resultSet.getString("area");
                String tinhTrang = resultSet.getString("status");

                // Gán lên các Label trong giao diện
                roomName.setText(soNha);
                roomArea.setText(dienTich + " m");
                roomStatus.setText(tinhTrang.equals("occupied") ? "Đang ở" : "Trống");

                Rooms room = new Rooms(Integer.parseInt(soNha), maCan, dienTich, tinhTrang);

                btnEditRoom.setOnAction(event -> {
                    handleEditRoomDetail(room);
                });

                if (tinhTrang.equals("occupied")) {
                    ownerBox.setVisible(true);
                    loadResidentsData();
                }
            } else {
                System.out.println("Không tìm thấy căn hộ có room_number = 101");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadResidentsData() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM residents WHERE room_number = '" + roomname + "'");

            residentList.clear();
            while (resultSet.next()) {
                String relationshipRaw = resultSet.getString("relationship_to_owner");
                String relationshipDisplay = switch (relationshipRaw) {
                    case "owner" -> "Chủ hộ";
                    case "spouse" -> "Vợ/Chồng";
                    case "parent" -> "Cha/Mẹ";
                    case "child" -> "Con cái";
                    default -> "Khác";
                };

                residentList.add(new Residents(
                        resultSet.getInt("id"),
                        resultSet.getString("full_name"),
                        resultSet.getDate("date_of_birth").toLocalDate(),
                        resultSet.getString("gender").equals("male") ? "Nam" : "Nữ",
                        resultSet.getString("phone"),
                        resultSet.getString("citizen_id"),
                        resultSet.getString("room_number"),
                        relationshipDisplay
                ));

                if (resultSet.getString("relationship_to_owner").equals("owner")) {
                    ownerField.setText(resultSet.getString("full_name"));
                }
            }

            tableResidents.setItems(residentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleCreateResident(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-resident.fxml",
                    "/styles/create-resident.css", owner);

            //  Reload lại bảng
            residentList.clear();
            loadResidentsData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleEditRoomDetail(Rooms room) {
        try {
            Stage owner = StageManager.getPrimaryStage();
            EditRoomController.setRoomToEdit(room);

            SceneNavigator.showPopupScene("/fxml/edit-room.fxml",
                    "/styles/edit-room.css", owner);

            loadRoomData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleEditResidents(Residents residents) {
        try {
            Stage owner = StageManager.getPrimaryStage();

            // Giả định bạn có thể truyền dữ liệu cần sửa qua controller hoặc static variable
            EditResidentController.setResidentToEdit(residents); // Hàm static để tạm giữ dữ liệu

            SceneNavigator.showPopupScene("/fxml/edit-resident.fxml", "/styles/edit-resident.css", owner);

            // Sau khi sửa, làm mới lại bảng dữ liệu:
            residentList.clear();
            loadResidentsData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleDeleteResidents(Residents residents) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn xóa cư dân này?", residents.getName());
        if (result) {
            try {
                Connection connection = DatabaseConnection.getConnection();
                Statement stmt = connection.createStatement();

                String deleteQuery = "DELETE FROM residents WHERE citizen_id = " + residents.getCitizenId();
                System.out.println(residents.getId());
                int rowsAffected = stmt.executeUpdate(deleteQuery);

                if (rowsAffected > 0) {
                    tableResidents.getItems().remove(residents);
                    System.out.println("Đã xóa: " + residents.getName());
                } else {
                    System.out.println("Không tìm thấy cư dân để xóa.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                            handleEditResidents(data);
                        });

                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 40; -fx-font-size: 12");
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

        colAction.setCellFactory(cellFactory);
    }


    //    Header Buton ---------------------------------------------------------
    public void changeToHomePage(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/home-page.fxml"
                , "/styles/home-page.css", event, true);

        HomePageController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToRooms(Event event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/rooms.fxml", "/styles/rooms.css",
                event, true);

        RoomsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToResidents(Event event) throws Exception {
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
