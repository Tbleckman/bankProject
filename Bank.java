public interface Bank {
    // All of the methods that I want each bank to have so that in the interface file
    // So that I can operate any bank account type dynamically
    public double getBankDeposit();
    public void changeFundsUp(double val);
    public void changeFundsDown(double val);
    public void wipeTransactions();
    public String getTransactions();
    public String getName();
    public void lowerBankNum();
    public void setTransactions(double[] arr);
}