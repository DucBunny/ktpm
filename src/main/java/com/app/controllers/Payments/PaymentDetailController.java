package com.app.controllers.Payments;

import com.app.controllers.HomePageController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Revenues.RevenuesController;
import com.app.controllers.Rooms.RoomsController;
import com.app.models.PaymentDetail;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class PaymentDetailController {
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
    private TableView<PaymentDetail> tablePaymentDetail;
    @FXML
    private TableColumn<PaymentDetail, String> nameResident;
    @FXML
    private TableColumn<PaymentDetail, String> amount;
    @FXML
    private TableColumn<PaymentDetail, String> nameRevenue;
    @FXML
    private TableColumn<PaymentDetail, String> note;
    @FXML
    private TableColumn<PaymentDetail, LocalDate> date;
    @FXML
    private TableColumn<PaymentDetail, String> roomNumber;
    @FXML
    private TableColumn<PaymentDetail, Void> actionPaymentDetail;

    private final ObservableList<PaymentDetail> PaymentDetailList = FXCollections.observableArrayList();

    @FXML
    public void initialize(String role, String username, String nameRevenueItem) {
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
        tablePaymentDetail.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        nameResident.prefWidthProperty().bind(tablePaymentDetail.widthProperty().subtract(padding).multiply(0.15));
        amount.prefWidthProperty().bind(tablePaymentDetail.widthProperty().subtract(padding).multiply(0.15));
        nameRevenue.prefWidthProperty().bind(tablePaymentDetail.widthProperty().subtract(padding).multiply(0.15));
        note.prefWidthProperty().bind(tablePaymentDetail.widthProperty().subtract(padding).multiply(0.2));
        date.prefWidthProperty().bind(tablePaymentDetail.widthProperty().subtract(padding).multiply(0.1));
        roomNumber.prefWidthProperty().bind(tablePaymentDetail.widthProperty().subtract(padding).multiply(0.1));

        nameResident.setCellValueFactory(new PropertyValueFactory<>("residentName"));
        amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        nameRevenue.setCellValueFactory(new PropertyValueFactory<>("revenueItem"));
        note.setCellValueFactory(new PropertyValueFactory<>("note"));
        date.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        roomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        // Định dạng lại ngày
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        date.setCellFactory(column -> new TableCell<PaymentDetail, LocalDate>() {
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

        loadPaymentDetailByRevenueItem(nameRevenueItem);
        addActionButtonsToTable();
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

    public void changeToPayments(ActionEvent event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Payments/payments.fxml", "/styles/Payments/payments.css",
                event, true);

        PaymentsController controller = loader.getController();
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
    public void loadPaymentDetailByRevenueItem(String nameRevenueItem) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = """
                        SELECT
                            p.id,
                            r.full_name AS resident_name,
                            p.amount,
                            ri.name AS revenue_item,
                            p.note,
                            p.payment_date,
                            rm.room_number
                        FROM payments p
                        JOIN residents r ON p.resident_id = r.id
                        JOIN revenue_items ri ON p.revenue_item_id = ri.id
                        JOIN rooms rm ON p.room_number = rm.room_number
                        WHERE ri.name = ?
                        ORDER BY p.payment_date DESC, p.amount DESC
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, nameRevenueItem);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                PaymentDetailList.add(new PaymentDetail(
                        resultSet.getInt("id"),
                        resultSet.getString("resident_name"),
                        resultSet.getString("amount"),
                        resultSet.getString("revenue_item"),
                        resultSet.getString("note"),
                        resultSet.getDate("payment_date").toLocalDate(),
                        resultSet.getString("room_number")
                ));
            }

            tablePaymentDetail.setItems(PaymentDetailList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<PaymentDetail, Void>, TableCell<PaymentDetail, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<PaymentDetail, Void> call(final TableColumn<PaymentDetail, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");

                    {
                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            PaymentDetail data = getTableView().getItems().get(getIndex());
                            handleEdit(data);
                        });

                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            PaymentDetail data = getTableView().getItems().get(getIndex());
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

        actionPaymentDetail.setCellFactory(cellFactory);
    }

    private void handleEdit(PaymentDetail paymentDetail) {
        try {
            // Tạo stage mới hoặc sử dụng SceneNavigator để mở form sửa
            Stage owner = StageManager.getPrimaryStage();

            // Giả định bạn có thể truyền dữ liệu cần sửa qua controller hoặc static variable
            EditPaymentController.setPaymentToEdit(paymentDetail); // Hàm static để tạm giữ dữ liệu

            SceneNavigator.showPopupScene("/fxml/Payments/edit-payment.fxml", "/styles/Payments/edit-payment.css", owner);

            // Sau khi sửa, làm mới lại bảng dữ liệu:
            PaymentDetailList.clear();
            loadPaymentDetailByRevenueItem(paymentDetail.getRevenueItem());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(PaymentDetail paymentDetail) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn xóa thu phí này?", paymentDetail.getResidentName());
        if (result) {
            try {
                Connection connection = DatabaseConnection.getConnection();

                String sql = "DELETE FROM payments WHERE id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, paymentDetail.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    tablePaymentDetail.getItems().remove(paymentDetail);
                    System.out.println("Đã xóa: " + paymentDetail.getResidentName());
                } else {
                    System.out.println("Không tìm thấy thu phí để xóa.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
