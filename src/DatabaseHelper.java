import java.sql.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/minibank";
    private static final String USER = "root";
    private static final String PASSWORD = "240405";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    //ĐĂNG NHẬP
    public static String authenticate(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
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

    public static void deleteCustomer(int customerId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Xóa User liên quan
            PreparedStatement p0 = conn.prepareStatement("DELETE FROM users WHERE customer_id=?");
            p0.setInt(1, customerId);
            p0.executeUpdate();

            // Xóa Tài khoản
            PreparedStatement p1 = conn.prepareStatement("DELETE FROM accounts WHERE customer_id=?");
            p1.setInt(1, customerId);
            p1.executeUpdate();

            // Xóa Khách hàng
            PreparedStatement p2 = conn.prepareStatement("DELETE FROM customers WHERE customer_id=?");
            p2.setInt(1, customerId);
            p2.executeUpdate();

            conn.commit();
        } catch (SQLException e) { e.printStackTrace(); }
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

    // Kiểm tra tài khoản tồn tại chưa
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
        // 1. Xử lý trạng thái (Checkbox -> String)
        String status = isLocked ? "KHOA" : "HOAT_DONG";

        // 2. Câu lệnh SQL
        // Chỉ update cột 'balance' và 'status'.
        // Điều kiện WHERE là tìm theo 'account_number' (hoặc 'account_id' tùy tên cột trong DB của bạn)
        String sql = "UPDATE accounts SET balance = ?, status = ? WHERE account_id = ?";

        // Lưu ý: Mình dùng biến 'connection' static của class (đã sửa lỗi connection null ở bước trước)
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            // Tham số 1: Số dư mới
            pstmt.setDouble(1, newBalance);

            // Tham số 2: Trạng thái mới
            pstmt.setString(2, status);

            // Tham số 3: Số TK dùng để tìm kiếm (WHERE)
            pstmt.setString(3, accId);

            return pstmt.executeUpdate() > 0; // Trả về true nếu update thành công
        }
    }
    // Xóa tài khoản (Cần xóa giao dịch liên quan trước)
    public static void deleteAccount(String accId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // 1. Xóa giao dịch liên quan đến TK này
            PreparedStatement p1 = conn.prepareStatement("DELETE FROM transactions WHERE account_id = ? OR related_account_id = ?");
            p1.setString(1, accId);
            p1.setString(2, accId);
            p1.executeUpdate();

            // 2. Xóa tài khoản
            PreparedStatement p2 = conn.prepareStatement("DELETE FROM accounts WHERE account_id = ?");
            p2.setString(1, accId);
            p2.executeUpdate();

            conn.commit();
        } catch (SQLException e) { e.printStackTrace(); }
    }

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

    // 1. NẠP TIỀN
    public static boolean performDeposit(String accId, double amount) {
        String sqlUpdate = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        String sqlLog = "INSERT INTO transactions (account_id, type, amount, related_account_id, message) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Bước 1: Cộng tiền
            try (PreparedStatement p1 = conn.prepareStatement(sqlUpdate)) {
                p1.setDouble(1, amount);
                p1.setString(2, accId);
                p1.executeUpdate();
            }

            // Bước 2: Ghi log (Dùng chung conn để không bị Lock)
            try (PreparedStatement p2 = conn.prepareStatement(sqlLog)) {
                p2.setString(1, accId);
                p2.setString(2, "NAP_TIEN");
                p2.setDouble(3, amount);
                p2.setString(4, null); // Nạp tiền không có người gửi/nhận
                p2.setString(5, "Nạp tiền mặt tại quầy");
                p2.executeUpdate();
            }

            conn.commit(); // Xác nhận thành công
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // 2. RÚT TIỀN
    public static boolean performWithdraw(String accId, double amount) {
        String sqlUpdate = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        String sqlLog = "INSERT INTO transactions (account_id, type, amount, related_account_id, message) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Bước 1: Trừ tiền
            try (PreparedStatement p1 = conn.prepareStatement(sqlUpdate)) {
                p1.setDouble(1, amount);
                p1.setString(2, accId);
                p1.executeUpdate();
            }

            // Bước 2: Ghi log
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

    // 3. CHUYỂN KHOẢN
    public static boolean performTransfer(String sourceId, String targetId, double amount) {
        String sqlMinus = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        String sqlPlus = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        String sqlLog = "INSERT INTO transactions (account_id, type, amount, related_account_id, message) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Bước 1: Trừ tiền người gửi
            try (PreparedStatement p1 = conn.prepareStatement(sqlMinus)) {
                p1.setDouble(1, amount);
                p1.setString(2, sourceId);
                p1.executeUpdate();
            }

            // Bước 2: Cộng tiền người nhận
            try (PreparedStatement p2 = conn.prepareStatement(sqlPlus)) {
                p2.setDouble(1, amount);
                p2.setString(2, targetId);
                p2.executeUpdate();
            }

            // Bước 3: Ghi log cho người gửi (CHUYEN_DI)
            try (PreparedStatement p3 = conn.prepareStatement(sqlLog)) {
                p3.setString(1, sourceId);
                p3.setString(2, "CHUYEN_DI");
                p3.setDouble(3, amount);
                p3.setString(4, targetId);
                p3.setString(5, "Chuyển tới " + targetId);
                p3.executeUpdate();
            }

            // Bước 4: Ghi log cho người nhận (NHAN_TIEN)
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
    // --- 7. KIỂM TRA TRẠNG THÁI TÀI KHOẢN (Mới thêm) ---
    public static boolean isAccountLocked(String accId) {
        String sql = "SELECT status FROM accounts WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, accId);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                // Nếu status khác "HOAT_DONG" thì coi như là bị khóa
                return !"HOAT_DONG".equalsIgnoreCase(status);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return true; // Nếu lỗi coi như khóa cho an toàn
    }

    // --- 8. LẤY SAO KÊ CỦA 1 TÀI KHOẢN ---
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
}