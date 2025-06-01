package com.app.controllers;

import com.app.models.Room;
import com.app.utils.ComboBoxOption;
import com.app.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

import java.sql.*;

public class EditRoomController {
    @FXML private TextField roomNameField;
    @FXML private TextField roomFloorField;
    @FXML private TextField roomAreaField;
    @FXML
    private ComboBox<ComboBoxOption> roomStatusCombox;
    @FXML
    private javafx.scene.control.Button saveButton;
    private static Room roomToEdit;

    public static void setRoomToEdit(Room room) {
        roomToEdit = room;
    }

    @FXML
    public void initialize() {
        // Giới tính
        roomStatusCombox.setItems(FXCollections.observableArrayList(
                new ComboBoxOption("Đang ở", "occupied"),
                new ComboBoxOption("Trống", "available")
        ));

        if (roomToEdit != null) {
            roomNameField.setText(String.valueOf(roomToEdit.getRoomNumber()));
            roomFloorField.setText(roomToEdit.getFloor());
            roomAreaField.setText(roomToEdit.getArea());

            String statusValue = roomToEdit.getStatus();
            for (ComboBoxOption option : roomStatusCombox.getItems()) {
                if (option.getValue().equals(statusValue)) {
                    roomStatusCombox.setValue(option);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            String roomName = roomNameField.getText();
            String roomFloor = roomFloorField.getText();
            String roomArea = roomAreaField.getText();
            String status = roomStatusCombox.getValue().getValue();

            String query = String.format(
                    "UPDATE rooms SET room_number='%s', floor='%s', area='%s', status='%s' WHERE room_number=%d",
                    roomName, roomFloor, roomArea, status, roomToEdit.getRoomNumber()
            );

            stmt.executeUpdate(query);

            // Đóng cửa sổ
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
