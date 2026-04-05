module com.hotel.app {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Icons
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    // Database & Security
    requires java.sql;
    requires com.oracle.database.jdbc;

    requires jbcrypt;
    requires io.github.cdimascio.dotenv.java;

    // Reflection permissions for JavaFX FXML + PropertyValueFactory
    opens com.hotel.app                              to javafx.fxml;
    opens com.hotel.app.controller                   to javafx.fxml;
    opens com.hotel.app.entity                       to javafx.base, javafx.fxml;
    opens com.hotel.app.controller.modules.shared    to javafx.fxml;
    opens com.hotel.app.controller.modules.manager   to javafx.fxml;
    opens com.hotel.app.controller.modules.staff     to javafx.fxml;  // required for RoomStatusController, CompletedTasksController

    exports com.hotel.app;
}
