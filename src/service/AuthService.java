package service;

import data.DatabaseHelper;

public class AuthService {
    public static boolean authenticate(String username, String password) {
        return DatabaseHelper.authenticate(username, password);
    }
}
