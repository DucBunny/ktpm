package com.app.controllers.HeaderUtils;

import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import com.app.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class UserInfoController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField roleField;

    private UserReloadCallback parentCallback; // Callback từ trang cha

    public void setParentCallback(UserReloadCallback callback) {
        this.parentCallback = callback;
    }

    public void initialize() {
        emailField.setText(UserSession.getEmail());
        userNameField.setText(UserSession.getUsername());
        roleField.setText(UserSession.getRole().equals("admin") ? "Quản trị viên" : "Kế toán");
    }

    public void changeToEditInfo() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/HeaderUtils/edit-info.fxml", "/styles/sign-in-create-account.css", owner);

        EditInfoController controller = loader.getController();

        // Sau khi sửa, làm mới lại bảng dữ liệu:
        controller.setCallback(() -> {
            userNameField.setText(UserSession.getUsername());

            // Gọi callback của trang cha
            if (parentCallback != null) {
                parentCallback.onUserCrud();
            }
        });
    }
}
