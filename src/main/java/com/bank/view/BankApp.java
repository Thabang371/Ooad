package com.bank.view;

import com.bank.dao.Database;
import com.bank.model.Customer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point of the JavaFX application. Sets up the database and switches between screens.
 * Views only display data and forward user actions to controllers (MVC boundary classes).
 */
public class BankApp extends Application {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        Database.initSchema();
        Database.loadSampleDataIfEmpty();

        this.stage = primaryStage;
        stage.setTitle("Banking System");
        showLogin();
        stage.show();
    }

    public void showLogin() {
        setRoot(new LoginView(this).getRoot(), 420, 360);
    }

    public void showRegister() {
        setRoot(new RegisterView(this).getRoot(), 460, 480);
    }

    public void showAccounts(Customer customer) {
        setRoot(new AccountView(this, customer).getRoot(), 960, 640);
    }

    private void setRoot(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
