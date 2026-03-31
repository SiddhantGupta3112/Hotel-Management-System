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

    /* IMPORTANT: jbcrypt 0.4 is an older 'Automatic Module'.
       If 'jbcrypt' is red, try 'org.mindrot.jbcrypt'
    */
    requires jbcrypt;
    requires io.github.cdimascio.dotenv.java;

    // Reflection permissions for JavaFX
    opens com.hotel.app to javafx.fxml;
    opens com.hotel.app.controller to javafx.fxml;

    exports com.hotel.app;
}