package com.app.controllers;

import com.app.models.Revenues;
import com.app.models.User;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class RevenuesController {
    @FXML
    private TableView<Revenues> tableRevenues;
    @FXML
    private TableColumn<Revenues, Integer> idRevenues;
    @FXML
    private TableColumn<Revenues, String> nameRevenues;
    @FXML
    private TableColumn<Revenues, String> createAtRevenues;
    @FXML
    private TableColumn<Revenues, String> statusRevenues;
    @FXML
    private TableColumn<Revenues, String> valueRevenues;
    @FXML
    private TableColumn<Revenues, String> descriptionRevenues;

    @FXML
    public void initialize() {
        // Trừ khoảng scroll bar 17px hoặc padding nếu cần
        double padding = 17; // hoặc 0 nếu không cần
        tableRevenues.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.05));
        nameRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.2));
        createAtRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.1));
        statusRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.15));
        valueRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.1));
        descriptionRevenues.prefWidthProperty().bind(tableRevenues.widthProperty().subtract(padding).multiply(0.3));


        idRevenues.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameRevenues.setCellValueFactory(new PropertyValueFactory<>("name"));
        createAtRevenues.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        statusRevenues.setCellValueFactory(new PropertyValueFactory<>("status"));
        valueRevenues.setCellValueFactory(new PropertyValueFactory<>("value"));
        descriptionRevenues.setCellValueFactory(new PropertyValueFactory<>("description"));

        loadRevenuesFromDatabase();
    }

    public void loadRevenuesFromDatabase() {
//        Lay data tu database
//        Connection connection = DatabaseConnection.getConnection();
//        Statement statement = connection.createStatement();
//        ResultSet resultSet = statement.executeQuery("SELECT * FROM revenues");
        ObservableList<Revenues> data = FXCollections.observableArrayList(
                new Revenues(1, "Hóa đơn tháng 1", "2025-01-10", "Đã thanh toán", "2,000,000", "Thu học phí tháng 1"),
                new Revenues(2, "Hóa đơn tháng 2", "2025-02-10", "Chưa thanh toán", "2,000,000", "Thu học phí tháng 2"),
                new Revenues(3, "Dịch vụ PT", "2025-03-05", "Đã thanh toán", "1,000,000", "Phí huấn luyện viên cá nhân")
        );
        tableRevenues.setItems(data);
    }
}
