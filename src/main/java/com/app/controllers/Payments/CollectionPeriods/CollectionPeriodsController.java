package com.app.controllers.Payments.CollectionPeriods;

import com.app.controllers.HeaderUtils.HeaderController;
import com.app.controllers.Payments.PaymentDetail.PaymentsController;
import com.app.models.CollectionPeriods;
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

public class CollectionPeriodsController {
    private double elasticity;      // co giãn (nếu ẩn cột)

    //    Header
    @FXML
    private VBox headerPane; // Tiêm nút gốc của header.fxml
    @FXML
    private HeaderController headerPaneController; // Tiêm controller của header.fxml

    @FXML
    private Button btnCreate;

    //    Body
    @FXML
    private TableView<CollectionPeriods> tableCollectionPeriods;
    @FXML
    private TableColumn<CollectionPeriods, String> namePeriod;
    @FXML
    private TableColumn<CollectionPeriods, String> codePeriod;
    @FXML
    private TableColumn<CollectionPeriods, String> totalAmountPeriod;
    @FXML
    private TableColumn<CollectionPeriods, String> totalPaidAmountPeriod;
    @FXML
    private TableColumn<CollectionPeriods, LocalDate> startDatePeriod;
    @FXML
    private TableColumn<CollectionPeriods, LocalDate> endDatePeriod;
    @FXML
    private TableColumn<CollectionPeriods, String> typePeriod;
    @FXML
    private TableColumn<CollectionPeriods, String> totalRoomPeriod;
    @FXML
    private TableColumn<CollectionPeriods, Void> actionPeriod;

    private final ObservableList<CollectionPeriods> collectionPeriodsList = FXCollections.observableArrayList();

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
            actionPeriod.setVisible(true);
            elasticity = 1;
            addActionButtonsToTable();
        }

        tableCollectionPeriods.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Thiết lập lắng nghe khi có thay đổi kích thước hoặc dữ liệu
        tableCollectionPeriods.widthProperty().addListener((obs, oldVal, newVal) -> adjustColumnWidths());
        collectionPeriodsList.addListener((ListChangeListener<CollectionPeriods>) c -> adjustColumnWidths());

        namePeriod.setCellValueFactory(new PropertyValueFactory<>("name"));
        codePeriod.setCellValueFactory(new PropertyValueFactory<>("code"));
        totalAmountPeriod.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalPaidAmountPeriod.setCellValueFactory(new PropertyValueFactory<>("totalPaidAmount"));
        startDatePeriod.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDatePeriod.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        typePeriod.setCellValueFactory(new PropertyValueFactory<>("type"));
        totalRoomPeriod.setCellValueFactory(new PropertyValueFactory<>("totalRoom"));

        // Định dạng lại ngày
        startDatePeriod.setCellFactory(DateFormat.forLocalDate());
        endDatePeriod.setCellFactory(DateFormat.forLocalDate());

        // Nháy chuột vào row sẽ mở chi tiết những người đóng
        tableCollectionPeriods.setRowFactory(tv -> {
            TableRow<CollectionPeriods> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 2 && row.getItem() != null) {
                    try {
                        openPeriodDetailScene(row.getItem().getId(), row.getItem().getName());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row;
        });

        loadCollectionPeriodsFromDatabase();
    }

    private void adjustColumnWidths() {
        // Lấy số dòng dữ liệu (item count)
        int rowCount = collectionPeriodsList.size();
        // Lấy chiều cao 1 hàng
        double rowHeight = 40;
        // Trừ chiều cao header
        double headerHeight = 50;
        // Tổng chiều cao dữ liệu
        double totalRowsHeight = rowCount * rowHeight;
        // Chiều cao hiển thị thực tế
        double tableContentHeight = tableCollectionPeriods.getHeight() - headerHeight;

        // Nếu tổng chiều cao dữ liệu > chiều cao table -> có scroll bar
        double padding = (totalRowsHeight > tableContentHeight) ? 18 : 0;
        double tableWidth = tableCollectionPeriods.getWidth() - padding;

        namePeriod.setPrefWidth(tableWidth * 0.11 * elasticity);
        codePeriod.setPrefWidth(tableWidth * 0.1 * elasticity);
        totalAmountPeriod.setPrefWidth(tableWidth * 0.15 * elasticity);
        totalPaidAmountPeriod.setPrefWidth(tableWidth * 0.15 * elasticity);
        startDatePeriod.setPrefWidth(tableWidth * 0.11 * elasticity);
        endDatePeriod.setPrefWidth(tableWidth * 0.11 * elasticity);
        typePeriod.setPrefWidth(tableWidth * 0.08 * elasticity);
        totalRoomPeriod.setPrefWidth(tableWidth * 0.07 * elasticity);
        actionPeriod.setPrefWidth(tableWidth * 0.12 * elasticity);
    }

    // Body --------------------------------------------------------------------
    public void handleCreatePeriod() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/Payments/CollectionPeriods/create-period.fxml", "/styles/Payments/CollectionPeriods/crud-period.css", owner);

        //  Reload lại bảng
        collectionPeriodsList.clear();
        loadCollectionPeriodsFromDatabase();
    }

    private void openPeriodDetailScene(int collectionPeriodId, String collectionPeriodName) throws IOException {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Payments/PaymentDetail/payments.fxml", "/styles/Payments/payments.css",
                tableCollectionPeriods);

        PaymentsController controller = loader.getController();
        controller.setCollectionPeriod(collectionPeriodId, collectionPeriodName);
        controller.initialize(UserSession.getRole());
        controller.initializeHeader();
    }

    public void loadCollectionPeriodsFromDatabase() {
        collectionPeriodsList.clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = """
                        SELECT
                           cp.id,
                           cp.name,
                           cp.code, 
                           cp.start_date,
                           cp.end_date,
                           cp.type,
                           COALESCE(SUM(ci_total.total_amount), 0) AS total_amount,
                           COALESCE(SUM(CASE WHEN paid_per_room.total_paid IS NOT NULL AND ci_total.total_amount IS NOT NULL
                                     THEN LEAST(paid_per_room.total_paid, ci_total.total_amount)
                                     ELSE 0 END), 0) AS total_paid_amount,
                           COALESCE(COUNT(DISTINCT p.room_number), 0) AS total_rooms_paid,
                           COALESCE(COUNT(DISTINCT ci_total.room_number), 0) AS total_rooms
                       FROM
                           collection_periods cp
                       LEFT JOIN (
                             SELECT
                                 collection_period_id,
                                 room_number,
                                 SUM(total_amount) AS total_amount
                             FROM
                                 collection_items
                             GROUP BY
                                 collection_period_id, room_number
                         ) ci_total ON cp.id = ci_total.collection_period_id
                       LEFT JOIN (
                             SELECT
                                 collection_period_id,
                                 room_number,
                                 SUM(paid_amount) AS total_paid
                             FROM
                                 payments
                             GROUP BY
                                 collection_period_id, room_number
                         ) paid_per_room ON ci_total.room_number = paid_per_room.room_number AND ci_total.collection_period_id = paid_per_room.collection_period_id
                       LEFT JOIN
                           payments p ON ci_total.room_number = p.room_number AND ci_total.collection_period_id = p.collection_period_id
                       GROUP BY
                           cp.id, cp.name, cp.code, cp.start_date, cp.end_date, cp.type
                       ORDER BY
                           cp.start_date DESC;
                    """;
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String typeRaw = resultSet.getString("type");
                String typeDisplay = switch (typeRaw) {
                    case "monthly" -> "Theo tháng";
                    case "quarterly" -> "Theo quý";
                    case "yearly" -> "Theo năm";
                    default -> "";
                };

                collectionPeriodsList.add(new CollectionPeriods(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("code"),
                        resultSet.getString("total_amount"),
                        resultSet.getString("total_paid_amount"),
                        resultSet.getDate("start_date").toLocalDate(),
                        resultSet.getDate("end_date").toLocalDate(),
                        typeDisplay,
                        resultSet.getString("total_rooms_paid") + " / " + resultSet.getString("total_rooms")
                ));
            }

            tableCollectionPeriods.setItems(collectionPeriodsList);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CustomAlert.showErrorAlert("Không thể tải dữ liệu đợt thu từ CSDL.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<CollectionPeriods, Void>, TableCell<CollectionPeriods, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<CollectionPeriods, Void> call(final TableColumn<CollectionPeriods, Void> param) {
                return new TableCell<>() {
                    private final Button btnSet = new Button("Đặt");
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");

                    {
                        btnSet.setStyle("-fx-background-color: #f9d567; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnSet.setOnAction((ActionEvent event) -> {
                            CollectionPeriods data = getTableView().getItems().get(getIndex());
                            try {
                                handleSet(data.getId(), data.getName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            CollectionPeriods data = getTableView().getItems().get(getIndex());
                            try {
                                handleEdit(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            CollectionPeriods data = getTableView().getItems().get(getIndex());
                            try {
                                handleDelete(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                    private final HBox pane = new HBox(6, btnSet, btnEdit, btnDelete);

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

        actionPeriod.setCellFactory(cellFactory);
    }

    private void handleSet(int collectionPeriodId, String collectionPeriodName) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/Payments/CollectionPeriods/revenues-periods.fxml", "/styles/Payments/CollectionPeriods/crud-period.css", owner);

        RevenuesPeriodsController controller = loader.getController();
        controller.setCollectionPeriod(collectionPeriodId, collectionPeriodName);
        controller.initialize();

        // Sau khi sửa, làm mới lại bảng dữ liệu:
        loadCollectionPeriodsFromDatabase();
    }

    private void handleEdit(CollectionPeriods collectionPeriods) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        EditPeriodController.setPeriodToEdit(collectionPeriods); // Hàm static để tạm giữ dữ liệu
        SceneNavigator.showPopupScene("/fxml/Payments/CollectionPeriods/edit-period.fxml", "/styles/Payments/CollectionPeriods/crud-period.css", owner);

        // Sau khi sửa, làm mới lại bảng dữ liệu:
        collectionPeriodsList.clear();
        loadCollectionPeriodsFromDatabase();
    }

    private void handleDelete(CollectionPeriods collectionPeriods) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn xóa đợt thu này?", collectionPeriods.getName());
        if (result) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Kiểm tra xem có bản ghi payments nào không
                    //                    double totalPaidAmount = 0.0;
                    //                    double totalExcessAmount = 0.0;
                    //                    double totalDebtAmount = 0.0;
                    //                    String checkPaymentsSql = """
                    //                            SELECT COALESCE(SUM(p.paid_amount), 0) AS total_paid,
                    //                                   COALESCE(SUM(p.excess_amount), 0) AS total_excess,
                    //                                   COALESCE(SUM(p.debt_amount), 0) AS total_debt
                    //                            FROM payments p
                    //                            WHERE p.collection_period_id = ?
                    //                            """;
                    //                    try (PreparedStatement checkStmt = connection.prepareStatement(checkPaymentsSql)) {
                    //                        checkStmt.setInt(1, collectionPeriods.getId());
                    //                        ResultSet rs = checkStmt.executeQuery();
                    //                        if (rs.next()) {
                    //                            totalPaidAmount = rs.getDouble("total_paid");
                    //                            totalExcessAmount = rs.getDouble("total_excess");
                    //                            totalDebtAmount = rs.getDouble("total_debt");
                    //                        }
                    //                    }

                    // Cảnh báo nếu có excess_amount hoặc debt_amount
                    //                    if (totalExcessAmount > 0 || totalDebtAmount > 0) {
                    //                        String warningMessage = "Đợt thu này có:\n" +
                    //                                (totalPaidAmount > 0 ? "- Tổng tiền đã thanh toán: " + String.format("%.2f", totalPaidAmount) + " VND\n" : "") +
                    //                                (totalExcessAmount > 0 ? "- Tổng tiền thừa: " + String.format("%.2f", totalExcessAmount) + " VND\n" : "") +
                    //                                (totalDebtAmount > 0 ? "- Tổng tiền nợ: " + String.format("%.2f", totalDebtAmount) + " VND\n" : "") +
                    //                                "Việc xóa có thể ảnh hưởng đến các thanh toán sau. Bạn có muốn tiếp tục?";
                    //                        boolean proceed = CustomAlert.showConfirmAlert("Cảnh báo dữ liệu thanh toán", warningMessage);
                    //                        if (!proceed) {
                    //                            connection.rollback();
                    //                            return;
                    //                        }
                    //                    }

                    // Xóa payments trước (nếu có)
                    String deletePaymentsSql = "DELETE FROM payments WHERE collection_period_id = ?";
                    try (PreparedStatement deletePaymentsStmt = connection.prepareStatement(deletePaymentsSql)) {
                        deletePaymentsStmt.setInt(1, collectionPeriods.getId());
                        deletePaymentsStmt.executeUpdate();
                    }

                    // Xóa collection_items
                    String deleteItemsSql = "DELETE FROM collection_items WHERE collection_period_id = ?";
                    try (PreparedStatement deleteItemsStmt = connection.prepareStatement(deleteItemsSql)) {
                        deleteItemsStmt.setInt(1, collectionPeriods.getId());
                        deleteItemsStmt.executeUpdate();
                    }

                    // Xóa collection_periods
                    String deletePeriodSql = "DELETE FROM collection_periods WHERE id = ?";
                    try (PreparedStatement deletePeriodStmt = connection.prepareStatement(deletePeriodSql)) {
                        deletePeriodStmt.setInt(1, collectionPeriods.getId());
                        int rowsAffected = deletePeriodStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            tableCollectionPeriods.getItems().remove(collectionPeriods);
                            CustomAlert.showSuccessAlert("Đã xóa đợt thu " + collectionPeriods.getName() + " thành công.", true, 0.7);
                        } else {
                            CustomAlert.showErrorAlert("Không tìm thấy đợt thu để xóa.");
                            connection.rollback();
                            return;
                        }
                    }

                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    CustomAlert.showErrorAlert("Lỗi khi xóa đợt thu: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException | IOException e) {
                CustomAlert.showErrorAlert("Lỗi kết nối CSDL: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}