package model;

public class Account {
    private String id;
    private String accountNumber;
    private String ownerName;
    private double balance;

    public Account(String id, String accountNumber, String ownerName, double balance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    // Getters giữ nguyên
    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public double getBalance() { return balance; }

}