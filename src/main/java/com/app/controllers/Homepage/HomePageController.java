package com.app.controllers.Homepage;

import com.app.controllers.HeaderUtils.HeaderController;
import com.app.controllers.Payments.CollectionPeriods.CollectionPeriodsController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Rooms.RoomsController;
import com.app.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class HomePageController {
    //    Header
    @FXML
    private VBox headerPane; // Tiêm nút gốc của header.fxml
    @FXML
    private HeaderController headerPaneController; // Tiêm controller của header.fxml

    //    Body
    @FXML
    private AnchorPane mainContentPane;
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

    //  Footer
    @FXML
    private HBox footerBar;

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

    public void initialize(String role) throws SQLException, IOException {
        if (StageManager.getPrimaryStage().getScene() != null) {
            StageManager.getPrimaryStage().getScene().getRoot().getProperties().put("controller", this);
        }

        if (Objects.equals(role, "admin")) {
            footerBar.setVisible(false);
            footerBar.setManaged(false);
            AnchorPane.setBottomAnchor(mainContentPane, 0.0);
        } else if (Objects.equals(role, "accountant")) {
        }

        initMonthlyRevenueChart();
        initQuarterlyRevenueChart();
        setValueToItem();
    }

    // Body --------------------------------------------------------------------
    public void changeToRooms(Event event) throws Exception {
        UserSession.setCurrentPage("rooms");
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Rooms/rooms.fxml", "/styles/Rooms/rooms.css",
                event, true);

        RoomsController controller = loader.getController();
        controller.initialize(UserSession.getRole());
        controller.initializeHeader();
    }

    public void changeToResidents(Event event) throws Exception {
        UserSession.setCurrentPage("residents");
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Residents/residents.fxml", "/styles/Residents/residents.css",
                event, true);

        ResidentsController controller = loader.getController();
        controller.initialize(UserSession.getRole());
        controller.initializeHeader();
    }

    public void changeToPayments(Event event) throws Exception {
        UserSession.setCurrentPage("payments");
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Payments/CollectionPeriods/collection-periods.fxml", "/styles/Payments/CollectionPeriods/collection-periods.css",
                event, true);

        CollectionPeriodsController controller = loader.getController();
        controller.initialize(UserSession.getRole());
        controller.initializeHeader();
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
                        SELECT COALESCE(SUM(p.paid_amount), 0) AS total_revenue
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

    public void initMonthlyRevenueChart() throws IOException {
        XYChart.Series<String, Number> seriesPaid = new XYChart.Series<>();
        XYChart.Series<String, Number> seriesTotal = new XYChart.Series<>();

        seriesTotal.setName("Tổng số tiền phải thu");
        seriesPaid.setName("Số tiền đã thu");

        ObservableList<String> categories = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = """
                    SELECT
                        cp.name,
                        COALESCE(SUM(ci.total_amount), 0) AS total_amount,
                        COALESCE((
                            SELECT SUM(p.paid_amount)
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
                        cp.start_date ASC;
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
            chart1.getData().addAll(seriesPaid, seriesTotal);
            chart1.setCategoryGap(10);
            chart1.setBarGap(2);
            chart1.setAnimated(false);

            for (XYChart.Series<String, Number> series : chart1.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            Tooltip tooltip = new Tooltip(series.getName() + ": " + String.format("%,d", data.getYValue().intValue()));
                            Tooltip.install(newNode, tooltip);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showErrorAlert("Không thể tải dữ liệu doanh thu tháng từ CSDL.");
        }
    }

    public void initQuarterlyRevenueChart() throws IOException {
        XYChart.Series<String, Number> seriesPaid = new XYChart.Series<>();
        XYChart.Series<String, Number> seriesTotal = new XYChart.Series<>();

        seriesTotal.setName("Tổng số tiền phải thu");
        seriesPaid.setName("Số tiền đã thu");

        ObservableList<String> categories = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = """
                    SELECT
                        cp.name,
                        COALESCE(SUM(ci.total_amount), 0) AS total_amount,
                        COALESCE((
                            SELECT SUM(p.paid_amount)
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
                        cp.start_date ASC;
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
            chart2.getData().addAll(seriesPaid, seriesTotal);
            chart2.setCategoryGap(10);
            chart2.setBarGap(2);
            chart2.setAnimated(false);

            for (XYChart.Series<String, Number> series : chart2.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Tooltip tooltip = new Tooltip(series.getName() + ": " + String.format("%,d", data.getYValue().intValue()));
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showErrorAlert("Không thể tải dữ liệu doanh thu quý từ CSDL.");
        }
    }

    // Footer Button -----------------------------------------------------------
    public void changeToCreatePayments() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/Payments/PaymentDetail/create-payment.fxml", "/styles/Payments/crud-payment.css", owner);
    }

    public void changeToCreateRevenues() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/Revenues/create-revenue.fxml", "/styles/Revenues/crud-revenue.css", owner);
    }

    public void exportReport() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/Homepage/export-report.fxml", "/styles/Payments/crud-payment.css", owner);
    }
}
