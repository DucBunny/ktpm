package com.app.controllers;

import com.app.models.Residents;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import javafx.collections.FXCollections;
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ResidentsController {
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
    private TableColumn<Residents, String> citizenIDResidents;
    @FXML
    private TableColumn<Residents, String> roomResidents;
    @FXML
    private TableColumn<Residents, String> relationshipResidents;
    @FXML
    private TableColumn<Residents, Void> actionResidents;

    private final ObservableList<Residents> ResidentsList = FXCollections.observableArrayList();

    @FXML
    public void initialize(String role, String username) {
        this.role = role;
        this.username = username;

        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
        } else if (Objects.equals(role, "cashier")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Thu ngân.");
        }

        nameLabel.setText("Xin chào, " + username);

        // Trừ khoảng scroll bar 17px hoặc padding nếu cần
        double padding = 17; // hoặc 0 nếu không cần
        tableResidents.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        nameResidents.prefWidthProperty().bind(tableResidents.widthProperty().subtract(padding).multiply(0.2));
        dateOfBirthResidents.prefWidthProperty().bind(tableResidents.widthProperty().subtract(padding).multiply(0.15));
        genderResidents.prefWidthProperty().bind(tableResidents.widthProperty().subtract(padding).multiply(0.1));
        phoneResidents.prefWidthProperty().bind(tableResidents.widthProperty().subtract(padding).multiply(0.12));
        citizenIDResidents.prefWidthProperty().bind(tableResidents.widthProperty().subtract(padding).multiply(0.12));
        roomResidents.prefWidthProperty().bind(tableResidents.widthProperty().subtract(padding).multiply(0.08));
        relationshipResidents.prefWidthProperty().bind(tableResidents.widthProperty().subtract(padding).multiply(0.08));

        nameResidents.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateOfBirthResidents.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        genderResidents.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneResidents.setCellValueFactory(new PropertyValueFactory<>("phone"));
        citizenIDResidents.setCellValueFactory(new PropertyValueFactory<>("citizenId"));
        roomResidents.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        relationshipResidents.setCellValueFactory(new PropertyValueFactory<>("relationshipToOwner"));

        // Định dạng lại ngày sinh
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        dateOfBirthResidents.setCellFactory(column -> new TableCell<Residents, LocalDate>() {
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

        loadResidentsFromDatabase();
        addActionButtonsToTable();
    }

    //    Header Buton ---------------------------------------------------------
    public void changeToHomePage(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/home-page.fxml"
                , "/styles/home-page.css", event, true);

        HomePageController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToRooms(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/rooms.fxml", "/styles/rooms.css",
                event, true);

        RoomsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToRevenues(ActionEvent event) throws Exception {
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

    //    Body -----------------------------------------------------------------
    public void handleCreateResident() {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-residents.fxml",
                    "/styles/create-residents.css", owner);

            //  Reload lại bảng
            ResidentsList.clear();
            loadResidentsFromDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadResidentsFromDatabase() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM residents ORDER BY room_number ASC");

            while (resultSet.next()) {
                ResidentsList.add(new Residents(
                        resultSet.getInt("id"),
                        resultSet.getString("full_name"),
                        resultSet.getDate("date_of_birth").toLocalDate(),
                        resultSet.getString("gender").equals("male") ? "Nam" : "Nữ",
                        resultSet.getString("phone"),
                        resultSet.getString("citizen_id"),
                        resultSet.getString("room_number"),
                        resultSet.getString("relationship_to_owner").equals("owner") ? "Chủ hộ" : " "
                ));
            }

            tableResidents.setItems(ResidentsList);
        } catch (Exception e) {
            e.printStackTrace();
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
                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            Residents data = getTableView().getItems().get(getIndex());
                            handleEdit(data);
                        });

                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            Residents data = getTableView().getItems().get(getIndex());
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

        actionResidents.setCellFactory(cellFactory);
    }

    private void handleEdit(Residents residents) {
        try {
            Stage owner = StageManager.getPrimaryStage();

            // Giả định bạn có thể truyền dữ liệu cần sửa qua controller hoặc static variable
            EditResidentController.setResidentToEdit(residents); // Hàm static để tạm giữ dữ liệu

            SceneNavigator.showPopupScene("/fxml/edit-resident.fxml", "/styles/edit-resident.css", owner);

            // Sau khi sửa, làm mới lại bảng dữ liệu:
            ResidentsList.clear();
            loadResidentsFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Residents residents) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn xóa cư dân này?", residents.getName());
        if (result) {
            try {
                Connection connection = DatabaseConnection.getConnection();
                Statement stmt = connection.createStatement();

                String deleteQuery = "DELETE FROM residents WHERE id = " + residents.getId();
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
}
