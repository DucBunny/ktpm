package com.app.controllers.Payments.CollectionPeriods;

import com.app.models.CollectionPeriods;
import com.app.utils.ComboBoxOption;
import com.app.utils.CustomAlert;
import com.app.utils.DatabaseConnection;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Stream;

public class EditPeriodController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField codeField;
    @FXML
    private ComboBox<ComboBoxOption> startDayBox;
    @FXML
    private ComboBox<ComboBoxOption> startMonthBox;
    @FXML
    private Spinner<Integer> startYearSpinner;
    @FXML
    private ComboBox<ComboBoxOption> endDayBox;
    @FXML
    private ComboBox<ComboBoxOption> endMonthBox;
    @FXML
    private Spinner<Integer> endYearSpinner;
    @FXML
    private ComboBox<ComboBoxOption> typeBox;

    @FXML
    private Button saveButton;

    private static CollectionPeriods periodToEdit;
    private boolean isInitializing = true; // Biến cờ để ngăn ChangeListener khi khởi tạo

    public static void setPeriodToEdit(CollectionPeriods collectionPeriods) {
        periodToEdit = collectionPeriods;
    }

    @FXML
    public void initialize() {
        initTypeBox();

        initDayBox();
        initMonthBox();
        initYearSpinner();

        // Set dữ liệu nếu có
        if (periodToEdit != null) {
            nameField.setText(periodToEdit.getName());
            codeField.setText(periodToEdit.getCode());
            setComboBoxValue(typeBox, periodToEdit.getType(), true);

            // Gán giá trị ngày
            if (periodToEdit.getStartDate() != null) {
                int startDay = periodToEdit.getStartDate().getDayOfMonth();
                int startMonth = periodToEdit.getStartDate().getMonthValue();
                int startYear = periodToEdit.getStartDate().getYear();
                setComboBoxValue(startDayBox, String.format("%02d", startDay), false);
                setComboBoxValue(startMonthBox, String.format("%02d", startMonth), false);
                startYearSpinner.getValueFactory().setValue(startYear);
            }

            if (periodToEdit.getEndDate() != null) {
                int endDay = periodToEdit.getEndDate().getDayOfMonth();
                int endMonth = periodToEdit.getEndDate().getMonthValue();
                int endYear = periodToEdit.getEndDate().getYear();
                setComboBoxValue(endDayBox, String.format("%02d", endDay), false);
                setComboBoxValue(endMonthBox, String.format("%02d", endMonth), false);
                endYearSpinner.getValueFactory().setValue(endYear);
            }
        }

        // Sau khi gán dữ liệu, gắn ChangeListener và cho phép gợi ý
        isInitializing = false;
        setupAutoSuggestEndDate();
        setupSaveButton();
    }

    private void initTypeBox() {
        typeBox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Theo tháng", "monthly"),
                new ComboBoxOption("Theo quý", "quarterly"),
                new ComboBoxOption("Theo năm", "yearly")
        ));
    }

    private void initDayBox() {
        ObservableList<ComboBoxOption> days = FXCollections.observableArrayList();
        for (int i = 1; i <= 31; i++) {
            String value = String.format("%02d", i);
            days.add(new ComboBoxOption(String.valueOf(i), value));
        }
        startDayBox.setItems(days);
        endDayBox.setItems(days);
    }

    private void initMonthBox() {
        ObservableList<ComboBoxOption> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            String value = String.format("%02d", i);
            months.add(new ComboBoxOption(String.valueOf(i), value));
        }
        startMonthBox.setItems(months);
        endMonthBox.setItems(months);
    }

    private void initYearSpinner() {
        SpinnerValueFactory<Integer> startYearFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, LocalDate.now().getYear() + 1, LocalDate.now().getYear());
        startYearSpinner.setValueFactory(startYearFactory);
        SpinnerValueFactory<Integer> endYearFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, LocalDate.now().getYear() + 1, LocalDate.now().getYear());
        endYearSpinner.setValueFactory(endYearFactory);
    }

    private void setComboBoxValue(ComboBox<ComboBoxOption> comboBox, String value, boolean compareWithLabel) {
        if (value != null && comboBox.getItems() != null) {
            for (ComboBoxOption option : comboBox.getItems()) {
                String compareValue = compareWithLabel ? option.getLabel() : option.getValue();
                if (compareValue != null && compareValue.equalsIgnoreCase(value.trim())) {
                    comboBox.setValue(option);
                    break;
                }
            }
        }
    }

    private void setupAutoSuggestEndDate() {
        ChangeListener<Object> listener = (obs, oldValue, newValue) -> {
            if (isInitializing || !areRequiredFieldsFilledForStartDate()) {
                return; // Không gợi ý khi đang khởi tạo hoặc thiếu trường
            }

            int startDay = Integer.parseInt(startDayBox.getValue().getValue());
            int startMonth = Integer.parseInt(startMonthBox.getValue().getValue());
            int startYear = startYearSpinner.getValue();
            String type = (typeBox.getValue() != null) ? typeBox.getValue().getValue() : "monthly";

            try {
                LocalDate startDate = LocalDate.of(startYear, startMonth, startDay);
                LocalDate endDate = calculateEndDate(startDate, type);

                setComboBoxValue(endDayBox, String.format("%02d", endDate.getDayOfMonth()), false);
                setComboBoxValue(endMonthBox, String.format("%02d", endDate.getMonthValue()), false);
                endYearSpinner.getValueFactory().setValue(endDate.getYear());
            } catch (DateTimeException e) {
                // Không cập nhật nếu ngày bắt đầu không hợp lệ
                e.printStackTrace();
            }

        };

        startDayBox.valueProperty().addListener(listener);
        startMonthBox.valueProperty().addListener(listener);
        startYearSpinner.valueProperty().addListener(listener);
        typeBox.valueProperty().addListener(listener);
    }

    private boolean areRequiredFieldsFilledForStartDate() {
        return startDayBox.getValue() != null &&
                startMonthBox.getValue() != null &&
                startYearSpinner.getValue() != null &&
                typeBox.getValue() != null;
    }

    private LocalDate calculateEndDate(LocalDate startDate, String type) {
        return switch (type) {
            case "monthly" -> startDate.plusMonths(1).minusDays(1);
            case "quarterly" -> startDate.plusMonths(3).minusDays(1);
            case "yearly" -> startDate.plusYears(1).minusDays(1);
            default -> startDate;
        };
    }

    private boolean areRequiredFieldsEmpty() {
        return Stream.of(
                nameField.getText(),
                codeField.getText()
        ).anyMatch(s -> s == null || s.trim().isEmpty()) ||
                Stream.of(
                        typeBox.getValue(),
                        startDayBox.getValue(),
                        startMonthBox.getValue(),
                        startYearSpinner.getValue(),
                        endDayBox.getValue(),
                        endMonthBox.getValue(),
                        endYearSpinner.getValue()
                ).anyMatch(Objects::isNull);
    }

    private boolean isValidDate(int day, int month, int year) {
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    private boolean isValidPeriod(LocalDate startDate, LocalDate endDate, String type) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1; // Bao gồm ngày kết thúc
        int startMonth = startDate.getMonthValue();

        // Nếu đang chỉnh sửa bản ghi hiện có, chấp nhận nếu dữ liệu không thay đổi
        if (periodToEdit != null &&
                startDate.equals(periodToEdit.getStartDate()) &&
                endDate.equals(periodToEdit.getEndDate()) &&
                type.equals(periodToEdit.getType())) {
            return true;
        }

        return switch (type) {
            case "monthly" -> {
                // Kiểm tra xem khoảng thời gian có khoảng 1 tháng không
                long monthsBetween = ChronoUnit.MONTHS.between(startDate, endDate.plusDays(1));
                yield monthsBetween == 1 || (monthsBetween == 0 && daysBetween >= 28);
            }
            case "quarterly" -> {
                // Kiểm tra tháng bắt đầu phải là 1, 4, 7, 10
                if (!(startMonth == 1 || startMonth == 4 || startMonth == 7 || startMonth == 10)) {
                    yield false;
                }
                // Kiểm tra khoảng thời gian có khoảng 3 tháng không
                yield ChronoUnit.MONTHS.between(startDate, endDate.plusDays(1)) == 3 ||
                        (ChronoUnit.MONTHS.between(startDate, endDate) == 2 && daysBetween >= 89);
            }
            case "yearly" ->
                // Kiểm tra khoảng thời gian có khoảng 12 tháng không
                    ChronoUnit.MONTHS.between(startDate, endDate.plusDays(1)) == 12 ||
                            (ChronoUnit.MONTHS.between(startDate, endDate) == 11 && daysBetween >= 364);
            default -> false;
        };
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> {
            if (areRequiredFieldsEmpty()) {
                showErrorAlert("Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            String name = nameField.getText().trim();
            String code = codeField.getText().trim();
            String type = typeBox.getValue().getValue();

            int startDay = Integer.parseInt(startDayBox.getValue().getValue());
            int startMonth = Integer.parseInt(startMonthBox.getValue().getValue());
            int startYear = startYearSpinner.getValue();
            int endDay = Integer.parseInt(endDayBox.getValue().getValue());
            int endMonth = Integer.parseInt(endMonthBox.getValue().getValue());
            int endYear = endYearSpinner.getValue();

            // Kiểm tra ngày hợp lệ
            if (!isValidDate(startDay, startMonth, startYear)) {
                showErrorAlert("Ngày bắt đầu không hợp lệ.");
                return;
            }

            if (!isValidDate(endDay, endMonth, endYear)) {
                showErrorAlert("Ngày kết thúc không hợp lệ.");
                return;
            }

            // Kiểm tra thời gian hợp lệ (ngày kết thúc không trước ngày bắt đầu)
            LocalDate startDate = LocalDate.of(startYear, startMonth, startDay);
            LocalDate endDate = LocalDate.of(endYear, endMonth, endDay);
            if (endDate.isBefore(startDate)) {
                showErrorAlert("Ngày kết thúc không thể trước ngày bắt đầu.");
                return;
            }

            // Kiểm tra khoảng thời gian theo type
            if (!isValidPeriod(startDate, endDate, type)) {
                String errorMessage = type.equals("quarterly") ? "Quý phải bắt đầu từ tháng 1, 4, 7 hoặc 10." : "Vui lòng chọn đúng một tháng, một quý hoặc một năm.";
                showErrorAlert(errorMessage);
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // Kiểm tra mã đợt thu trùng lặp (trừ bản ghi hiện tại)
                    String checkSql = "SELECT id FROM collection_periods WHERE code = ? AND id != ?";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                        checkStmt.setString(1, code);
                        checkStmt.setInt(2, periodToEdit.getId());
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            showErrorAlert("Mã đợt thu đã tồn tại trong hệ thống.");
                            connection.rollback();
                            return;
                        }
                    }

                    // Cập nhật đợt thu
                    String sql = "UPDATE collection_periods SET name = ?, start_date = ?, end_date = ?, type = ?, code = ? WHERE id = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, name);
                        stmt.setDate(2, Date.valueOf(LocalDate.of(startYear, startMonth, startDay)));
                        stmt.setDate(3, Date.valueOf(LocalDate.of(endYear, endMonth, endDay)));
                        stmt.setString(4, type);
                        stmt.setString(5, code);
                        stmt.setInt(6, periodToEdit.getId());

                        if (stmt.executeUpdate() == 0) {
                            showErrorAlert("Không tìm thấy đợt thu để cập nhật.");
                            connection.rollback();
                            return;
                        }
                    }

                    connection.commit();
                    CustomAlert.showSuccessAlert("Cập nhật đợt thu thành công", true, 0.7);
                    handleSave();
                } catch (SQLException ex) {
                    connection.rollback();
                    if (ex.getSQLState().equals("23000") && ex.getMessage().contains("unique_code")) {
                        showErrorAlert("Mã đợt thu đã tồn tại trong hệ thống.");
                    } else {
                        ex.printStackTrace();
                        showErrorAlert("Lỗi SQL: " + ex.getMessage());
                    }
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
                showErrorAlert("Lỗi: " + ex.getMessage());
            }
        });
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
