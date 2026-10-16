public abstract class Account {
    protected String accountNumber;
    protected double balance;
    protected String branch;
    protected Customer customer;

    public Account(String accountNumber, double initialBalance, String branch, Customer customer) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.branch = branch;
        this.customer = customer;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            System.out.println("Deposited BWP " + amount + ". New balance: BWP " + this.balance);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    // Abstract method to be implemented by subclasses
    public abstract boolean withdraw(double amount);

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public String getBranch() { return branch; }
    public Customer getCustomer() { return customer; }
    
    public void setBranch(String branch) { this.branch = branch; }
}