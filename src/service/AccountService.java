package service;

import data.DatabaseHelper;
import model.Account;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountService {
    public static boolean createAccount(Account account, int customerId) {
        return DatabaseHelper.createAccount(account.getAccountNumber(), customerId, account.getBalance());
    }

    public static boolean updateAccount(Account account, boolean isLocked) {
        try {
            return DatabaseHelper.updateAccount(account.getAccountNumber(), account.getBalance(), isLocked);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteAccount(String accountId) {
        DatabaseHelper.deleteAccount(accountId);
    }

    public static boolean isAccountExist(String accountId) {
        return DatabaseHelper.isAccountExist(accountId);
    }

    public static boolean isCustomerExist(int customerId) {
        return DatabaseHelper.isCustomerExist(customerId);
    }

    public static ResultSet getAllAccounts() {
        return DatabaseHelper.getAllAccounts();
    }

    public static ResultSet getAccountsByCustomerId(int customerId) {
        return DatabaseHelper.getAccountsByCustomerId(customerId);
    }
}
