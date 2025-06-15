package com.app.controllers.HeaderUtils;

import com.app.controllers.Homepage.HomePageController;
import com.app.controllers.Payments.CollectionPeriods.CollectionPeriodsController;
import com.app.controllers.Payments.PaymentDetail.PaymentsController;
import com.app.controllers.Residents.ResidentsController;
import com.app.controllers.Revenues.RevenuesController;
import com.app.controllers.Rooms.RoomDetailController;
import com.app.controllers.Rooms.RoomsController;
import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import com.app.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class HeaderController {
    private String role;
    private String username;
    private String email;

    @FXML
    private Label roleLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private MenuItem MenuItem_SignUp;

    @FXML
    private Button homeButton;
    @FXML
    private Button roomsButton;
    @FXML
    private Button residentsButton;
    @FXML
    private Button revenuesButton;
    @FXML
    private Button paymentsButton;

    public void setUserInfo(String email, String role, String username) {
        this.email = email;
        this.role = role;
        this.username = username;
    }

    public void initialize() throws SQLException, IOException {
        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
        } else if (Objects.equals(role, "accountant")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Kế toán.");
        }

        nameLabel.setText("Xin chào, " + (username != null ? username : ""));

        updateButtonActive();
    }

    private void updateButtonActive() {
        // Đã link file CSS trong Vbox to nhất
        homeButton.setId(null);
        roomsButton.setId(null);
        residentsButton.setId(null);
        revenuesButton.setId(null);
        paymentsButton.setId(null);

        String currentPage = UserSession.getCurrentPage();
        if (currentPage != null) {
            switch (currentPage.toLowerCase()) {
                case "home":
                    homeButton.setId("buttonActive");
                    break;
                case "rooms":
                    roomsButton.setId("buttonActive");
                    break;
                case "residents":
                    residentsButton.setId("buttonActive");
                    break;
                case "revenues":
                    revenuesButton.setId("buttonActive");
                    break;
                case "payments":
                    paymentsButton.setId("buttonActive");
                    break;
            }
        }
    }

    // Header Button -----------------------------------------------------------
    public void changeToHomePage(ActionEvent event) throws Exception {
        UserSession.setCurrentPage("home");
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Homepage/home-page.fxml", "/styles/home-page.css", event, true);

        HomePageController controller = loader.getController();
        controller.initialize(role);
        controller.initializeHeader();
    }

    public void changeToRooms(Event event) throws Exception {
        UserSession.setCurrentPage("rooms");
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Rooms/rooms.fxml", "/styles/Rooms/rooms.css", event, true);

        RoomsController controller = loader.getController();
        controller.initialize(role);
        controller.initializeHeader();
    }

    public void changeToResidents(Event event) throws Exception {
        UserSession.setCurrentPage("residents");
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Residents/residents.fxml", "/styles/Residents/residents.css",
                event, true);

        ResidentsController controller = loader.getController();
        controller.initialize(role);
        controller.initializeHeader();
    }

    public void changeToRevenues(Event event) throws Exception {
        UserSession.setCurrentPage("revenues");
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Revenues/revenues.fxml", "/styles/Revenues/revenues.css",
                event, true);

        RevenuesController controller = loader.getController();
        controller.initialize(role);
        controller.initializeHeader();
    }

    public void changeToPayments(Event event) throws Exception {
        UserSession.setCurrentPage("payments");
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/Payments/CollectionPeriods/collection-periods.fxml", "/styles/Payments/CollectionPeriods/collection-periods.css",
                event, true);

        CollectionPeriodsController controller = loader.getController();
        controller.initialize(role);
        controller.initializeHeader();
    }

    // Pop-up Button Cài đặt ---------------------------------------------------
    public void changeToInfo() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        FXMLLoader loader = SceneNavigator.showPopupSceneFXML("/fxml/HeaderUtils/user-info.fxml",
                "/styles/sign-in-create-account.css", owner);
        UserInfoController controller = loader.getController();

        // Truyền callback từ trang cha
        Object parentController = owner.getScene().getRoot().getProperties().get("controller");
        if (parentController != null) {
            controller.setParentCallback(() -> {
                try {
                    if (parentController instanceof HomePageController) {
                        ((HomePageController) parentController).initializeHeader();
                        ((HomePageController) parentController).initialize(UserSession.getRole());
                    } else if (parentController instanceof RoomsController) {
                        ((RoomsController) parentController).initializeHeader();
                        ((RoomsController) parentController).initialize(UserSession.getRole());
                    } else if (parentController instanceof RoomDetailController) {
                        ((RoomDetailController) parentController).initializeHeader();
                        ((RoomDetailController) parentController).initialize(UserSession.getRole());
                    } else if (parentController instanceof ResidentsController) {
                        ((ResidentsController) parentController).initializeHeader();
                        ((ResidentsController) parentController).initialize(UserSession.getRole());
                    } else if (parentController instanceof RevenuesController) {
                        ((RevenuesController) parentController).initializeHeader();
                        ((RevenuesController) parentController).initialize(UserSession.getRole());
                    } else if (parentController instanceof CollectionPeriodsController) {
                        ((CollectionPeriodsController) parentController).initializeHeader();
                        ((CollectionPeriodsController) parentController).initialize(UserSession.getRole());
                    } else if (parentController instanceof PaymentsController) {
                        ((PaymentsController) parentController).initializeHeader();
                        ((PaymentsController) parentController).initialize(UserSession.getRole());
                    }
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void changeToPassword() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/HeaderUtils/change-password.fxml", "/styles/sign-in-create-account.css", owner);
    }

    public void changeToCreateAccount() throws IOException {
        Stage owner = StageManager.getPrimaryStage();
        SceneNavigator.showPopupScene("/fxml/HeaderUtils/create-account.fxml",
                "/styles/sign-in-create-account.css", owner);
    }

    public void changeToSignIn(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/HeaderUtils/sign-in.fxml", "/styles/sign-in-create-account.css",
                event, false);
    }
}