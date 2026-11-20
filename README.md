# Ooad – Banking System (CSE202 Part B)

A desktop banking system written in **Java 21 + JavaFX**, using **JDBC** with an embedded **H2** database.
Customers can log in or register, open Savings, Investment and Cheque accounts, deposit, withdraw,
pay monthly interest and view each account's transaction history.

## How to run

You need **JDK 21** and **Maven 3.9+**.

```bash
mvn javafx:run      # start the application
mvn test            # run the unit and integration tests
```

On the first start the database file `data/bankdb.mv.db` is created and loaded with 10 sample customers.
Delete the `data/` folder to reset to the sample data.

### Running in GitHub Codespaces

1. On GitHub, open **Code → Codespaces → Create codespace on this branch**.
2. Wait for the container to build (it compiles the project automatically).
3. Open the **Ports** tab, then open port **6080** in the browser (desktop password: `vscode`).
4. In the Codespaces terminal run `mvn javafx:run`. The window appears in the desktop tab.

## Sample logins

Every sample customer's password is `password123`.

| Username | Name | Accounts |
|---|---|---|
| kabo | Kabo Molefe | SAV-1001, INV-2001, CHQ-3001 |
| neo | Neo Sebina | SAV-1002 |
| lorato | Lorato Kgosi | INV-2002, CHQ-3002 |
| tumelo | Tumelo Dube | SAV-1003 |
| boitumelo | Boitumelo Phiri | CHQ-3003 |
| onalenna | Onalenna Mosweu | INV-2003 |
| kagiso | Kagiso Ntsima | SAV-1004 |
| mpho | Mpho Seretse | CHQ-3004 |
| masego | Masego Ramotswa | SAV-1005 |
| thato | Thato Kebonang | INV-2004 |

## Business rules

| Account | Interest (monthly) | Withdrawals | Opening rule |
|---|---|---|---|
| Savings | 0.05 % | Not allowed | Any amount |
| Investment | 5 % | Allowed up to the balance | At least BWP 500 |
| Cheque | None | Allowed up to the balance | Employer name and address required |

## Project structure (MVC)

```
src/main/java/com/bank/
├── model/        Account (abstract), SavingsAccount, InvestmentAccount, ChequeAccount,
│                 InterestBearing (interface), Customer, Transaction, PasswordUtil
├── dao/          Database, CustomerDAO, AccountDAO, TransactionDAO, DataAccessException
├── controller/   LoginController, AccountController
└── view/         BankApp, LoginView, RegisterView, AccountView   (JavaFX)
src/main/resources/
├── schema.sql        tables, keys and CHECK constraints
├── sample-data.sql   10 sample customers and 13 accounts
└── style.css
src/test/java/com/bank/
├── model/AccountModelTest.java               unit tests
└── integration/BankingIntegrationTest.java   controllers + DAOs + in-memory H2
```

- **View** classes only build the screen and pass user input to a controller. They contain no business rules.
- **Controllers** validate input, call the model and save the result through the DAOs.
- **Model** classes hold the business rules (for example, Savings accounts refuse withdrawals).
- **DAOs** run all SQL with `PreparedStatement`, which protects against SQL injection.

## Where the OOP principles are used

| Principle | Where |
|---|---|
| Abstraction | `Account` is abstract, with the abstract methods `withdraw()` and `getAccountType()`. `InterestBearing` is an interface. |
| Encapsulation | Fields are private or protected. The balance changes only through `deposit()` and `withdraw()`. Passwords are stored only as SHA-256 hashes. |
| Inheritance | `SavingsAccount`, `InvestmentAccount` and `ChequeAccount` extend `Account`. |
| Polymorphism | `AccountController.withdraw()` calls `account.withdraw()` and each subclass applies its own rule. Interest is paid through the `InterestBearing` interface. |
| Composition | A `Customer` owns its `Account`s. In the database, `ON DELETE CASCADE` removes the accounts when the customer is deleted. |

## Database

The schema is in `src/main/resources/schema.sql`. It has three tables:

- **customers**
- **accounts**: all three account types share this table, and the `account_type` column says which one a row is.
- **transactions**

Constraints:

- A balance can never be negative.
- The account type must be one of the three known types.
- A cheque account must have an employer.
