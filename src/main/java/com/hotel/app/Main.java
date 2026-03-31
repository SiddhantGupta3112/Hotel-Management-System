package com.hotel.app;

import com.hotel.app.repository.UserRepository;
import com.hotel.app.service.AuthService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("Hotel Management System - Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}