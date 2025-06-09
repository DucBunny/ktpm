package com.app.controllers.Rooms;

import com.app.models.Vehicles;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class VehicleDetailController {
    @FXML
    private TextField typeField;
    @FXML
    private TextField plateNumberField;
    @FXML
    private TextField brandField;
    @FXML
    private TextField colorField;
    @FXML
    private TextField dayField;
    @FXML
    private TextField monthField;
    @FXML
    private TextField yearField;
    @FXML
    private TextArea noteField;
    @FXML
    private TextField isActiveField;
    @FXML
    private TextField roomNumberField;

    private static Vehicles vehicleDetail;

    public static void setVehicleDetail(Vehicles vehicle) {
        vehicleDetail = vehicle;
    }

    @FXML
    public void initialize(String roomNumber) {
        if (vehicleDetail == null) {
            return;
        }

        // Set dữ liệu nếu có
        typeField.setText(mapType(vehicleDetail.getType()));
        plateNumberField.setText(vehicleDetail.getPlateNumber());
        brandField.setText(vehicleDetail.getBrand());
        colorField.setText(vehicleDetail.getColor());
        noteField.setText(vehicleDetail.getNote());
        isActiveField.setText(mapIsActive(vehicleDetail.getIsActive()));
        roomNumberField.setText("Phòng " + roomNumber);

        // Gán giá trị ngày sinh
        if (vehicleDetail.getRegistrationDate() != null) {
            int day = vehicleDetail.getRegistrationDate().getDayOfMonth();
            int month = vehicleDetail.getRegistrationDate().getMonthValue();
            int year = vehicleDetail.getRegistrationDate().getYear();
            dayField.setText(String.format("%02d", day));
            monthField.setText(String.format("%02d", month));
            yearField.setText(String.valueOf(year));
        }
    }

    private String mapType(String type) {
        if (type == null)
            return "";
        return switch (type.toLowerCase()) {
            case "motorbike" -> "Xe máy";
            case "car" -> "Ô tô";
            default -> type;
        };
    }

    private String mapIsActive(String isActive) {
        if (isActive == null)
            return "";
        return switch (isActive) {
            case "1" -> "Hoạt động";
            case "0" -> "Đã hủy";
            default -> isActive;
        };
    }
}