package service;

import data.DatabaseHelper;

import java.sql.ResultSet;

public class TransactionService {
    public static boolean deposit(String accountId, double amount) {
        return DatabaseHelper.performDeposit(accountId, amount);
    }

    public static boolean withdraw(String accountId, double amount) {
        return DatabaseHelper.performWithdraw(accountId, amount);
    }

    public static boolean transfer(String sourceAccountId, String targetAccountId, double amount) {
        // Basic checks at service layer
        if (sourceAccountId == null || targetAccountId == null) return false;
        if (sourceAccountId.equals(targetAccountId)) return false;

        // Check lock status
        if (DatabaseHelper.isAccountLocked(sourceAccountId) || DatabaseHelper.isAccountLocked(targetAccountId)) return false;

        // Check balance
        Double bal = DatabaseHelper.getAccountBalance(sourceAccountId);
        if (bal == null || bal < amount) return false;

        return DatabaseHelper.performTransfer(sourceAccountId, targetAccountId, amount);
    }

    public static boolean isAccountLocked(String accountId) {
        return DatabaseHelper.isAccountLocked(accountId);
    }

    /**
     * Perform transfer but return a reason code for UI display.
     * Possible return values: SUCCESS, INVALID, SAME_ACCOUNT, ACCOUNT_LOCKED, INSUFFICIENT_FUNDS, DB_ERROR
     */
    public static String transferWithReason(String sourceAccountId, String targetAccountId, double amount) {
        if (sourceAccountId == null || targetAccountId == null) return "INVALID";
        if (sourceAccountId.equals(targetAccountId)) return "SAME_ACCOUNT";

        if (DatabaseHelper.isAccountLocked(sourceAccountId) || DatabaseHelper.isAccountLocked(targetAccountId)) return "ACCOUNT_LOCKED";

        Double bal = DatabaseHelper.getAccountBalance(sourceAccountId);
        if (bal == null) return "INVALID";
        if (bal < amount) return "INSUFFICIENT_FUNDS";

        boolean ok = DatabaseHelper.performTransfer(sourceAccountId, targetAccountId, amount);
        return ok ? "SUCCESS" : "DB_ERROR";
    }

    public static ResultSet getAllTransactions() {
        return DatabaseHelper.getAllTransactions();
    }

    public static ResultSet getTransactionsByAccount(String accountId) {
        return DatabaseHelper.getTransactionsByAccount(accountId);
    }
}
