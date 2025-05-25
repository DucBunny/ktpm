package com.app.controllers;

import com.app.models.Revenues;
import com.app.models.User;
import com.app.utils.DatabaseConnection;
import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

public class RevenuesController {
    @FXML
    private TableView<Revenues> tableRevenues;
    @FXML
    private TableColumn<Revenues, Integer> idRevenues;
    @FXML
    private TableColumn<Revenues, String> nameRevenues;
    @FXML
    private TableColumn<Revenues, String> statusRevenues;
    @FXML
    private TableColumn<Revenues, String> valueRevenues;
    @FXML
    private TableColumn<Revenues, String> descriptionRevenues;
    @FXML
    private TableColumn<Revenues, String> categoryRevenues;

    @FXML
    private TableColumn<Revenues, Void> actionRevenues;

    private ObservableList<Revenues> revenuesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Trừ khoảng scroll bar 17px hoặc padding nếu cần
        double padding = 17; // hoặc 0 nếu không cần
        tableRevenues.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.05));
        nameRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.2));
        statusRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.15));
        valueRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.1));
        descriptionRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.3));
        categoryRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.1));

        idRevenues.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameRevenues.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusRevenues.setCellValueFactory(new PropertyValueFactory<>("status"));
        valueRevenues.setCellValueFactory(new PropertyValueFactory<>("value"));
        descriptionRevenues.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryRevenues.setCellValueFactory(new PropertyValueFactory<>("category"));

        loadRevenuesFromDatabase();
        addActionButtonsToTable();
    }

    public void loadRevenuesFromDatabase() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM revenue_items");

            while (resultSet.next()) {
                revenuesList.add(new Revenues(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("status").equals("active") ? "Mở" : "Đóng",
                        resultSet.getString("unit_price"),
                        resultSet.getString("description"),
                        resultSet.getString("category").equals("mandatory") ? "Bắt buộc" : "Tự nguyện"
                ));
            }

            tableRevenues.setItems(revenuesList);

        } catch (Exception e) {
            e.printStackTrace();
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
                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            Revenues data = getTableView().getItems().get(getIndex());
                            handleEdit(data);
                        });

                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5;");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            Revenues data = getTableView().getItems().get(getIndex());
                            handleDelete(data);
                        });
                    }

                    private final HBox pane = new HBox(10, btnEdit, btnDelete);

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

    private void handleEdit(Revenues revenues) {
        try {
            // Tạo stage mới hoặc sử dụng SceneNavigator để mở form sửa
            Stage owner = StageManager.getPrimaryStage();

            // Giả định bạn có thể truyền dữ liệu cần sửa qua controller hoặc static variable
            EditRevenueController.setRevenueToEdit(revenues); // Hàm static để tạm giữ dữ liệu

            SceneNavigator.showPopupScene("/fxml/formUpdateRevenue.fxml", "/styles/form-update-revenue.css", false, owner);

            // Sau khi sửa, làm mới lại bảng dữ liệu:
            revenuesList.clear();
            loadRevenuesFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleDelete(Revenues revenues) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa khoản thu");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa khoản thu này?");
        alert.setContentText(revenues.getName());

        // Áp dụng CSS tùy chỉnh
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/delete-revenue.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                String deleteQuery = "DELETE FROM revenue_items WHERE id = " + revenues.getId();
                int rowsAffected = stmt.executeUpdate(deleteQuery);

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


    public void changeToSignIn(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/sign-in.fxml", "/styles/sign-in-sign-up.css",
                event, false, false);
    }

    public void changeToSignUp(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/sign-up.fxml",
                    "/styles/sign-in-sign-up.css", false, owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleAdd(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-revenues.fxml",
                    "/styles/create-revenues.css", false, owner);

        //  Reload lại bảng
            revenuesList.clear();
            loadRevenuesFromDatabase();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToCreateFees(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-revenues.fxml",
                    "/styles/create-revenues.css", false, owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
