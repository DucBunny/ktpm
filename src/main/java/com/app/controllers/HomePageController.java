package com.app.controllers;

import com.app.controllers.Payments.CollectionPeriods.CollectionPeriodsController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Revenues.RevenuesController;
import com.app.controllers.Rooms.RoomsController;
import com.app.models.CollectionPeriods;
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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class HomePageController {
    private String role;
    private String username;

    @FXML
    private Label roleLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private MenuItem MenuItem_SignUp;
    @FXML
    private AnchorPane mainContentPane;
    @FXML
    private HBox footerBar;
    @FXML
    private BarChart<String, Number> chart;

    public void initialize(String role, String username) throws IOException {
        this.role = role;
        this.username = username;

        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
            footerBar.setVisible(false);
            footerBar.setManaged(false);
            AnchorPane.setBottomAnchor(mainContentPane, 0.0);
        } else if (Objects.equals(role, "accountant")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Kế toán.");
        }

        nameLabel.setText("Xin chào, " + username);
        initBarChart();
    }

    // Header Button -----------------------------------------------------------
    public void changeToRooms(Event event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Rooms/rooms.fxml", "/styles/Rooms/rooms.css",
                event, true);

        RoomsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToResidents(Event event) throws Exception {
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

    // Footer Button -----------------------------------------------------------
    public void changeToCreatePayments() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/Payments/create-payment.fxml", "/styles/Payments/create-payment.css", owner);
    }

    public void changeToCreateRevenues() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/Revenues/create-revenue.fxml", "/styles/Revenues/crud-revenue.css", owner);
    }

    public void initBarChart() throws IOException {

        // Khởi tạo 2 series
        XYChart.Series<String, Number> seriesTotal = new XYChart.Series<>();
        XYChart.Series<String, Number> seriesPaid = new XYChart.Series<>();

        seriesTotal.setName("Tổng số tiền");
        seriesPaid.setName("Số tiền đã đóng");

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = """
            SELECT
                cp.name,
                COALESCE(SUM(ci.total_amount), 0) AS total_amount,
                COALESCE((
                    SELECT SUM(p.amount)
                    FROM payments p
                    WHERE p.collection_period_id = cp.id
                ), 0) AS total_paid_amount
            FROM
                collection_periods cp
            LEFT JOIN
                collection_items ci ON cp.id = ci.collection_period_id
            GROUP BY
                cp.id, cp.name, cp.start_date, cp.end_date, cp.type
            ORDER BY
                cp.start_date DESC;
        """;

            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int totalAmount = resultSet.getInt("total_amount");
                int totalPaidAmount = resultSet.getInt("total_paid_amount");
                seriesTotal.getData().add(new XYChart.Data<>(name, totalAmount));
                seriesPaid.getData().add(new XYChart.Data<>(name, totalPaidAmount));
            }

            chart.getData().clear();
            chart.getData().setAll(seriesTotal, seriesPaid);


            // Cài đặt khoảng cách giữa các cột
            chart.setCategoryGap(10);
            chart.setBarGap(2);

        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showErrorAlert("Không thể tải dữ liệu đợt thu từ CSDL.");
        }
    }


}
