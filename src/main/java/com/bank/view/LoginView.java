package com.bank.view;

import com.bank.controller.LoginController;
import com.bank.dao.DataAccessException;
import com.bank.model.Customer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Login screen (boundary class). No business logic: it passes input to LoginController. */
public class LoginView {

    private final VBox root = new VBox(10);

    public LoginView(BankApp app) {
        LoginController controller = new LoginController();

        Label title = new Label("Banking System");
        title.getStyleClass().add("heading");
        Label subtitle = new Label("Sign in to your accounts");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label message = new Label();
        message.getStyleClass().add("error");
        message.setWrapText(true);

        Button loginButton = new Button("Log in");
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> {
            try {
                Customer customer = controller.authenticate(usernameField.getText(), passwordField.getText());
                app.showAccounts(customer);
            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
                passwordField.clear();
            } catch (DataAccessException ex) {
                message.setText("The database is unavailable. Please try again later.");
            }
        });

        Hyperlink register = new Hyperlink("New customer? Register here");
        register.setOnAction(e -> app.showRegister());

        root.getChildren().addAll(title, subtitle, usernameField, passwordField, loginButton, message, register);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER_LEFT);
        root.setMaxWidth(360);
    }

    public Parent getRoot() {
        return root;
    }
}
