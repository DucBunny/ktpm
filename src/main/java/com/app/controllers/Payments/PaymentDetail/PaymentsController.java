package com.app.controllers.Payments.PaymentDetail;

import com.app.controllers.HeaderUtils.HeaderController;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class PaymentsController {
    private int collectionPeriodId;
    private String collectionPeriodName;
    private double elasticity;      // co giãn (nếu ẩn cột)

    //    Header
    @FXML
    private VBox headerPane; // Tiêm nút gốc của header.fxml
    @FXML
    private HeaderController headerPaneController; // Tiêm controller của header.fxml

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
    private TableColumn<Payments, String> totalAmountPayments;
    @FXML
    private TableColumn<Payments, String> paidAmountPayments;
    @FXML
    private TableColumn<Payments, LocalDate> datePayments;
    @FXML
    private TableColumn<Payments, String> notePayments;
    @FXML
    private TableColumn<Payments, String> statusPayments;
    @FXML
    private TableColumn<Payments, Void> actionPayments;

    private final ObservableList<Payments> paymentsList = FXCollections.observableArrayList();

    public void setCollectionPeriod(int collectionPeriodId, String collectionPeriodName) {
        this.collectionPeriodId = collectionPeriodId;
        this.collectionPeriodName = collectionPeriodName;
    }

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

    @FXML
    public void initialize(String role) {
        if (StageManager.getPrimaryStage().getScene() != null) {
            StageManager.getPrimaryStage().getScene().getRoot().getProperties().put("controller", this);
        }

        if (Objects.equals(role, "admin")) {
            elasticity = (double) 100 / 88;       // ẩn cột action 12%
        } else if (Objects.equals(role, "accountant")) {
            btnCreate.setVisible(true);
            actionPayments.setVisible(true);
            elasticity = 1;
            addActionButtonsToTable();
        }

        contentLabel.setText("CÁC HỘ ĐÃ ĐÓNG " + collectionPeriodName.toUpperCase());

        tablePayments.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Thiết lập lắng nghe khi có thay đổi kích thước hoặc dữ liệu
        tablePayments.widthProperty().addListener((obs, oldVal, newVal) -> adjustColumnWidths());
        paymentsList.addListener((ListChangeListener<Payments>) c -> adjustColumnWidths());

        roomNumberPayments.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        totalAmountPayments.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        paidAmountPayments.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        datePayments.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        notePayments.setCellValueFactory(new PropertyValueFactory<>("note"));
        statusPayments.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Định dạng lại ngày thu
        datePayments.setCellFactory(DateFormat.forLocalDate());

        // Nháy chuột vào row sẽ mở chi tiết cư dân
        tablePayments.setRowFactory(tv -> {
            TableRow<Payments> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 2 && row.getItem() != null) {
                    try {
                        openPaymentDetailScene(row.getItem());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row;
        });

        loadPaymentsFromDatabase();
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

        roomNumberPayments.setPrefWidth(tableWidth * 0.2 * elasticity);
        totalAmountPayments.setPrefWidth(tableWidth * 0.1 * elasticity);
        paidAmountPayments.setPrefWidth(tableWidth * 0.1 * elasticity);
        datePayments.setPrefWidth(tableWidth * 0.1 * elasticity);
        notePayments.setPrefWidth(tableWidth * 0.3 * elasticity);
        statusPayments.setPrefWidth(tableWidth * 0.08 * elasticity);
        actionPayments.setPrefWidth(tableWidth * 0.12 * elasticity);
    }

    // Body --------------------------------------------------------------------
    public void handleCreatePayment() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/Payments/PaymentDetail/create-payment.fxml", "/styles/Payments/crud-payment.css", owner);
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

    public void openPaymentDetailScene(Payments payment) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        PaymentDetailController.setPaymentDetail(payment); // Hàm static để tạm giữ dữ liệu
        SceneNavigator.showPopupScene("/fxml/Payments/PaymentDetail/payment-detail.fxml", "/styles/Payments/crud-payment.css", owner);
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
                          COALESCE(SUM(ci.total_amount), 0) AS total_amount,
                          p.paid_amount,
                          p.debt_amount,
                          p.excess_amount,
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
                        resultSet.getString("total_amount"),
                        resultSet.getString("paid_amount"),
                        resultSet.getDate("payment_date") != null ? resultSet.getDate("payment_date").toLocalDate() : null,
                        resultSet.getString("note"),
                        resultSet.getString("status").equals("unpaid") ? "Chưa đóng" : "Đã đóng"
                );

                payment.setDebtAmount(resultSet.getString("debt_amount"));
                payment.setExcessAmount(resultSet.getString("excess_amount"));
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
                    private final Button btnDelete = new Button("Xóa");
                    private final Button btnSet = new Button("Đặt");
                    private final Button btnSet2 = new Button("Đặt");

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

                        btnCancel.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnCancel.setOnAction((ActionEvent event) -> {
                            Payments data = getTableView().getItems().get(getIndex());
                            try {
                                handleCancel(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            Payments data = getTableView().getItems().get(getIndex());
                            try {
                                handleDelete(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnSet.setStyle("-fx-background-color: #f9d567; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnSet.setOnAction((ActionEvent event) -> {
                            Payments data = getTableView().getItems().get(getIndex());
                            try {
                                handleSet(data.getRoomNumber(), data.getCollectionPeriod());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnSet2.setStyle("-fx-background-color: #f9d567; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnSet2.setOnAction((ActionEvent event) -> {
                            Payments data = getTableView().getItems().get(getIndex());
                            try {
                                handleSet(data.getRoomNumber(), data.getCollectionPeriod());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                    private final HBox panePaid = new HBox(6, btnSet, btnEdit, btnCancel);
                    private final HBox paneUnpaid = new HBox(10, btnSet2, btnDelete);

                    {
                        panePaid.setAlignment(Pos.CENTER);
                        paneUnpaid.setAlignment(Pos.CENTER);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Payments payment = getTableView().getItems().get(getIndex());
                            if (payment != null) {
                                if ("Đã đóng".equals(payment.getStatus())) {
                                    setGraphic(panePaid);
                                } else {
                                    setGraphic(paneUnpaid);
                                }
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
        SceneNavigator.showPopupScene("/fxml/Payments/PaymentDetail/edit-payment.fxml", "/styles/Payments/crud-payment.css", owner);

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

    private void handleDelete(Payments collectionItem) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn xóa thanh toán này?", collectionItem.getRoomNumber() + " - Đợt thu " + collectionPeriodName);
        if (result) {
            try {
                Connection connection = DatabaseConnection.getConnection();
                String sql = "DELETE FROM collection_items WHERE room_number = ? AND collection_period_id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, collectionItem.getRoomNumber().replace("Phòng ", ""));
                stmt.setInt(2, collectionPeriodId);

                if (stmt.executeUpdate() > 0) {
                    tablePayments.getItems().remove(collectionItem);
                    System.out.println("Đã xóa căn hộ: " + collectionItem.getRoomNumber() + " khỏi đợt thu " + collectionPeriodName);
                    CustomAlert.showSuccessAlert("Đã xóa căn hộ khỏi đợt thu thành công", true, 0.7);
                } else {
                    CustomAlert.showErrorAlert("Không tìm thấy đợt thu để xóa.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSet(String roomNumber, String collectionPeriodName) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/Payments/PaymentDetail/revenues-room-detail.fxml", "/styles/Payments/CollectionPeriods/crud-period.css", owner);

        RevenuesPeriodsDetailController controller = loader.getController();
        controller.setDetail(roomNumber, collectionPeriodName);

        //  Reload lại bảng
        controller.setCallback(() -> {
            // Chỉ reload khi thực sự thêm mới thành công!
            paymentsList.clear();
            loadPaymentsFromDatabase();
        });
    }
}