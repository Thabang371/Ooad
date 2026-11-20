package com.bank.view;

import com.bank.controller.AccountController;
import com.bank.dao.DataAccessException;
import com.bank.model.Account;
import com.bank.model.ChequeAccount;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Main screen after login: lists the customer's accounts and lets them deposit, withdraw,
 * open new accounts, pay monthly interest and view history. Every action goes through AccountController.
 */
public class AccountView {

    private final BorderPane root = new BorderPane();
    private final AccountController controller = new AccountController();
    private final Customer customer;

    private final TableView<Account> table = new TableView<>();
    private final ListView<Transaction> history = new ListView<>();
    private final Label status = new Label();

    public AccountView(BankApp app, Customer customer) {
        this.customer = customer;

        // ---------- top bar
        Label welcome = new Label("Welcome, " + customer.getFullName());
        welcome.getStyleClass().add("heading");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = new Button("Log out");
        logout.setOnAction(e -> app.showLogin());
        HBox top = new HBox(10, welcome, spacer, logout);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(16));
        root.setTop(top);

        // ---------- accounts table
        table.getColumns().add(column("Account no.", a -> a.getAccountNumber()));
        table.getColumns().add(column("Type", a -> a.getAccountType()));
        table.getColumns().add(column("Branch", a -> a.getBranch()));
        table.getColumns().add(column("Balance (BWP)", a -> String.format("%,.2f", a.getBalance())));
        table.getColumns().add(column("Employer", a -> a instanceof ChequeAccount c ? c.getEmployerName() : ""));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("You have no accounts yet. Open one on the right."));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> refreshHistory());

        history.setPrefHeight(180);
        history.setPlaceholder(new Label("Select an account to see its history."));

        VBox centre = new VBox(8, new Label("Your accounts"), table, new Label("Transaction history"), history);
        VBox.setVgrow(table, Priority.ALWAYS);
        centre.setPadding(new Insets(0, 8, 16, 16));
        root.setCenter(centre);

        // ---------- right-hand side: actions
        root.setRight(buildActions());

        // ---------- status bar
        status.setWrapText(true);
        status.setPadding(new Insets(8, 16, 12, 16));
        root.setBottom(status);

        refreshTable();
    }

    private VBox buildActions() {
        // deposit / withdraw
        TextField amount = new TextField();
        amount.setPromptText("Amount, e.g. 250.00");
        Button deposit = new Button("Deposit");
        Button withdraw = new Button("Withdraw");
        deposit.setOnAction(e -> run(() -> controller.deposit(selected(), amount.getText()), amount));
        withdraw.setOnAction(e -> run(() -> controller.withdraw(selected(), amount.getText()), amount));
        VBox moneyBox = new VBox(8, amount, new HBox(8, deposit, withdraw));
        TitledPane money = new TitledPane("Deposit / withdraw (selected account)", moneyBox);
        money.setCollapsible(false);

        // open account
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("SAVINGS", "INVESTMENT", "CHEQUE"));
        type.setPromptText("Account type");
        TextField branch = new TextField();
        TextField opening = new TextField("0");
        TextField employer = new TextField();
        TextField employerAddress = new TextField();
        Label employerLabel = new Label("Employer");
        Label employerAddressLabel = new Label("Employer address");

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.addRow(0, new Label("Type"), type);
        form.addRow(1, new Label("Branch"), branch);
        form.addRow(2, new Label("Opening deposit"), opening);
        form.addRow(3, employerLabel, employer);
        form.addRow(4, employerAddressLabel, employerAddress);
        setEmployerVisible(false, employerLabel, employer, employerAddressLabel, employerAddress);
        type.valueProperty().addListener((obs, old, val) ->
                setEmployerVisible("CHEQUE".equals(val), employerLabel, employer, employerAddressLabel, employerAddress));

        Button open = new Button("Open account");
        open.setOnAction(e -> {
            try {
                Account account = controller.openAccount(customer, type.getValue(), branch.getText(),
                        opening.getText(), employer.getText(), employerAddress.getText());
                refreshTable();
                table.getSelectionModel().select(account);
                showOk("Opened " + account.getAccountType().toLowerCase() + " account " + account.getAccountNumber() + ".");
                branch.clear();
                opening.setText("0");
                employer.clear();
                employerAddress.clear();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (DataAccessException ex) {
                showError("Could not save the account. Please try again.");
            }
        });
        TitledPane openPane = new TitledPane("Open a new account", new VBox(8, form, open));
        openPane.setCollapsible(false);

        // interest
        Button interest = new Button("Pay monthly interest");
        interest.setOnAction(e -> {
            try {
                double total = controller.payMonthlyInterest(customer);
                refreshTable();
                showOk(String.format("Interest of BWP %,.2f paid into your Savings and Investment accounts.", total));
            } catch (DataAccessException ex) {
                showError("Interest could not be saved. No balances were changed in the database.");
            }
        });

        VBox right = new VBox(12, money, openPane, interest);
        right.setPadding(new Insets(0, 16, 16, 8));
        right.setPrefWidth(340);
        return right;
    }

    private interface Action { String execute(); }

    /** Runs a controller action and shows its success or error message in the status bar. */
    private void run(Action action, TextField toClear) {
        try {
            String msg = action.execute();
            Account keep = selected();
            refreshTable();
            table.getSelectionModel().select(keep);
            toClear.clear();
            showOk(msg);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (DataAccessException ex) {
            showError("The database is unavailable. Please try again later.");
        }
    }

    private Account selected() {
        return table.getSelectionModel().getSelectedItem();
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(customer.getAccounts()));
        table.refresh();
        refreshHistory();
    }

    private void refreshHistory() {
        Account account = selected();
        if (account == null) {
            history.getItems().clear();
        } else {
            try {
                history.setItems(FXCollections.observableArrayList(controller.getHistory(account)));
            } catch (DataAccessException ex) {
                showError("Could not load the history.");
            }
        }
    }

    private void showOk(String text) {
        status.getStyleClass().setAll("label", "ok");
        status.setText(text);
    }

    private void showError(String text) {
        status.getStyleClass().setAll("label", "error");
        status.setText(text);
    }

    private static void setEmployerVisible(boolean visible, javafx.scene.Node... nodes) {
        for (javafx.scene.Node n : nodes) {
            n.setVisible(visible);
            n.setManaged(visible);
        }
    }

    private static TableColumn<Account, String> column(String title, java.util.function.Function<Account, String> getter) {
        TableColumn<Account, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(getter.apply(data.getValue())));
        return col;
    }

    public Parent getRoot() {
        return root;
    }
}
