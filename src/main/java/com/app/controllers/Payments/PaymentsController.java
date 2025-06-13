package com.app.controllers.Payments;

import com.app.controllers.HomePageController;
import com.app.controllers.Payments.CollectionPeriods.CollectionPeriodsController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Revenues.RevenuesController;
import com.app.controllers.Rooms.RoomsController;
import com.app.models.Payments;
import com.app.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import java.util.Objects;

public class PaymentsController {
    private String role;
    private String username;
    private int collectionPeriodId;
    private String collectionPeriodName;
    private double elasticity;      // co giãn (nếu ẩn cột)

    //    Header
    @FXML
    private Label roleLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private MenuItem MenuItem_SignUp;

    @FXML
    private Label contentLabel;

    @FXML
    private Button btnCreate;
    //    Body
    @FXML
    private TableView<Payments> tablePayments;
    @FXML
    private TableColumn<Payments, String> roomNumberPayments;
    @FXML
    private TableColumn<Payments, String> amountPayments;
    @FXML
    private TableColumn<Payments, LocalDate> datePayments;
    @FXML
    private TableColumn<Payments, String> notePayments;
    @FXML
    private TableColumn<Payments, String> statusPayments;
    @FXML
    private TableColumn<Payments, Void> actionPayments;

    private final ObservableList<Payments> paymentsList = FXCollections.observableArrayList();

    @FXML
    public void initialize(String role, String username, int collectionPeriodId, String collectionPeriodName) {
        this.role = role;
        this.username = username;
        this.collectionPeriodId = collectionPeriodId;
        this.collectionPeriodName = collectionPeriodName;

        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
            elasticity = (double) 10 / 9;       // ẩn cột action 10%
        } else if (Objects.equals(role, "accountant")) {
            btnCreate.setVisible(true);
            actionPayments.setVisible(true);
            roleLabel.setText("Bạn đang đăng nhập với quyền Kế toán.");
            elasticity = 1;
        }

        nameLabel.setText("Xin chào");
        contentLabel.setText("CÁC HỘ ĐÃ ĐÓNG " + collectionPeriodName.toUpperCase());

        tablePayments.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Thiết lập lắng nghe khi có thay đổi kích thước hoặc dữ liệu
        tablePayments.widthProperty().addListener((obs, oldVal, newVal) -> adjustColumnWidths());
        paymentsList.addListener((ListChangeListener<Payments>) c -> adjustColumnWidths());

        roomNumberPayments.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        amountPayments.setCellValueFactory(new PropertyValueFactory<>("amount"));
        datePayments.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        notePayments.setCellValueFactory(new PropertyValueFactory<>("note"));
        statusPayments.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Định dạng lại ngày thu
        datePayments.setCellFactory(DateFormat.forLocalDate());

        loadPaymentsFromDatabase();
        addActionButtonsToTable();
    }

    private void adjustColumnWidths() {
        // Lấy số dòng dữ liệu (item count)
        int rowCount = paymentsList.size();
        // Lấy chiều cao 1 hàng
        double rowHeight = 40;
        // Trừ chiều cao header
        double headerHeight = 50;
        // Tổng chiều cao dữ liệu
        double totalRowsHeight = rowCount * rowHeight;
        // Chiều cao hiển thị thực tế
        double tableContentHeight = tablePayments.getHeight() - headerHeight;

        // Nếu tổng chiều cao dữ liệu > chiều cao table -> có scroll bar
        double padding = (totalRowsHeight > tableContentHeight) ? 18 : 0;
        double tableWidth = tablePayments.getWidth() - padding;

        roomNumberPayments.setPrefWidth(tableWidth * 0.15 * elasticity);
        amountPayments.setPrefWidth(tableWidth * 0.15 * elasticity);
        datePayments.setPrefWidth(tableWidth * 0.15 * elasticity);
        notePayments.setPrefWidth(tableWidth * 0.3 * elasticity);
        statusPayments.setPrefWidth(tableWidth * 0.15 * elasticity);
        actionPayments.setPrefWidth(tableWidth * 0.1 * elasticity);
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
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Payments/CollectionPeriods/collection-periods.fxml", "/styles/Payments/CollectionPeriods/collection-periods.css",
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

    // Body --------------------------------------------------------------------
    public void handleCreatePayment() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/Payments/create-payment.fxml", "/styles/Payments/crud-payment.css", owner);
        CreatePaymentController controller = loader.getController();
        controller.setPreselectedPeriod(collectionPeriodName);
        controller.applyPreselectedPeriod();

        //  Reload lại bảng
        controller.setCallback(() -> {
            // Chỉ reload khi thực sự thêm mới thành công!
            paymentsList.clear();
            loadPaymentsFromDatabase();
        });
    }

    public void loadPaymentsFromDatabase() {
        paymentsList.clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = """
                        SELECT
                          p.id,
                          ci.room_number,
                          cp.name AS collection_period_name,
                          COALESCE(SUM(ci.total_amount), 0) AS amount,
                          p.payment_date,
                          p.note,
                          r.floor,
                          CASE
                            WHEN p.id IS NOT NULL THEN 'paid'
                            ELSE 'unpaid'
                          END AS status
                        FROM collection_items ci
                        LEFT JOIN payments p ON ci.room_number = p.room_number AND ci.collection_period_id = p.collection_period_id
                        JOIN rooms r ON ci.room_number = r.room_number
                        JOIN collection_periods cp ON ci.collection_period_id = cp.id
                        WHERE ci.collection_period_id = ?
                        GROUP BY p.id, ci.room_number, p.payment_date, p.note, ci.collection_period_id, r.floor
                        ORDER BY status DESC, r.floor ASC, ci.room_number ASC;
                    """;
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, collectionPeriodId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Payments payment = new Payments(
                        resultSet.getInt("id"),
                        "Phòng " + resultSet.getString("room_number"),
                        resultSet.getString("amount"),
                        resultSet.getDate("payment_date") != null ? resultSet.getDate("payment_date").toLocalDate() : null,
                        resultSet.getString("note"),
                        resultSet.getString("status").equals("unpaid") ? "Chưa đóng" : "Đã đóng"
                );

                payment.setCollectionPeriod(resultSet.getString("collection_period_name"));

                paymentsList.add(payment);
            }

            tablePayments.setItems(paymentsList);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CustomAlert.showErrorAlert("Không thể tải dữ liệu thanh toán từ CSDL.");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<Payments, Void>, TableCell<Payments, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Payments, Void> call(final TableColumn<Payments, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnCancel = new Button("Hủy");

                    {
                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            Payments data = getTableView().getItems().get(getIndex());
                            try {
                                handleEdit(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnCancel.setOnAction((ActionEvent event) -> {
                            Payments data = getTableView().getItems().get(getIndex());
                            try {
                                handleCancel(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                    private final HBox pane = new HBox(10, btnEdit, btnCancel);

                    {
                        pane.setAlignment(Pos.CENTER);
                    }

                    // Chỉ hiển thị 2 nút khi trạng thái "Đã đóng tiền"
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Payments payment = getTableView().getItems().get(getIndex());
                            if (payment != null && "Đã đóng".equals(payment.getStatus())) {
                                setGraphic(pane);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };

        actionPayments.setCellFactory(cellFactory);
    }

    private void handleEdit(Payments payment) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        EditPaymentController.setPaymentToEdit(payment); // Hàm static để tạm giữ dữ liệu
        SceneNavigator.showPopupScene("/fxml/Payments/edit-payment.fxml", "/styles/Payments/crud-payment.css", owner);

        // Sau khi sửa, làm mới lại bảng dữ liệu:
        paymentsList.clear();
        loadPaymentsFromDatabase();
    }

    private void handleCancel(Payments payment) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn hủy thanh toán này?", payment.getRoomNumber() + " - Đợt thu " + collectionPeriodName);
        if (result) {
            try {
                Connection connection = DatabaseConnection.getConnection();
                String sql = "DELETE FROM payments WHERE room_number = ? AND collection_period_id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, payment.getRoomNumber().replace("Phòng ", ""));
                stmt.setInt(2, collectionPeriodId);

                if (stmt.executeUpdate() > 0) {
                    System.out.println("Chuyển trạng thái sang chưa đóng: " + payment.getRoomNumber());
                    CustomAlert.showSuccessAlert("Đã chuyển trạng thái sang chưa đóng", true, 0.7);
                    loadPaymentsFromDatabase();
                } else {
                    CustomAlert.showErrorAlert("Không tìm thấy thanh toán để chuyển trạng thái.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void changeToPassword(ActionEvent event) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/change-password.fxml", "/styles/change-password.css", owner);
    }
}