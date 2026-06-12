package model;
import java.sql.Timestamp;

public class Transaction {
    private int id;
    private String type;
    private double amount;
    private String relatedAccount;
    private String message;
    private Timestamp date;

    public Transaction(int id, String type, double amount, String relatedAccount, String message, Timestamp date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.relatedAccount = relatedAccount;
        this.message = message;
        this.date = date;
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getRelatedAccount() { return relatedAccount; } // Getter mới
    public String getMessage() { return message; }
    public Timestamp getDate() { return date; }

    @Override
    public String toString() {
        return type + " - " + amount;
    }
}