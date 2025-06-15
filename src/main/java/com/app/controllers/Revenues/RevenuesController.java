package com.app.controllers.Revenues;

import com.app.controllers.HeaderUtils.HeaderController;
import com.app.models.Revenues;
import com.app.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import java.util.Objects;

public class RevenuesController {
    private double elasticity;      // co giãn (nếu ẩn cột)

    //    Header
    @FXML
    private VBox headerPane; // Tiêm nút gốc của header.fxml
    @FXML
    private HeaderController headerPaneController; // Tiêm controller của header.fxml

    //    Body
    @FXML
    private Button btnCreate;

    @FXML
    private TableView<Revenues> tableRevenues;
    @FXML
    private TableColumn<Revenues, String> nameRevenues;
    @FXML
    private TableColumn<Revenues, String> codeRevenues;
    @FXML
    private TableColumn<Revenues, String> unitPriceRevenues;
    @FXML
    private TableColumn<Revenues, String> quantityUnitRevenues;
    @FXML
    private TableColumn<Revenues, String> descriptionRevenues;
    @FXML
    private TableColumn<Revenues, String> categoryRevenues;
    @FXML
    private TableColumn<Revenues, String> statusRevenues;
    @FXML
    private TableColumn<Revenues, Void> actionRevenues;

    private final ObservableList<Revenues> revenuesList = FXCollections.observableArrayList();

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
    public void initialize(String role) throws IOException {
        if (StageManager.getPrimaryStage().getScene() != null) {
            StageManager.getPrimaryStage().getScene().getRoot().getProperties().put("controller", this);
        }

        if (Objects.equals(role, "admin")) {
            elasticity = (double) 10 / 9;       // ẩn cột action 10%
        } else if (Objects.equals(role, "accountant")) {
            btnCreate.setVisible(true);
            actionRevenues.setVisible(true);
            elasticity = 1;
            addActionButtonsToTable();
        }

        tableRevenues.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Thiết lập lắng nghe khi có thay đổi kích thước hoặc dữ liệu
        tableRevenues.widthProperty().addListener((obs, oldVal, newVal) -> adjustColumnWidths(elasticity));
        revenuesList.addListener((ListChangeListener<Revenues>) c -> adjustColumnWidths(elasticity));

        nameRevenues.setCellValueFactory(new PropertyValueFactory<>("name"));
        codeRevenues.setCellValueFactory(new PropertyValueFactory<>("code"));
        unitPriceRevenues.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        quantityUnitRevenues.setCellValueFactory(new PropertyValueFactory<>("quantityUnit"));
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
        codeRevenues.setPrefWidth(tableWidth * 0.15 * elasticity);
        unitPriceRevenues.setPrefWidth(tableWidth * 0.09 * elasticity);
        quantityUnitRevenues.setPrefWidth(tableWidth * 0.08 * elasticity);
        descriptionRevenues.setPrefWidth(tableWidth * 0.22 * elasticity);
        categoryRevenues.setPrefWidth(tableWidth * 0.08 * elasticity);
        statusRevenues.setPrefWidth(tableWidth * 0.08 * elasticity);
        actionRevenues.setPrefWidth(tableWidth * 0.1 * elasticity);
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
                String quantityUnitRaw = resultSet.getString("quantity_unit");
                String quantityUnitRawDisplay = switch (quantityUnitRaw == null ? "" : quantityUnitRaw) {
                    case "car" -> "Ô tô";
                    case "motorbike" -> "Xe máy";
                    case "package" -> "Gói";
                    case "totalResident" -> "Nhân khẩu";
                    case "kWh" -> "kWh";
                    case "m2" -> "m2";
                    case "m3" -> "m3";
                    default -> "";
                };

                revenuesList.add(new Revenues(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("code"),
                        resultSet.getString("unit_price").equals("1.00") ? "" : resultSet.getString("unit_price"),
                        quantityUnitRawDisplay,
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

                        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-pref-width: 50; -fx-font-size: 14");
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
