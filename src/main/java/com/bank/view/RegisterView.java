package com.bank.view;

import com.bank.controller.LoginController;
import com.bank.dao.DataAccessException;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Registration screen for a new customer (boundary class). */
public class RegisterView {

    private final VBox root = new VBox(12);

    public RegisterView(BankApp app) {
        LoginController controller = new LoginController();

        Label title = new Label("Register");
        title.getStyleClass().add("heading");

        TextField firstName = new TextField();
        TextField surname = new TextField();
        TextField address = new TextField();
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        PasswordField confirm = new PasswordField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        String[] labels = {"First name", "Surname", "Address", "Username", "Password", "Confirm password"};
        TextField[] fields = {firstName, surname, address, username, password, confirm};
        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(fields[i], 1, i);
        }

        Label message = new Label();
        message.setWrapText(true);
        message.getStyleClass().add("error");

        Button create = new Button("Create account");
        create.setDefaultButton(true);
        create.setOnAction(e -> {
            try {
                controller.register(firstName.getText(), surname.getText(), address.getText(),
                        username.getText(), password.getText(), confirm.getText());
                app.showLogin();
            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
            } catch (DataAccessException ex) {
                message.setText("Could not save your details. Please try again.");
            }
        });
        Button back = new Button("Back to login");
        back.setOnAction(e -> app.showLogin());

        root.getChildren().addAll(title, form, new HBox(10, create, back), message);
        root.setPadding(new Insets(28));
    }

    public Parent getRoot() {
        return root;
    }
}
