module com.hotel.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.sql;
    requires com.oracle.database.jdbc;
    requires jbcrypt;
    requires io.github.cdimascio.dotenv.java;

    // Main entry and core controllers
    opens com.hotel.app to javafx.fxml;
    opens com.hotel.app.controller to javafx.fxml;

    // Entities need to be open to javafx.base for TableView cell factories
    opens com.hotel.app.entity to javafx.base, javafx.fxml;

    // --- Role-Based Module Controllers ---
    // Opens the shared folder where MyProfileController lives
    opens com.hotel.app.controller.modules.shared to javafx.fxml;

    // Opens the specific role folders
    //opens com.hotel.app.controller.modules.customer to javafx.fxml;
    opens com.hotel.app.controller.modules.manager to javafx.fxml;
    // opens com.hotel.app.controller.modules.staff to javafx.fxml;

    exports com.hotel.app;
}