package com.app.controllers.Payments;

import com.app.controllers.HomePageController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Revenues.RevenuesController;
import com.app.controllers.Rooms.RoomsController;
import com.app.models.PaymentStatistics;
import com.app.utils.DatabaseConnection;
import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class CollectionPeriodsController {
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
    private TableView<PaymentStatistics> tablePaymentStatistics;
    @FXML
    private TableColumn<PaymentStatistics, String> name;
    @FXML
    private TableColumn<PaymentStatistics, String> totalAmount;
    @FXML
    private TableColumn<PaymentStatistics, String> description;
    @FXML
    private TableColumn<PaymentStatistics, String> category;
    @FXML
    private TableColumn<PaymentStatistics, String> status;
    @FXML
    private TableColumn<PaymentStatistics, Integer> numberOfPaymentRooms;

    private final ObservableList<PaymentStatistics> PaymentStatisticsList = FXCollections.observableArrayList();

    @FXML
    public void initialize(String role, String username) {
        this.role = role;
        this.username = username;

        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
        } else if (Objects.equals(role, "accountant")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Kế toán.");
        }

        nameLabel.setText("Xin chào, " + username);

        // Trừ khoảng scroll bar 17px hoặc padding nếu cần
        double padding = 17; // hoặc 0 nếu không cần
        tablePaymentStatistics.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        name.prefWidthProperty().bind(tablePaymentStatistics.widthProperty().subtract(padding).multiply(0.2));
        totalAmount.prefWidthProperty().bind(tablePaymentStatistics.widthProperty().subtract(padding).multiply(0.15));
        description.prefWidthProperty().bind(tablePaymentStatistics.widthProperty().subtract(padding).multiply(0.3));
        category.prefWidthProperty().bind(tablePaymentStatistics.widthProperty().subtract(padding).multiply(0.1));
        status.prefWidthProperty().bind(tablePaymentStatistics.widthProperty().subtract(padding).multiply(0.1));
        numberOfPaymentRooms.prefWidthProperty().bind(tablePaymentStatistics.widthProperty().subtract(padding).multiply(0.149));

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        totalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        category.setCellValueFactory(new PropertyValueFactory<>("category"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        numberOfPaymentRooms.setCellValueFactory(new PropertyValueFactory<>("numberOfPaymentRooms"));

        loadPaymentStatisticsFromDatabase();

        // Nháy chuột vào row sẽ mở chi tiết những người đóng
        tablePaymentStatistics.setRowFactory(tv -> {
            TableRow<PaymentStatistics> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 2 && row.getItem().getnumberOfPaymentRooms() != 0) {
                    try {
                        openPaymentDetailScene(row.getItem().getname());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row;
        });
    }

    // Header Button -----------------------------------------------------------
    public void changeToHomePage(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/home-page.fxml"
                , "/styles/home-page.css", event, true);

        HomePageController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToRooms(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Rooms/rooms.fxml", "/styles/Rooms/rooms.css",
                event, true);

        RoomsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToResidents(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Residents/residents.fxml", "/styles/Residents/residents.css",
                event, true);

        ResidentsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToRevenues(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Revenues/revenues.fxml", "/styles/Revenues/revenues.css",
                event, true);

        RevenuesController controller = loader.getController();
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
    private void openPaymentDetailScene(String revenueItemName) throws IOException {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Payments/payment-detail.fxml", "/styles/Payments/payment-detail.css",
                tablePaymentStatistics);

        PaymentDetailController controller = loader.getController();
        controller.initialize(role, username, revenueItemName);
    }

    public void loadPaymentStatisticsFromDatabase() {
        String query = """
                    SELECT
                        ri.name,
                        COALESCE(SUM(p.amount), 0) AS total_amount,
                        ri.description,
                        ri.category,
                        ri.status,
                        COUNT(DISTINCT p.resident_id) AS number_of_payers
                    FROM revenue_items ri
                    LEFT JOIN payments p ON ri.id = p.revenue_item_id
                    GROUP BY ri.id
                    ORDER BY total_amount DESC, ri.name ASC
                """;

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            PaymentStatisticsList.clear();

            while (resultSet.next()) {
                PaymentStatisticsList.add(new PaymentStatistics(
                        resultSet.getString("name"),
                        resultSet.getString("total_amount"),
                        resultSet.getString("description"),
                        resultSet.getString("category").equals("mandatory") ? "Bắt buộc" : "Tự nguyện",
                        resultSet.getString("status").equals("active") ? "Mở" : "Đóng",
                        resultSet.getInt("number_of_payers")
                ));
            }

            tablePaymentStatistics.setItems(PaymentStatisticsList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}