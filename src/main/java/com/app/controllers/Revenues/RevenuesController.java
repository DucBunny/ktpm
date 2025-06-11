package com.app.controllers.Revenues;

import com.app.controllers.HomePageController;
import com.app.controllers.Payments.CollectionPeriods.CollectionPeriodsController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Rooms.RoomsController;
import com.app.models.Revenues;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
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
import java.util.Objects;

public class RevenuesController {
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

    //    Body
    @FXML
    private Button btnCreate;

    @FXML
    private TableView<Revenues> tableRevenues;
    @FXML
    private TableColumn<Revenues, String> nameRevenues;
    @FXML
    private TableColumn<Revenues, String> unitPriceRevenues;
    @FXML
    private TableColumn<Revenues, String> descriptionRevenues;
    @FXML
    private TableColumn<Revenues, String> categoryRevenues;
    @FXML
    private TableColumn<Revenues, String> statusRevenues;
    @FXML
    private TableColumn<Revenues, Void> actionRevenues;

    private final ObservableList<Revenues> revenuesList = FXCollections.observableArrayList();

    @FXML
    public void initialize(String role, String username) throws IOException {
        this.role = role;
        this.username = username;

        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
            elasticity = (double) 10 / 9;       // ẩn cột action 10%
        } else if (Objects.equals(role, "accountant")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Kế toán.");
            btnCreate.setVisible(true);
            actionRevenues.setVisible(true);
            addActionButtonsToTable();
            elasticity = 1;
        }

        nameLabel.setText("Xin chào, " + username);

        tableRevenues.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Thiết lập lắng nghe khi có thay đổi kích thước hoặc dữ liệu
        tableRevenues.widthProperty().addListener((obs, oldVal, newVal) -> adjustColumnWidths(elasticity));
        revenuesList.addListener((ListChangeListener<Revenues>) c -> adjustColumnWidths(elasticity));

        nameRevenues.setCellValueFactory(new PropertyValueFactory<>("name"));
        unitPriceRevenues.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        descriptionRevenues.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryRevenues.setCellValueFactory(new PropertyValueFactory<>("category"));
        statusRevenues.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Nháy chuột vào row sẽ mở chi tiết khoản thu
        tableRevenues.setRowFactory(tv -> {
            TableRow<Revenues> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 2 && row.getItem() != null) {
                    try {
                        openRevenueDetailScene(row.getItem());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row;
        });

        loadRevenuesFromDatabase();
    }

    private void adjustColumnWidths(double elasticity) {
        // Lấy số dòng dữ liệu (item count)
        int rowCount = revenuesList.size();
        // Lấy chiều cao 1 hàng
        double rowHeight = 40;
        // Trừ chiều cao header
        double headerHeight = 50;
        // Tổng chiều cao dữ liệu
        double totalRowsHeight = rowCount * rowHeight;
        // Chiều cao hiển thị thực tế
        double tableContentHeight = tableRevenues.getHeight() - headerHeight;

        // Nếu tổng chiều cao dữ liệu > chiều cao table -> có scroll bar
        double padding = (totalRowsHeight > tableContentHeight) ? 18 : 0;
        double tableWidth = tableRevenues.getWidth() - padding;

        nameRevenues.setPrefWidth(tableWidth * 0.2 * elasticity);
        unitPriceRevenues.setPrefWidth(tableWidth * 0.15 * elasticity);
        descriptionRevenues.setPrefWidth(tableWidth * 0.35 * elasticity);
        categoryRevenues.setPrefWidth(tableWidth * 0.1 * elasticity);
        statusRevenues.setPrefWidth(tableWidth * 0.1 * elasticity);
        actionRevenues.setPrefWidth(tableWidth * 0.1 * elasticity);
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

    public void changeToPayments(Event event) throws Exception {
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
    public void handleCreateRevenue() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/Revenues/create-revenue.fxml", "/styles/Revenues/crud-revenue.css", owner);

        //  Reload lại bảng
        revenuesList.clear();
        loadRevenuesFromDatabase();
    }

    public void openRevenueDetailScene(Revenues revenue) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        RevenueDetailController.setRevenueDetail(revenue); // Hàm static để tạm giữ dữ liệu
        SceneNavigator.showPopupScene("/fxml/Revenues/revenue-detail.fxml", "/styles/Revenues/crud-revenue.css", owner);
    }

    public void loadRevenuesFromDatabase() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT * FROM revenue_items ORDER BY status ASC , unit_price DESC, name ASC";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                revenuesList.add(new Revenues(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("unit_price").equals("1.00") ? "" : resultSet.getString("unit_price"),
                        resultSet.getString("description"),
                        resultSet.getString("category").equals("mandatory") ? "Bắt buộc" : "Tự nguyện",
                        resultSet.getString("status").equals("active") ? "Mở" : "Đóng"
                ));
            }

            tableRevenues.setItems(revenuesList);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CustomAlert.showErrorAlert("Không thể tải dữ liệu khoản thu từ CSDL.");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<Revenues, Void>, TableCell<Revenues, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Revenues, Void> call(final TableColumn<Revenues, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");

                    {
                        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnEdit.setOnAction((ActionEvent event) -> {
                            Revenues data = getTableView().getItems().get(getIndex());
                            try {
                                handleEdit(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
                        btnDelete.setOnAction((ActionEvent event) -> {
                            Revenues data = getTableView().getItems().get(getIndex());
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

        actionRevenues.setCellFactory(cellFactory);
    }

    private void handleEdit(Revenues revenues) throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        EditRevenueController.setRevenueToEdit(revenues); // Hàm static để tạm giữ dữ liệu
        SceneNavigator.showPopupScene("/fxml/Revenues/edit-revenue.fxml", "/styles/Revenues/crud-revenue.css", owner);

        // Sau khi sửa, làm mới lại bảng dữ liệu
        revenuesList.clear();
        loadRevenuesFromDatabase();
    }

    private void handleDelete(Revenues revenues) throws IOException {
        boolean result = CustomAlert.showConfirmAlert("Bạn có chắc chắn muốn xóa khoản thu này?", revenues.getName());
        if (result) {
            try {
                Connection connection = DatabaseConnection.getConnection();
                String sql = "DELETE FROM revenue_items WHERE id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, revenues.getId());

                if (stmt.executeUpdate() > 0) {
                    tableRevenues.getItems().remove(revenues);
                    System.out.println("Đã xóa khoản thu: " + revenues.getName());
                    CustomAlert.showSuccessAlert("Đã xóa khoản thu thành công", true, 0.7);
                } else {
                    CustomAlert.showErrorAlert("Không tìm thấy khoản thu để xóa.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
