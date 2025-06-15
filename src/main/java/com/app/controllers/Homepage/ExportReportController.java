package com.app.controllers.Homepage;

import com.app.models.CollectionItems;
import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ExportReportController {
    @FXML
    private ComboBox<ComboBoxOption> roomBox;
    @FXML
    private ComboBox<ComboBoxOption> periodBox;
    @FXML
    private TextField amountField;
    @FXML
    private TextArea noteArea;

    @FXML
    private AnchorPane periodAnchorPane;
    @FXML
    private AnchorPane amountAnchorPane;

    @FXML
    private Button saveButton;

    private final ObservableList<ComboBoxOption> roomSuggestions = FXCollections.observableArrayList();
    private final ObservableList<ComboBoxOption> allRooms = FXCollections.observableArrayList();

    private final ObservableList<ComboBoxOption> periodSuggestions = FXCollections.observableArrayList();
    private final ObservableList<ComboBoxOption> allPeriods = FXCollections.observableArrayList();

    public void initialize() {
        periodAnchorPane.setVisible(false);
        periodAnchorPane.setManaged(false);
        amountAnchorPane.setVisible(false);
        amountAnchorPane.setManaged(false);

        initRoomBox();

        setupRoomBoxSearch();
        setupPeriodBoxSearch();
        setupSaveButton();
    }

    private void initRoomBox() {
        roomBox.getItems().clear();
        allRooms.clear();
        roomSuggestions.clear();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT room_number, floor FROM rooms ORDER BY floor ASC, room_number ASC";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String roomNumber = rs.getString("room_number");
                ComboBoxOption option = new ComboBoxOption("Phòng " + roomNumber, roomNumber);
                allRooms.add(option);
                roomSuggestions.add(option);
            }
            roomBox.setItems(roomSuggestions);

            roomBox.setConverter(new StringConverter<ComboBoxOption>() {
                @Override
                public String toString(ComboBoxOption option) {
                    return option != null ? option.getLabel() : "";
                }

                @Override
                public ComboBoxOption fromString(String string) {
                    if (string == null || string.trim().isEmpty()) {
                        return null;
                    }

                    // Tìm mục khớp với văn bản nhập
                    String lowerInput = string.trim().toLowerCase();
                    for (ComboBoxOption room : allRooms) {
                        if (room.getLabel().toLowerCase().equals(lowerInput)) {
                            return room;
                        }
                    }
                    return null; // Không cho phép String tự do
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải danh sách phòng từ CSDL.");
        }
    }

    private void loadPeriodsForSelectedRoom(String roomNumber) {
        periodBox.getItems().clear();
        allPeriods.clear();
        periodSuggestions.clear();
        if (roomNumber == null || roomNumber.isEmpty())
            return;
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sql = """
                    SELECT DISTINCT cp.name
                    FROM collection_periods cp
                    JOIN collection_items ci ON cp.id = ci.collection_period_id
                    WHERE ci.room_number = ?
                    ORDER BY cp.name ASC;
                    """;
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, roomNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String period = rs.getString("name");
                ComboBoxOption option = new ComboBoxOption(period, period);
                allPeriods.add(option);
                periodSuggestions.add(option);
            }
            periodBox.setItems(periodSuggestions);

            periodBox.setConverter(new StringConverter<ComboBoxOption>() {
                @Override
                public String toString(ComboBoxOption option) {
                    return option != null ? option.getLabel() : "";
                }

                @Override
                public ComboBoxOption fromString(String string) {
                    if (string == null || string.trim().isEmpty()) {
                        return null;
                    }

                    // Tìm mục khớp với văn bản nhập
                    String lowerInput = string.trim().toLowerCase();
                    for (ComboBoxOption period : allPeriods) {
                        if (period.getLabel().toLowerCase().equals(lowerInput)) {
                            return period;
                        }
                    }
                    return null; // Không cho phép String tự do
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải danh sách đợt thu từ CSDL.");
        }
    }

    private void setupRoomBoxSearch() {
        roomBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                String inputText = newValue.trim();
                ObservableList<ComboBoxOption> filteredItems = FXCollections.observableArrayList();
                if (inputText.isEmpty()) {
                    filteredItems.addAll(allRooms);
                } else {
                    String lowerInput = inputText.toLowerCase();
                    for (ComboBoxOption room : allRooms) {
                        if (room.getLabel().toLowerCase().contains(lowerInput) ||
                                room.getValue().toLowerCase().contains(lowerInput)) {
                            filteredItems.add(room);
                        }
                    }
                }
                roomSuggestions.setAll(filteredItems);

                // Hiển thị dropdown nếu có văn bản và có mục khớp
                if (!inputText.isEmpty() && !filteredItems.isEmpty()) {
                    roomBox.show();
                } else {
                    roomBox.hide();
                }
            }
        });

        // Khi chọn phòng, load periods cho phòng đó
        roomBox.setOnAction(e -> {
            ComboBoxOption selected = roomBox.getValue();
            Stage stage = (Stage) saveButton.getScene().getWindow();
            if (selected != null) {
                roomBox.setValue(selected);
                Platform.runLater(() -> roomBox.getEditor().setText(selected.getLabel()));

                loadPeriodsForSelectedRoom(selected.getValue());

                // Reset periodBox selection
                periodBox.setValue(null);
                periodBox.getEditor().setText("");

                // Hiện periodBox
                stage.setHeight(418.5);
                periodAnchorPane.setVisible(true);
                periodAnchorPane.setManaged(true);
            } else {
                stage.setHeight(348.5);
                periodAnchorPane.setVisible(false);
                periodAnchorPane.setManaged(false);
            }
        });
    }

    private void setupPeriodBoxSearch() {
        periodBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                String inputText = newValue.trim();
                ObservableList<ComboBoxOption> filteredItems = FXCollections.observableArrayList();
                if (inputText.isEmpty()) {
                    filteredItems.addAll(allPeriods);
                } else {
                    String lowerInput = inputText.toLowerCase();
                    for (ComboBoxOption period : allPeriods) {
                        if (period.getLabel().toLowerCase().contains(lowerInput) ||
                                period.getValue().toLowerCase().contains(lowerInput)) {
                            filteredItems.add(period);
                        }
                    }
                }
                periodSuggestions.setAll(filteredItems);

                // Hiển thị dropdown nếu có văn bản và có mục khớp
                if (!inputText.isEmpty() && !filteredItems.isEmpty()) {
                    periodBox.show();
                } else {
                    periodBox.hide();
                }
            }
        });

        // Khi chọn period, hiện amount
        periodBox.setOnAction(e -> {
            ComboBoxOption selectedPeriod = periodBox.getValue();
            Stage stage = (Stage) saveButton.getScene().getWindow();
            if (selectedPeriod != null) {
                periodBox.setValue(selectedPeriod);
                Platform.runLater(() -> periodBox.getEditor().setText(selectedPeriod.getLabel()));

                stage.setHeight(603.5);
                amountAnchorPane.setVisible(true);
                amountAnchorPane.setManaged(true);

                ComboBoxOption roomSelected = roomBox.getValue();
                if (roomSelected != null) {
                    autoFillAmount(selectedPeriod.getValue(), roomSelected.getValue());
                }
            } else {
                stage.setHeight(418.5);
                amountAnchorPane.setVisible(false);
                amountAnchorPane.setManaged(false);
                amountField.clear();
            }
        });
    }

    private void autoFillAmount(String period, String roomNumber) {
        if (period == null || period.isEmpty() || roomNumber == null || roomNumber.isEmpty()) {
            amountField.clear();
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = """
                        SELECT SUM(total_amount) AS total
                        FROM collection_items ci
                        JOIN collection_periods cp ON ci.collection_period_id = cp.id
                        WHERE cp.name = ? AND ci.room_number = ?
                    """;
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, period);
            stmt.setString(2, roomNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                amountField.setText(String.valueOf(rs.getDouble("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Không thể lấy tổng số tiền từ CSDL.");
        }
    }

    private boolean areRequiredFieldsEmpty() {
        return Stream.of(
                amountField.getText()
        ).anyMatch(s -> s == null || s.trim().isEmpty()) ||
                Stream.of(
                        periodBox.getValue(),
                        roomBox.getValue()
                ).anyMatch(Objects::isNull);
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            if (areRequiredFieldsEmpty()) {
                showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            String period = periodBox.getValue().getValue();
            String roomNumber = roomBox.getValue().getValue();
            String note = noteArea.getText().trim();
            String amount = amountField.getText().trim();

            try {
                // Xuất báo cáo PDF
                generatePdfReport(roomNumber, period, amount, note);
                CustomAlert.showSuccessAlert("Xuất báo cáo thành công", true, 0.7);
                handleSave();
            } catch (IOException ex) {
                ex.printStackTrace();
                showErrorAlert("Lỗi khi xuất báo cáo: " + ex.getMessage());
            }
        });
    }

    private void generatePdfReport(String roomNumber, String period, String totalAmount, String note) throws IOException {
        // Tải dữ liệu collection_items
        List<CollectionItems> items = fetchCollectionItems(roomNumber, period);

        // Tạo tài liệu PDF
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDType0Font font = PDType0Font.load(document, getClass().getResourceAsStream("/styles/fonts/Arial.ttf"));

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Tiêu đề
                contentStream.setFont(font, 20);
                contentStream.beginText();
                String title = "HÓA ĐƠN THANH TOÁN ĐỢT THU " + period.toUpperCase();
                float titleWidth = font.getStringWidth(title) / 1000 * 20;
                float pageWidth = page.getMediaBox().getWidth();
                float titleX = (pageWidth - titleWidth) / 2;
                contentStream.newLineAtOffset(titleX, 750);
                contentStream.showText(title);
                contentStream.endText();

                contentStream.setFont(font, 14);
                contentStream.beginText();
                LocalDate currentDate = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Ngày' dd 'tháng' MM 'năm' yyyy");
                String formattedDate = currentDate.format(formatter);
                float formattedDateWidth = font.getStringWidth(formattedDate) / 1000 * 12;
                float titleXDate = (pageWidth - formattedDateWidth) / 2;
                contentStream.newLineAtOffset(titleXDate, 720);
                contentStream.showText(formattedDate);
                contentStream.newLineAtOffset(40 - titleXDate, -40);
                contentStream.showText("Căn hộ: Phòng " + roomNumber);
                contentStream.endText();

                // Vẽ bảng collection_items

                float yPosition = 620;
                float[] columnWidths = {30, 150, 80, 90, 90, 90};
                float tableStartX = (pageWidth - 530) / 2;
                float tableStartY = yPosition;
                float tableWidth = 530;

                // Đường kẻ header trên
                contentStream.setLineWidth(1f);
                contentStream.moveTo(tableStartX, yPosition + 15);
                contentStream.lineTo(tableStartX + tableWidth, yPosition + 15);
                contentStream.stroke();

                // Đường kẻ header dưới
                contentStream.setLineWidth(1f);
                contentStream.moveTo(tableStartX, tableStartY - 5);
                contentStream.lineTo(tableStartX + tableWidth, tableStartY - 5);
                contentStream.stroke();

                // Tiêu đề bảng
                contentStream.setFont(font, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth - 530) / 2 + 3, yPosition);
                contentStream.showText("STT");
                contentStream.newLineAtOffset(columnWidths[0] + 2, 0);
                contentStream.showText("Tên mục");
                contentStream.newLineAtOffset(columnWidths[1], 0);
                contentStream.showText("Đơn vị");
                contentStream.newLineAtOffset(columnWidths[2], 0);
                contentStream.showText("Số lượng");
                contentStream.newLineAtOffset(columnWidths[3], 0);
                contentStream.showText("Đơn giá");
                contentStream.newLineAtOffset(columnWidths[4], 0);
                contentStream.showText("Tiền");
                contentStream.endText();

                // Vẽ các đường kẻ cột
                float x = tableStartX;
                for (float colWidth : columnWidths) {
                    contentStream.moveTo(x, tableStartY + 15);
                    contentStream.lineTo(x, tableStartY - 5 - (items.size()) * 20);
                    contentStream.stroke();
                    x += colWidth;
                }

                // Cột trái
                contentStream.moveTo(tableStartX, tableStartY + 15);
                contentStream.lineTo(tableStartX, tableStartY - 10 - (items.size() + 1) * 20);
                contentStream.stroke();

                // Cột phải
                contentStream.moveTo(tableStartX + tableWidth, tableStartY + 15);
                contentStream.lineTo(tableStartX + tableWidth, tableStartY - 10 - (items.size() + 1) * 20);
                contentStream.stroke();

                yPosition -= 20;

                int stt = 1;
                // Dữ liệu bảng
                for (CollectionItems item : items) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(((pageWidth - 530) / 2) + 5, yPosition);
                    contentStream.showText(String.valueOf(stt++));
                    contentStream.newLineAtOffset(columnWidths[0], 0);
                    contentStream.showText(item.getName());
                    contentStream.newLineAtOffset(columnWidths[1], 0);
                    contentStream.showText(String.valueOf(item.getQuantityUnit()));
                    contentStream.newLineAtOffset(columnWidths[2], 0);
                    contentStream.showText(String.valueOf(item.getQuantity()));
                    contentStream.newLineAtOffset(columnWidths[3], 0);
                    contentStream.showText(String.valueOf(item.getUnitPrice()).equals("1.0") ? "" : String.valueOf(item.getUnitPrice()));
                    contentStream.newLineAtOffset(columnWidths[4], 0);
                    contentStream.showText(String.valueOf(item.getTotalAmount()));
                    contentStream.endText();

                    // Vẽ đường kẻ ngang dưới dòng này
                    contentStream.setLineWidth(0.5f);
                    contentStream.moveTo(tableStartX, yPosition - 5);
                    contentStream.lineTo(tableStartX + tableWidth, yPosition - 5);
                    contentStream.stroke();

                    yPosition -= 20;
                }

                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(395, yPosition);
                contentStream.showText("Tổng tiền:           " + totalAmount);
                contentStream.endText();

                // Đường kẻ cuối bảng
                contentStream.setLineWidth(1f);
                contentStream.moveTo(tableStartX, yPosition - 10);
                contentStream.lineTo(tableStartX + tableWidth, yPosition - 10);
                contentStream.stroke();

                // Ghi chú
                if (!note.isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.newLineAtOffset(40, yPosition - 40);
                    contentStream.showText("Ghi chú: " + note);
                    contentStream.endText();
                }

                // Mục Ký tên (góc phải dưới)
                contentStream.beginText();
                contentStream.newLineAtOffset(pageWidth - 100 - font.getStringWidth("Ký tên người nộp") / 1000 * 12, yPosition - 120);
                contentStream.showText("Ký tên người nộp");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(100, yPosition - 120);
                contentStream.showText("Ký tên kế toán");
                contentStream.endText();
            }


            // Tạo thư mục reports nếu chưa tồn tại
            String projectDir = System.getProperty("user.dir");
            Path reportsDir = Paths.get(projectDir, "src", "main", "resources", "reports");
            Files.createDirectories(reportsDir);

            // Lưu file PDF vào thư mục reports
            String safePeriod = period.replaceAll("[^a-zA-Z0-9-_]", "_"); // Thay ký tự không hợp lệ bằng '_'
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "BaoCao_" + roomNumber + "_" + safePeriod + "_" + timestamp + ".pdf";
            File outputFile = new File(reportsDir.toFile(), fileName);
            document.save(outputFile);
        }
    }

    private List<CollectionItems> fetchCollectionItems(String roomNumber, String period) {
        List<CollectionItems> items = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = """
                    SELECT ri.name, ci.quantity, ci.unit_price, ci.total_amount, ci.quantity_unit
                    FROM collection_items ci
                    JOIN collection_periods cp ON ci.collection_period_id = cp.id
                    JOIN revenue_items ri ON ci.revenue_item_id = ri.id
                    WHERE ci.room_number = ? AND cp.name = ?
                    """;
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, roomNumber);
            stmt.setString(2, period);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String quantityUnitRaw = rs.getString("quantity_unit");
                String quantityUnitRawDisplay = switch (quantityUnitRaw == null ? "" : quantityUnitRaw) {
                    case "car", "motorbike" -> "Chiếc";
                    case "package" -> "Gói";
                    case "totalResident" -> "Nhân khẩu";
                    case "m2" -> "m2";
                    case "m3" -> "m3";
                    default -> "";
                };

                items.add(new CollectionItems(
                        rs.getString("name"),
                        quantityUnitRawDisplay,
                        rs.getDouble("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("total_amount")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải danh sách mục thu từ CSDL.");
        }
        return items;
    }

    private void handleSave() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    // Utils -------------------------------------------------------------------
    private void showErrorAlert(String message) {
        try {
            CustomAlert.showErrorAlert(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}