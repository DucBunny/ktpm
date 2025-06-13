package com.app.controllers.Payments.CollectionPeriods;

import com.app.controllers.HomePageController;
import com.app.controllers.Payments.PaymentsController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Revenues.RevenuesController;
import com.app.controllers.Rooms.RoomsController;
import com.app.models.CollectionPeriods;
import com.app.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
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

public class CollectionPeriodsController {
    private String role;
    private String username;

    private double elasticity;      // co giãn (nếu ẩn cột)

    //    Header
    @FXML
    private Label roleLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private MenuItem MenuItem_SignUp;

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
    private TableColumn<CollectionPeriods, Void> actionPeriod;

    private final ObservableList<CollectionPeriods> collectionPeriodsList = FXCollections.observableArrayList();


    @FXML
    public void initialize(String role, String username) {
        this.role = role;
        this.username = username;
        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
            elasticity = (double) 10 / 9;       // ẩn cột action 10%
        } else if (Objects.equals(role, "accountant")) {
            btnCreate.setVisible(true);
            actionPeriod.setVisible(true);
            roleLabel.setText("Bạn đang đăng nhập với quyền Kế toán.");
            elasticity = 1;
        }

        nameLabel.setText("Xin chào");

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
        addActionButtonsToTable();
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

        namePeriod.setPrefWidth(tableWidth * 0.2 * elasticity);
        codePeriod.setPrefWidth(tableWidth * 0.1 * elasticity);
        totalAmountPeriod.setPrefWidth(tableWidth * 0.15 * elasticity);
        totalPaidAmountPeriod.setPrefWidth(tableWidth * 0.15 * elasticity);
        startDatePeriod.setPrefWidth(tableWidth * 0.11 * elasticity);
        endDatePeriod.setPrefWidth(tableWidth * 0.11 * elasticity);
        typePeriod.setPrefWidth(tableWidth * 0.08 * elasticity);
        actionPeriod.setPrefWidth(tableWidth * 0.1 * elasticity);
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

    public void changeToResidents(Event event) throws Exception {
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
    public void handleCreatePeriod() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/Payments/CollectionPeriods/create-period.fxml", "/styles/Payments/CollectionPeriods/crud-period.css", owner);

        //  Reload lại bảng
        collectionPeriodsList.clear();
        loadCollectionPeriodsFromDatabase();
    }

    private void openPeriodDetailScene(int collectionPeriodId, String collectionPeriodName) throws IOException {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Payments/payments.fxml", "/styles/Payments/payments.css",
                tableCollectionPeriods);

        PaymentsController controller = loader.getController();
        controller.initialize(role, username, collectionPeriodId, collectionPeriodName);
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
                        typeDisplay
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
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");

                    {
                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            CollectionPeriods data = getTableView().getItems().get(getIndex());
                            try {
                                handleEdit(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            CollectionPeriods data = getTableView().getItems().get(getIndex());
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

        actionPeriod.setCellFactory(cellFactory);
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
            try {
                Connection connection = DatabaseConnection.getConnection();
                String sql = "DELETE FROM collection_periods WHERE id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, collectionPeriods.getId());

                if (stmt.executeUpdate() > 0) {
                    tableCollectionPeriods.getItems().remove(collectionPeriods);
                    System.out.println("Đã xóa đợt thu: " + collectionPeriods.getName());
                    CustomAlert.showSuccessAlert("Đã xóa đợt thu thành công", true, 0.7);
                } else {
                    CustomAlert.showErrorAlert("Không tìm thấy đợt thu để xóa.");
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