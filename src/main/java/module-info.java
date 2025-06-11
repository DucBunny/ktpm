module com.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;

    exports com.app;
    exports com.app.controllers;
    exports com.app.models;
    exports com.app.views;

    opens com.app.controllers to javafx.fxml;
    opens com.app.models to javafx.fxml;
    opens com.app.views to javafx.fxml;
    exports com.app.controllers.Residents;
    opens com.app.controllers.Residents to javafx.fxml;
    exports com.app.controllers.Rooms;
    opens com.app.controllers.Rooms to javafx.fxml;
    exports com.app.controllers.Revenues;
    opens com.app.controllers.Revenues to javafx.fxml;
    exports com.app.controllers.Payments;
    opens com.app.controllers.Payments to javafx.fxml;
    exports com.app.controllers.Payments.CollectionPeriods;
    opens com.app.controllers.Payments.CollectionPeriods to javafx.fxml;
}