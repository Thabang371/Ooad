public class InvestmentAccount extends Account implements InterestBearing {
    private static final double INTEREST_RATE = 0.05; // 5%
    private static final double MIN_INITIAL_DEPOSIT = 500.00;

    public InvestmentAccount(String accountNumber, double initialBalance, String branch, Customer customer) {
        super(accountNumber, initialBalance, branch, customer);
        if (initialBalance < MIN_INITIAL_DEPOSIT) {
            throw new IllegalArgumentException("Investment accounts require a minimum initial deposit of BWP 500.00");
        }
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            System.out.println("Withdrew BWP " + amount + ". New balance: BWP " + this.balance);
            return true;
        }
        System.out.println("Insufficient funds or invalid amount for withdrawal.");
        return false;
    }

    @Override
    public double calculateInterest() {
        return this.balance * INTEREST_RATE;
    }
}