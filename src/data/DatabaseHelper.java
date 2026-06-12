package data;
import java.sql.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/minibank";
    private static final String USER = "root";
    private static final String PASSWORD = "240405";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. Add MySQL Connector/J to the classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    //ĐĂNG NHẬP
    public static boolean authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        // Sử dụng try-with-resources để tự động đóng kết nối
        try (java.sql.Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            java.sql.ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //QUẢN LÝ KHÁCH HÀNG
    public static boolean addCustomer(String name, String dob, String address, String phone, String cardId) {
        String sql = "INSERT INTO customers (full_name, dob, address, phone, identity_card) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dob);
            pstmt.setString(3, address);
            pstmt.setString(4, phone);
            pstmt.setString(5, cardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean updateCustomer(int id, String name, String dob, String address, String phone, String cardId) {
        String sql = "UPDATE customers SET full_name=?, dob=?, address=?, phone=?, identity_card=? WHERE customer_id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dob);
            pstmt.setString(3, address);
            pstmt.setString(4, phone);
            pstmt.setString(5, cardId);
            pstmt.setInt(6, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public static String checkDuplicateCustomer(String phone, String idCard) {
        String sql = "SELECT full_name, phone, identity_card FROM customers WHERE phone = ? OR identity_card = ?";

        try (Connection conn = getConnection();java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setString(2, idCard);

            java.sql.ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String existingPhone = rs.getString("phone");
                String existingId = rs.getString("identity_card");

                if (phone.equals(existingPhone)) {
                    return "Lỗi: Số điện thoại " + phone + " đã được đăng ký!";
                }
                if (idCard.equals(existingId)) {
                    return "Lỗi: Số CCCD/CMND " + idCard + " đã tồn tại trong hệ thống!";
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi kiểm tra dữ liệu: " + e.getMessage();
        }
    }

    public static boolean deleteCustomer(int customerId) {
        String sqlTrans = "DELETE FROM transactions WHERE account_id IN (SELECT account_id FROM accounts WHERE customer_id = ?)";
        String sqlAcc = "DELETE FROM accounts WHERE customer_id = ?";
        String sqlCust = "DELETE FROM customers WHERE customer_id = ?";
        try (java.sql.Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (java.sql.PreparedStatement p1 = conn.prepareStatement(sqlTrans)) {
                    p1.setInt(1, customerId);
                    p1.executeUpdate();
                }
                try (java.sql.PreparedStatement p2 = conn.prepareStatement(sqlAcc)) {
                    p2.setInt(1, customerId);
                    p2.executeUpdate();
                }
                try (java.sql.PreparedStatement p3 = conn.prepareStatement(sqlCust)) {
                    p3.setInt(1, customerId);
                    int rows = p3.executeUpdate();
                    if (rows == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback(); 
                e.printStackTrace();
                return false;
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet searchCustomers(String keyword) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT * FROM customers WHERE full_name LIKE ? OR phone LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            return pstmt.executeQuery();
        } catch (SQLException e) { return null; }
    }

    //QUẢN LÝ TÀI KHOẢN
    public static ResultSet getAllAccounts() {
        try {
            Connection conn = getConnection();
            String sql = "SELECT a.account_id, a.balance, a.status, " +
                    "c.full_name, c.phone, c.dob, c.address, c.identity_card " +
                    "FROM accounts a " +
                    "JOIN customers c ON a.customer_id = c.customer_id " +
                    "ORDER BY a.account_id DESC";
            return conn.prepareStatement(sql).executeQuery();
        } catch (SQLException e) { return null; }
    }

    public static ResultSet getAccountsByCustomerId(int customerId) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT a.account_id, a.balance, a.status, " +
                    "c.full_name, c.phone, c.dob, c.address, c.identity_card " +
                    "FROM accounts a " +
                    "JOIN customers c ON a.customer_id = c.customer_id " +
                    "WHERE a.customer_id = ? " +
                    "ORDER BY a.account_id DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            return pstmt.executeQuery();
        } catch (SQLException e) { return null; }
    }

    // CẬP NHẬT SỐ DƯ
    public static void updateBalanceInDB(String accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // GIAO DỊCH
    public static void logTransactionToDB(String accountId, String type, double amount, String relatedAcc, String message) {
        String sql = "INSERT INTO transactions (account_id, type, amount, related_account_id, message) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, relatedAcc);
            pstmt.setString(5, message);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static ResultSet getAllTransactions() {
        try {
            Connection conn = getConnection();
            String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
            return conn.prepareStatement(sql).executeQuery();
        } catch (SQLException e) { return null; }
    }

    public static boolean isAccountExist(String accId) {
        try (Connection conn = getConnection()) {
            PreparedStatement p = conn.prepareStatement("SELECT 1 FROM accounts WHERE account_id = ?");
            p.setString(1, accId);
            return p.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    // Tạo tài khoản mới
    public static boolean createAccount(String accId, int custId, double balance) {
        // Bỏ cột account_type trong câu INSERT
        String sql = "INSERT INTO accounts (account_id, customer_id, balance, status) VALUES (?, ?, ?, 'HOAT_DONG')";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accId);
            pstmt.setInt(2, custId);
            pstmt.setDouble(3, balance);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Cập nhật tài khoản
    public static boolean updateAccount(String accId, double newBalance, boolean isLocked) throws SQLException {
        String status = isLocked ? "KHOA" : "HOAT_DONG";
        String sql = "UPDATE accounts SET balance = ?, status = ? WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, status);
            pstmt.setString(3, accId);
            return pstmt.executeUpdate() > 0;
        }
    }
    //xóa tài khoản
    public static void deleteAccount(String accId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement p1 = conn.prepareStatement("DELETE FROM transactions WHERE account_id = ? OR related_account_id = ?");
            p1.setString(1, accId);
            p1.setString(2, accId);
            p1.executeUpdate();
            PreparedStatement p2 = conn.prepareStatement("DELETE FROM accounts WHERE account_id = ?");
            p2.setString(1, accId);
            p2.executeUpdate();
            conn.commit();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    // Kiểm tra tồn tại của khách hàng
    public static boolean isCustomerExist(int customerId) {
        try (Connection conn = getConnection()) {
            PreparedStatement p = conn.prepareStatement("SELECT 1 FROM customers WHERE customer_id = ?");
            p.setInt(1, customerId);
            return p.executeQuery().next(); // Trả về true nếu tìm thấy khách hàng
        } catch (SQLException e) { return false; }
    }

    public static ResultSet getAllCustomers() {
        try {
            return getConnection().createStatement().executeQuery("SELECT * FROM customers ORDER BY customer_id DESC");
        } catch (SQLException e) { return null; }
    }

    // Nạp tiền
    public static boolean performDeposit(String accId, double amount) {
        String sqlUpdate = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        String sqlLog = "INSERT INTO transactions (account_id, type, amount, related_account_id, message) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction
            try (PreparedStatement p1 = conn.prepareStatement(sqlUpdate)) {
                p1.setDouble(1, amount);
                p1.setString(2, accId);
                p1.executeUpdate();
            }
            try (PreparedStatement p2 = conn.prepareStatement(sqlLog)) {
                p2.setString(1, accId);
                p2.setString(2, "NAP_TIEN");
                p2.setDouble(3, amount);
                p2.setString(4, null); // Nạp tiền không có người gửi/nhận
                p2.setString(5, "Nạp tiền mặt tại quầy");
                p2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    //Rút tiền  
    public static boolean performWithdraw(String accId, double amount) {
        String sqlUpdate = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        String sqlLog = "INSERT INTO transactions (account_id, type, amount, related_account_id, message) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(sqlUpdate)) {
                p1.setDouble(1, amount);
                p1.setString(2, accId);
                p1.executeUpdate();
            }
            try (PreparedStatement p2 = conn.prepareStatement(sqlLog)) {
                p2.setString(1, accId);
                p2.setString(2, "RUT_TIEN");
                p2.setDouble(3, amount);
                p2.setString(4, null);
                p2.setString(5, "Rút tiền mặt");
                p2.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Chuyển tiền
    public static boolean performTransfer(String sourceId, String targetId, double amount) {
        String sqlMinus = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        String sqlPlus = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        String sqlLog = "INSERT INTO transactions (account_id, type, amount, related_account_id, message) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {conn = getConnection();conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(sqlMinus)) {
                p1.setDouble(1, amount);
                p1.setString(2, sourceId);
                p1.executeUpdate();
            }
            try (PreparedStatement p2 = conn.prepareStatement(sqlPlus)) {
                p2.setDouble(1, amount);
                p2.setString(2, targetId);
                p2.executeUpdate();
            }
            try (PreparedStatement p3 = conn.prepareStatement(sqlLog)) {
                p3.setString(1, sourceId);
                p3.setString(2, "CHUYEN_DI");
                p3.setDouble(3, amount);
                p3.setString(4, targetId);
                p3.setString(5, "Chuyển tới " + targetId);
                p3.executeUpdate();
            }
            try (PreparedStatement p4 = conn.prepareStatement(sqlLog)) {
                p4.setString(1, targetId);
                p4.setString(2, "NHAN_TIEN");
                p4.setDouble(3, amount);
                p4.setString(4, sourceId);
                p4.setString(5, "Nhận từ " + sourceId);
                p4.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    // Kiểm tra tài khoản
    public static boolean isAccountLocked(String accId) {
        String sql = "SELECT status FROM accounts WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, accId);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                return !"HOAT_DONG".equalsIgnoreCase(status);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return true;
    }

    // Lấy giao dịch theo tài khoản
    public static ResultSet getTransactionsByAccount(String accId) {
        try {
            Connection conn = getConnection();
            // Lấy tất cả giao dịch mà account_id này là chủ thể (người nạp, rút, gửi hoặc nhận)
            String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, accId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lấy số dư hiện tại của 1 tài khoản
    public static Double getAccountBalance(String accId) {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, accId);
            ResultSet rs = p.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}