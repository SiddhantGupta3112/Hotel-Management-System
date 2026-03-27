module com.hotel.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires jbcrypt;

    opens com.hotel.app to javafx.fxml;
    opens com.hotel.app.controller to javafx.fxml;
    exports com.hotel.app;
}