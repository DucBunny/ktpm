package com.app.controllers.Resident;

import com.app.models.Residents;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ResidentDetailController {
    // Left
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField placeOfBirthField;
    @FXML
    private TextField occupationField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField genderField;
    @FXML
    private TextField roomField;

    // Right
    @FXML
    private TextField dayField;
    @FXML
    private TextField monthField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField hometownField;
    @FXML
    private TextField ethnicityField;
    @FXML
    private TextField idCardNumberField;
    @FXML
    private TextField residenceStatusField;
    @FXML
    private TextField relationshipField;
    @FXML
    private TextField statusField;

    private static Residents residentDetail;

    public static void setResidentDetail(Residents resident) {
        residentDetail = resident;
    }

    @FXML
    public void initialize() {
        if (residentDetail == null) {
            return;
        }

        // Set dữ liệu
        fullNameField.setText(residentDetail.getFullName());
        placeOfBirthField.setText(residentDetail.getPlaceOfBirth());
        occupationField.setText(residentDetail.getOccupation());
        phoneField.setText(residentDetail.getPhone());
        genderField.setText(mapGender(residentDetail.getGender()));
        roomField.setText(residentDetail.getRoomNumber());

        hometownField.setText(residentDetail.getHometown());
        ethnicityField.setText(residentDetail.getEthnicity());
        idCardNumberField.setText(residentDetail.getIdCardNumber());
        residenceStatusField.setText(mapResidenceStatus(residentDetail.getResidenceStatus()));
        relationshipField.setText(mapRelationship(residentDetail.getRelationshipToOwner()));
        statusField.setText(mapStatus(residentDetail.getStatus()));

        if (residentDetail.getDateOfBirth() != null) {
            int day = residentDetail.getDateOfBirth().getDayOfMonth();
            int month = residentDetail.getDateOfBirth().getMonthValue();
            int year = residentDetail.getDateOfBirth().getYear();
            dayField.setText(String.format("%02d", day));
            monthField.setText(String.format("%02d", month));
            yearField.setText(String.valueOf(year));
        }
    }

    private String mapGender(String gender) {
        if (gender == null)
            return "";
        return switch (gender.toLowerCase()) {
            case "male" -> "Nam";
            case "female" -> "Nữ";
            case "other" -> "Khác";
            default -> gender;
        };
    }

    private String mapResidenceStatus(String status) {
        if (status == null)
            return "";
        return switch (status.toLowerCase()) {
            case "permanent" -> "Thường trú";
            case "temporary" -> "Tạm trú";
            default -> status;
        };
    }

    private String mapRelationship(String relationship) {
        if (relationship == null)
            return "";
        return switch (relationship.toLowerCase()) {
            case "owner" -> "Chủ hộ";
            case "spouse" -> "Vợ/Chồng";
            case "parent" -> "Cha/Mẹ";
            case "child" -> "Con cái";
            case "other" -> "Khác";
            default -> relationship;
        };
    }

    private String mapStatus(String status) {
        if (status == null)
            return "";
        return switch (status.toLowerCase()) {
            case "living" -> "Đang ở";
            case "moved_out" -> "Đã rời";
            default -> status;
        };
    }
}
