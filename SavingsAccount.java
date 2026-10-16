public class SavingsAccount extends Account implements InterestBearing {
    private static final double INTEREST_RATE = 0.0005; // 0.05%

    public SavingsAccount(String accountNumber, double initialBalance, String branch, Customer customer) {
        super(accountNumber, initialBalance, branch, customer);
    }

    @Override
    public boolean withdraw(double amount) {
        // Savings accounts do not allow withdrawals
        System.out.println("Withdrawals are not permitted from a Savings Account.");
        return false; 
    }

    @Override
    public double calculateInterest() {
        return this.balance * INTEREST_RATE;
    }
}