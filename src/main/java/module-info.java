module com.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    exports com.app;
    exports com.app.controllers;
    exports com.app.models;
    exports com.app.views;

    opens com.app.controllers to javafx.fxml;
    opens com.app.models to javafx.fxml;
    opens com.app.views to javafx.fxml;

}