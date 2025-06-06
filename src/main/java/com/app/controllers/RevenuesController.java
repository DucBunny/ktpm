package com.app.controllers;

import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Rooms.RoomsController;
import com.app.models.Revenues;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class RevenuesController {
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
    private Button btnCreate;

    @FXML
    private TableView<Revenues> tableRevenues;
    @FXML
    private TableColumn<Revenues, String> nameRevenues;
    @FXML
    private TableColumn<Revenues, String> valueRevenues;
    @FXML
    private TableColumn<Revenues, String> descriptionRevenues;
    @FXML
    private TableColumn<Revenues, String> categoryRevenues;
    @FXML
    private TableColumn<Revenues, String> statusRevenues;
    @FXML
    private TableColumn<Revenues, Void> actionRevenues;

    private final ObservableList<Revenues> revenuesList = FXCollections.observableArrayList();

    @FXML
    public void initialize(String role, String username) throws IOException {
        this.role = role;
        this.username = username;

        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
        } else if (Objects.equals(role, "accountant")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Kế toán.");
            btnCreate.setVisible(true);
            actionRevenues.setVisible(true);

            addActionButtonsToTable();
        }

        nameLabel.setText("Xin chào, " + username);

        // Trừ khoảng scroll bar 17px hoặc padding nếu cần
        double padding = 17; // hoặc 0 nếu không cần
        tableRevenues.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        nameRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.2));
        valueRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.15));
        descriptionRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.3));
        categoryRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.1));
        statusRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.1));

        nameRevenues.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueRevenues.setCellValueFactory(new PropertyValueFactory<>("value"));
        descriptionRevenues.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryRevenues.setCellValueFactory(new PropertyValueFactory<>("category"));
        statusRevenues.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadRevenuesFromDatabase();
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

    public void changeToResidents(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/residents.fxml", "/styles/residents.css",
                event, true);

        ResidentsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToPayments(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/payments.fxml", "/styles/payments.css",
                event, true);

        PaymentsController controller = loader.getController();
        controller.initialize(role, username);
    }

    //  Pop-up Button Cài đặt --------------------------------------------------
    public void changeToSignUp() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/create-account.fxml",
                "/styles/sign-in-create-account.css", owner);
    }

    public void changeToSignIn(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/sign-in.fxml", "/styles/sign-in-create-account.css",
                event, false);
    }

    //    Body -----------------------------------------------------------------
    public void handleCreateRevenue() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/create-revenue.fxml", "/styles/create-revenue.css", owner);

        //  Reload lại bảng
        revenuesList.clear();
        loadRevenuesFromDatabase();
    }

    public void loadRevenuesFromDatabase() throws IOException {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT * FROM revenue_items ORDER BY unit_price DESC";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                revenuesList.add(new Revenues(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("unit_price"),
                        resultSet.getString("description"),
                        resultSet.getString("category").equals("mandatory") ? "Bắt buộc" : "Tự nguyện",
                        resultSet.getString("status").equals("active") ? "Mở" : "Đóng"
                ));
            }

            tableRevenues.setItems(revenuesList);
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showErrorAlert("Không thể tải dữ liệu khoản thu từ CSDL.");
        }
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<Revenues, Void>, TableCell<Revenues, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Revenues, Void> call(final TableColumn<Revenues, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");

                    {
                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            Revenues data = getTableView().getItems().get(getIndex());
                            try {
                                handleEdit(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            Revenues data = getTableView().getItems().get(getIndex());
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

        actionRevenues.setCellFactory(cellFactory);
    }

    private void handleEdit(Revenues revenues) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        EditRevenueController.setRevenueToEdit(revenues); // Hàm static để tạm giữ dữ liệu
        SceneNavigator.showPopupScene("/fxml/edit-revenue.fxml", "/styles/edit-revenue.css", owner);

        // Sau khi sửa, làm mới lại bảng dữ liệu
        revenuesList.clear();
        loadRevenuesFromDatabase();
    }

    private void handleDelete(Revenues revenues) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn xóa khoản thu này?", revenues.getName());
        if (result) {
            try {
                Connection connection = DatabaseConnection.getConnection();
                String sql = "DELETE FROM revenue_items WHERE id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, revenues.getId());

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    tableRevenues.getItems().remove(revenues);
                    System.out.println("Đã xóa: " + revenues.getName());
                } else {
                    System.out.println("Không tìm thấy khoản thu để xóa.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
