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




    // 3. CHUYỂN KHOẢN (Quan trọng)
    public void transfer(Account receiver, double amount) throws InsufficientFundsException {
        if (amount > balance) {
            throw new InsufficientFundsException("Số dư không đủ để chuyển!");
        }

        // Trừ tiền người gửi
        this.balance -= amount;
        DatabaseHelper.updateBalanceInDB(this.accountNumber, this.balance);

        // Cộng tiền người nhận
        receiver.balance += amount;
        DatabaseHelper.updateBalanceInDB(receiver.getAccountNumber(), receiver.balance);

        // Ghi log CHO NGƯỜI GỬI (Loại: CHUYỂN ĐI)
        // relatedAccount = Tài khoản người nhận
        DatabaseHelper.logTransactionToDB(
                this.accountNumber,
                "CHUYỂN ĐI",
                amount,
                receiver.getAccountNumber(),
                "Chuyển tới " + receiver.getOwnerName()
        );
        // Ghi log CHO NGƯỜI NHẬN (Loại: NHẬN TIỀN)
        // relatedAccount = Tài khoản người gửi
        DatabaseHelper.logTransactionToDB(
                receiver.getAccountNumber(),
                "NHẬN TIỀN",
                amount,
                this.accountNumber,
                "Nhận từ " + this.getOwnerName()
        );
    }
}