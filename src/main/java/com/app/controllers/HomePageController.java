package com.app.controllers;

import com.app.controllers.Payments.CollectionPeriods.CollectionPeriodsController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Revenues.RevenuesController;
import com.app.controllers.Rooms.RoomsController;
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
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
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
    private BarChart<String, Number> chart1;
    @FXML
    private BarChart<String, Number> chart2;

    @FXML
    private Label labelTotalRooms;

    @FXML
    private Label labelTotalResidents;

    @FXML
    private Label labelTotalRevenues;


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
        initMonthlyRevenueChart();
        initQuarterlyRevenueChart();
        setValueToItem();
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

    public void initMonthlyRevenueChart() throws IOException {
        XYChart.Series<String, Number> seriesTotal = new XYChart.Series<>();
        XYChart.Series<String, Number> seriesPaid = new XYChart.Series<>();

        seriesTotal.setName("Tổng số tiền");
        seriesPaid.setName("Số tiền đã đóng");

        ObservableList<String> categories = FXCollections.observableArrayList();

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
            WHERE cp.type = 'monthly'
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
                categories.add(name);
            }

            chart1.getData().clear();
            chart1.getData().addAll(seriesTotal, seriesPaid);
            chart1.setCategoryGap(10);
            chart1.setBarGap(2);
            chart1.setAnimated(false);

        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showErrorAlert("Không thể tải dữ liệu doanh thu tháng từ CSDL.");
        }
    }


    public void initQuarterlyRevenueChart() throws IOException {
        XYChart.Series<String, Number> seriesTotal = new XYChart.Series<>();
        XYChart.Series<String, Number> seriesPaid = new XYChart.Series<>();

        seriesTotal.setName("Tổng số tiền");
        seriesPaid.setName("Số tiền đã đóng");

        ObservableList<String> categories = FXCollections.observableArrayList();

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
            WHERE cp.type = 'quarterly'
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
                categories.add(name);
            }

            chart2.getData().clear();
            chart2.getData().addAll(seriesTotal, seriesPaid);
            chart2.setCategoryGap(10);
            chart2.setBarGap(2);
            chart2.setAnimated(false);

        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showErrorAlert("Không thể tải dữ liệu doanh thu quý từ CSDL.");
        }
    }


    public void setValueToItem() throws IOException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Lấy tổng số phòng
            String queryRooms = "SELECT COUNT(*) AS total_rooms FROM rooms";
            PreparedStatement stmtRooms = connection.prepareStatement(queryRooms);
            ResultSet rsRooms = stmtRooms.executeQuery();
            if (rsRooms.next()) {
                int totalRooms = rsRooms.getInt("total_rooms");
                labelTotalRooms.setText(String.valueOf(totalRooms));
            }

            // Lấy tổng số cư dân
            String queryResidents = "SELECT COUNT(*) AS total_residents FROM residents";
            PreparedStatement stmtResidents = connection.prepareStatement(queryResidents);
            ResultSet rsResidents = stmtResidents.executeQuery();
            if (rsResidents.next()) {
                int totalResidents = rsResidents.getInt("total_residents");
                labelTotalResidents.setText(String.valueOf(totalResidents));
            }

            // Lấy tổng doanh thu
            String queryRevenues = """
            SELECT COALESCE(SUM(p.amount), 0) AS total_revenue
            FROM payments p
        """;
            PreparedStatement stmtRevenues = connection.prepareStatement(queryRevenues);
            ResultSet rsRevenues = stmtRevenues.executeQuery();
            if (rsRevenues.next()) {
                int totalRevenues = rsRevenues.getInt("total_revenue");
                // Chuyển sang đơn vị 'k'
                String revenueText = String.format("%,d k", totalRevenues / 1000);
                labelTotalRevenues.setText(revenueText);
            }

        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showErrorAlert("Không thể tải dữ liệu dashboard từ CSDL.");
        }
    }


}
