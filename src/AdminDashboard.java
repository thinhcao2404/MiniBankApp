import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class AdminDashboard extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable tblCustomer, tblAccount, tblTransaction;
    private DefaultTableModel modelCustomer, modelAccount, modelTransaction;

    // Các ô nhập liệu
    private JTextField txtName, txtDob, txtAddress, txtPhone, txtCardId, txtSearchCustomer;
    private int selectedCustomerId = -1;

    public AdminDashboard() {
        setTitle("Mini Bank — Vai trò: ADMIN");
        setSize(1100, 750); // Mở rộng frame chút cho thoáng
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Padding cho tab để không bị dính sát
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));

        tabbedPane.addTab("Khách hàng", createCustomerPanel());
        tabbedPane.addTab("Tài khoản", createAccountPanel());
        tabbedPane.addTab("Giao dịch", createTransactionPanel());
        tabbedPane.addTab("Sao kê", createStatementPanel());

        add(tabbedPane);

        loadCustomerData("");
        loadAccountData(null);
    }

    // ========================================================================
    // TAB KHÁCH HÀNG
    // ========================================================================
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        // --- 1. TOP: TÌM KIẾM ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Tìm KH: "));
        txtSearchCustomer = new JTextField(25);
        topPanel.add(txtSearchCustomer);

        JButton btnSearch = new JButton("Tìm kiếm");
        styleButton(btnSearch, new Color(23, 162, 184)); // Màu Cyan
        topPanel.add(btnSearch);
        panel.add(topPanel, BorderLayout.NORTH);

        // --- 2. CENTER: BẢNG DỮ LIỆU (FIX MÀU HEADER) ---
        String[] cols = {"ID", "Họ tên", "Điện thoại", "Ngày sinh", "Địa chỉ", "Số giấy tờ"};
        modelCustomer = new DefaultTableModel(cols, 0);
        tblCustomer = new JTable(modelCustomer);
        tblCustomer.setRowHeight(28);
        tblCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblCustomer.setFillsViewportHeight(true); // Quan trọng để hiển thị background

        // --- ĐOẠN CODE QUAN TRỌNG ĐỂ HIỆN MÀU XANH HEADER ---
        JTableHeader header = tblCustomer.getTableHeader();
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(new Color(0, 102, 204)); // Xanh dương đậm
                l.setForeground(Color.WHITE); // Chữ trắng
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setOpaque(true); // Bắt buộc phải có để hiện màu
                return l;
            }
        });

        panel.add(new JScrollPane(tblCustomer), BorderLayout.CENTER);

        // --- 3. BOTTOM: FORM NHẬP LIỆU ---
        JPanel bottomWrap = new JPanel(new BorderLayout());
        bottomWrap.setBackground(Color.WHITE);

        TitledBorder border = BorderFactory.createTitledBorder("THÊM KHÁCH HÀNG");
        border.setTitleColor(new Color(0, 102, 204));
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        border.setTitleJustification(TitledBorder.CENTER);
        bottomWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 0),
                border
        ));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Khởi tạo TextFields
        txtName = new JTextField(); txtDob = new JTextField("");
        txtAddress = new JTextField(); txtPhone = new JTextField(); txtCardId = new JTextField();

        addFormRow(form, "Họ tên:", txtName, 0, gbc);
        addFormRow(form, "Ngày sinh (dd-MM-yyyy):", txtDob, 1, gbc);
        addFormRow(form, "Địa chỉ:", txtAddress, 2, gbc);
        addFormRow(form, "Điện thoại:", txtPhone, 3, gbc);
        addFormRow(form, "Số giấy tờ:", txtCardId, 4, gbc);

        bottomWrap.add(form, BorderLayout.CENTER);

        // --- CÁC NÚT BẤM (FIX MÀU NỀN) ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setBackground(Color.WHITE);

        JButton btnAdd = new JButton("Thêm");
        styleButton(btnAdd, new Color(40, 167, 69)); // Xanh lá

        JButton btnEdit = new JButton("Sửa KH đã chọn");
        styleButton(btnEdit, new Color(255, 193, 7)); // Vàng
        btnEdit.setForeground(Color.BLACK); // Chữ đen

        JButton btnDel = new JButton("Xóa KH đã chọn");
        styleButton(btnDel, new Color(220, 53, 69)); // Đỏ

        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDel);
        bottomWrap.add(btnPanel, BorderLayout.SOUTH);
        panel.add(bottomWrap, BorderLayout.SOUTH);

        // === LOGIC EVENT ===
        tblCustomer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = tblCustomer.getSelectedRow();
                if (r >= 0) {
                    selectedCustomerId = Integer.parseInt(tblCustomer.getValueAt(r, 0).toString());
                    txtName.setText(tblCustomer.getValueAt(r, 1).toString());
                    txtPhone.setText(tblCustomer.getValueAt(r, 2).toString());
                    txtDob.setText(tblCustomer.getValueAt(r, 3).toString());
                    txtAddress.setText(tblCustomer.getValueAt(r, 4).toString());
                    txtCardId.setText(tblCustomer.getValueAt(r, 5).toString());
                }
            }
        });

        txtSearchCustomer.addActionListener(e -> loadCustomerData(txtSearchCustomer.getText()));
        btnSearch.addActionListener(e -> loadCustomerData(txtSearchCustomer.getText()));

        btnAdd.addActionListener(e -> {
            if(DatabaseHelper.addCustomer(txtName.getText(), txtDob.getText(), txtAddress.getText(), txtPhone.getText(), txtCardId.getText())) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!"); loadCustomerData(""); clearForm();
            } else JOptionPane.showMessageDialog(this, "Lỗi thêm!");
        });

        btnEdit.addActionListener(e -> {
            if(selectedCustomerId != -1 && DatabaseHelper.updateCustomer(selectedCustomerId, txtName.getText(), txtDob.getText(), txtAddress.getText(), txtPhone.getText(), txtCardId.getText())) {
                JOptionPane.showMessageDialog(this, "Sửa thành công!"); loadCustomerData(""); clearForm();
            }
        });

        btnDel.addActionListener(e -> {
            if(selectedCustomerId != -1 && JOptionPane.showConfirmDialog(this, "Xóa khách hàng này?", "Xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DatabaseHelper.deleteCustomer(selectedCustomerId); loadCustomerData(""); clearForm();
            }
        });

        return panel;
    }
    private void addFormRow(JPanel panel, String labelText, JTextField field, int y, GridBagConstraints gbc) {
        gbc.gridx = 0; // Cột 0: Label
        gbc.gridy = y; // Dòng y
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 10, 5, 10); // Khoảng cách
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label, gbc);

        gbc.gridx = 1; // Cột 1: TextField
        gbc.weightx = 1.0; // Giãn ra hết cỡ
        field.setPreferredSize(new Dimension(100, 30)); // Chiều cao ô nhập
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field, gbc);
    }
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 38));

        // 1. Tắt các chế độ vẽ mặc định xấu xí
        btn.setContentAreaFilled(false); // Tắt nền chữ nhật gốc
        btn.setFocusPainted(false);      // Tắt viền focus khi bấm
        btn.setBorderPainted(false);     // Tắt viền đen

        // 2. Can thiệp vào cách vẽ của nút (Override UI)
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Khử răng cưa cho đẹp
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Logic đổi màu khi nhấn hoặc di chuột
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                if (model.isPressed()) {
                    g2.setColor(b.getBackground().darker()); // Nhấn: Tối hơn
                } else if (model.isRollover()) {
                    g2.setColor(b.getBackground().brighter()); // Di chuột: Sáng hơn
                } else {
                    g2.setColor(b.getBackground()); // Bình thường
                }

                // Vẽ hình chữ nhật bo tròn (Bo góc 20px)
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);

                g2.dispose();

                // 3. Gọi lệnh vẽ chữ gốc của Java đè lên trên nền vừa vẽ
                super.paint(g, c);
            }
        });
    }

    private JTextField txtAccCustomerId, txtAccBalance, txtAccNumberCustom;
    private JTextField txtEditAccNumber, txtEditAccBalance, txtEditAccNewNumber;
    private JCheckBox chkAccStatus;
    // ========================================================================
    // TAB TÀI KHOẢN
    // ========================================================================
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        // --- A. PHẦN NHẬP LIỆU ---
        JPanel inputContainer = new JPanel();
        inputContainer.setLayout(new BoxLayout(inputContainer, BoxLayout.Y_AXIS));
        inputContainer.setBackground(Color.WHITE);

        // Dòng 1: Chọn Khách hàng (Giữ nguyên)
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p1.setBackground(Color.WHITE);
        p1.add(new JLabel("ID Khách:"));
        txtAccCustomerId = new JTextField(15);
        p1.add(txtAccCustomerId);
        JButton btnSelectCustomer = new JButton("Chọn từ KH đã chọn");
        styleButton(btnSelectCustomer, new Color(23, 162, 184));
        p1.add(btnSelectCustomer);
        JButton btnLoadAcc = new JButton("Tải danh sách TK");
        styleButton(btnLoadAcc, new Color(23, 162, 184));
        p1.add(btnLoadAcc);
        inputContainer.add(p1);

        // Dòng 2: Tạo tài khoản (BỎ LOẠI)
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p2.setBackground(Color.WHITE);

        // --- ĐÃ XÓA CBO LOẠI Ở ĐÂY ---

        p2.add(new JLabel("Số dư ban đầu:"));
        txtAccBalance = new JTextField("0", 10);
        p2.add(txtAccBalance);

        p2.add(new JLabel("Số TK (tự chọn):"));
        txtAccNumberCustom = new JTextField(10);
        p2.add(txtAccNumberCustom);

        JButton btnSuggest = new JButton("Gợi ý số TK");
        styleButton(btnSuggest, new Color(23, 162, 184));
        p2.add(btnSuggest);

        JButton btnCreate = new JButton("Tạo TK");
        styleButton(btnCreate, new Color(40, 167, 69));
        p2.add(btnCreate);
        inputContainer.add(p2);

        // Khung SỬA TÀI KHOẢN (BỎ LOẠI)
        JPanel editPanel = new JPanel(new BorderLayout());
        editPanel.setBackground(Color.WHITE);
        TitledBorder border = BorderFactory.createTitledBorder("SỬA TÀI KHOẢN");
        border.setTitleColor(new Color(0, 123, 255));
        editPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0), border));

        JPanel editForm = new JPanel(new GridBagLayout());
        editForm.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header cột sửa (Bỏ Loại)
        gbc.gridy = 0;
        gbc.gridx = 0; editForm.add(new JLabel("Số TK:"), gbc);
        gbc.gridx = 1; editForm.add(new JLabel("Số dư mới:"), gbc);
        gbc.gridx = 2; editForm.add(new JLabel("Trạng thái:"), gbc);

        // Input sửa (Bỏ Combo Loại)
        gbc.gridy = 1;
        txtEditAccNumber = new JTextField(12); txtEditAccNumber.setEditable(false);
        gbc.gridx = 0; editForm.add(txtEditAccNumber, gbc);

        txtEditAccBalance = new JTextField(12);
        gbc.gridx = 1; editForm.add(txtEditAccBalance, gbc);

        chkAccStatus = new JCheckBox("Khóa tài khoản");
        chkAccStatus.setBackground(Color.WHITE);
        gbc.gridx = 2; editForm.add(chkAccStatus, gbc);

        editPanel.add(editForm, BorderLayout.CENTER);

        // Nút bấm sửa/xóa (Giữ nguyên)
        JPanel editBtnPnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        editBtnPnl.setBackground(Color.WHITE);
        JButton btnUpdate = new JButton("Cập nhật"); styleButton(btnUpdate, new Color(255, 193, 7)); btnUpdate.setForeground(Color.BLACK);
        JButton btnDeleteAcc = new JButton("Xóa TK"); styleButton(btnDeleteAcc, new Color(220, 53, 69));
        editBtnPnl.add(btnUpdate); editBtnPnl.add(btnDeleteAcc);
        editPanel.add(editBtnPnl, BorderLayout.SOUTH);

        inputContainer.add(editPanel);
        panel.add(inputContainer, BorderLayout.NORTH);

        // --- B. BẢNG DỮ LIỆU (BỎ CỘT LOẠI) ---
        String[] cols = {"Số TK", "Số dư", "Trạng thái", "Họ tên KH", "Điện thoại", "Ngày sinh", "Địa chỉ", "Số giấy tờ"};
        modelAccount = new DefaultTableModel(cols, 0);
        tblAccount = new JTable(modelAccount);
        tblAccount.setRowHeight(25);

        // Style Header
        JTableHeader header = tblAccount.getTableHeader();
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(new Color(0, 102, 204)); l.setForeground(Color.WHITE); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setHorizontalAlignment(JLabel.CENTER); l.setOpaque(true);
                return l;
            }
        });
        panel.add(new JScrollPane(tblAccount), BorderLayout.CENTER);

        // --- XỬ LÝ SỰ KIỆN ---

        // 1. Click bảng -> Đổ lên form
        tblAccount.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = tblAccount.getSelectedRow();
                if (r >= 0) {
                    txtEditAccNumber.setText(tblAccount.getValueAt(r, 0).toString());
                    // Cột 1 là Số dư (do bỏ cột Loại)
                    String balanceStr = tblAccount.getValueAt(r, 1).toString().replace(",", "").replace(" đ", "");
                    txtEditAccBalance.setText(balanceStr);
                    String status = tblAccount.getValueAt(r, 2).toString();
                    chkAccStatus.setSelected(!status.equalsIgnoreCase("HOAT_DONG"));
                }
            }
        });

        // 2. Nút Tạo (Bỏ tham số type)
        btnCreate.addActionListener(e -> {
            try {
                String accId = txtAccNumberCustom.getText().trim();
                String custIdStr = txtAccCustomerId.getText().trim();
                double bal = Double.parseDouble(txtAccBalance.getText().trim());

                if (accId.isEmpty() || custIdStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Thiếu thông tin!"); return; }
                if (DatabaseHelper.isAccountExist(accId)) { JOptionPane.showMessageDialog(this, "Trùng số TK!"); return; }
                if (!DatabaseHelper.isCustomerExist(Integer.parseInt(custIdStr))) { JOptionPane.showMessageDialog(this, "Khách hàng không tồn tại!"); return; }

                // Gọi hàm mới không có type
                if (DatabaseHelper.createAccount(accId, Integer.parseInt(custIdStr), bal)) {
                    JOptionPane.showMessageDialog(this, "Tạo thành công!"); loadAccountData(null);
                    txtAccNumberCustom.setText(""); txtAccBalance.setText("0");
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi nhập liệu!"); }
        });

        // 3. Nút Cập nhật (Bỏ tham số type)
        btnUpdate.addActionListener(e -> {
            // 1. Lấy dữ liệu từ giao diện
            String accId = txtEditAccNumber.getText();
            double newBalance = Double.parseDouble(txtEditAccBalance.getText()); // Ô Số dư mới
            boolean isLocked = chkAccStatus.isSelected(); // Checkbox

            try {
                // 2. Gọi hàm vừa viết
                if (DatabaseHelper.updateAccount(accId, newBalance, isLocked)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    // Đóng form hoặc load lại bảng...
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy tài khoản!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Các nút khác (Gợi ý, Chọn KH, Tải danh sách, Xóa) giữ nguyên logic cũ
        btnSuggest.addActionListener(e -> txtAccNumberCustom.setText(String.valueOf((long)(Math.random()*90000000L)+10000000L)));
        btnSelectCustomer.addActionListener(e -> { if(selectedCustomerId!=-1) txtAccCustomerId.setText(String.valueOf(selectedCustomerId)); });
        btnLoadAcc.addActionListener(e -> {
            String cid = txtAccCustomerId.getText().trim();
            loadAccountData(cid.isEmpty() ? null : Integer.parseInt(cid));
        });
        btnDeleteAcc.addActionListener(e -> {
            if(!txtEditAccNumber.getText().isEmpty() && JOptionPane.showConfirmDialog(this,"Xóa?")==0) {
                DatabaseHelper.deleteAccount(txtEditAccNumber.getText()); loadAccountData(null);
            }
        });

        return panel;
    }
    // --- KHAI BÁO BIẾN TAB GIAO DỊCH ---
    private JPanel cardsPanel; // Panel chứa các giao diện con
    private CardLayout cardLayout;
    private JComboBox<String> cboTransType; // Chọn loại giao dịch

    // Component cho NẠP
    private JComboBox<ComboItem> cboDepCust, cboDepAcc;
    private JTextField txtDepAmount;
    private JLabel lblDepBalance;

    // Component cho RÚT
    private JComboBox<ComboItem> cboWdrCust, cboWdrAcc;
    private JTextField txtWdrAmount;
    private JLabel lblWdrBalance;

    // Component cho CHUYỂN
    private JComboBox<ComboItem> cboTraCustSource, cboTraAccSource;
    private JComboBox<ComboItem> cboTraCustTarget, cboTraAccTarget;
    private JTextField txtTraAmount;
    private JLabel lblTraBalanceSource, lblTraBalanceTarget;

    // --- BIẾN TAB SAO KÊ ---
    private JTextField txtStatementAccId;
    private JTable tblStatement;
    private DefaultTableModel modelStatement;

    // --- TAB GIAO DỊCH (Đã sửa lỗi NullPointerException + Thêm bảng ở dưới) ---
    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        // --- 1. PHẦN TRÊN: CHỌN LOẠI GIAO DỊCH ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // Khung tiêu đề xanh
        JPanel frameBlue = new JPanel(new FlowLayout(FlowLayout.CENTER));
        frameBlue.setPreferredSize(new Dimension(800, 40));
        frameBlue.setBackground(new Color(23, 162, 184));
        JLabel lblTitle = new JLabel("THỰC HIỆN GIAO DỊCH");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        frameBlue.add(lblTitle);

        // Ô chọn loại GD
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.add(new JLabel("Chọn loại giao dịch: "));
        cboTransType = new JComboBox<>(new String[]{"Nạp tiền", "Rút tiền", "Chuyển khoản"});
        cboTransType.setPreferredSize(new Dimension(150, 30));
        controlPanel.add(cboTransType);

        topPanel.add(frameBlue, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        // --- 2. PHẦN GIỮA: CÁC FORM NHẬP LIỆU (CARD LAYOUT) ---
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(Color.WHITE);

        // Thêm các panel con vào CardLayout
        cardsPanel.add(createDepositPanel(), "NAP");
        cardsPanel.add(createWithdrawPanel(), "RUT");
        cardsPanel.add(createTransferPanel(), "CHUYEN");

        // --- THAY ĐỔI: Đưa trực tiếp cardsPanel vào CENTER ---
        // (Đã xóa toàn bộ phần tạo bảng tblTransaction và tablePanel ở đây)
        panel.add(cardsPanel, BorderLayout.CENTER);


        // --- XỬ LÝ SỰ KIỆN ---
        cboTransType.addActionListener(e -> {
            String selected = (String) cboTransType.getSelectedItem();
            if ("Nạp tiền".equals(selected)) cardLayout.show(cardsPanel, "NAP");
            else if ("Rút tiền".equals(selected)) cardLayout.show(cardsPanel, "RUT");
            else if ("Chuyển khoản".equals(selected)) cardLayout.show(cardsPanel, "CHUYEN");
        });

        // Load dữ liệu Combo (Đảm bảo các hàm này đã tồn tại và chạy đúng)
        loadCustomersToCombo(cboDepCust);
        loadCustomersToCombo(cboWdrCust);
        loadCustomersToCombo(cboTraCustSource);
        loadCustomersToCombo(cboTraCustTarget);

        return panel;
    }
    // --- GIAO DIỆN NẠP TIỀN ---
    private JPanel createDepositPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(null, "NẠP TIỀN", TitledBorder.CENTER, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10); g.fill = GridBagConstraints.HORIZONTAL;

        cboDepCust = new JComboBox<>(); cboDepAcc = new JComboBox<>();
        txtDepAmount = new JTextField(); lblDepBalance = new JLabel("Số dư: 0 đ");
        JButton btnNap = new JButton("Nạp"); styleButton(btnNap, new Color(40, 167, 69));

        // Dòng 1: Chọn Khách
        g.gridx=0; g.gridy=0; p.add(new JLabel("Khách hàng nạp:"), g);
        g.gridx=1; g.weightx=1; p.add(cboDepCust, g);

        // Dòng 2: Chọn Tài khoản
        g.gridx=2; g.weightx=0; p.add(new JLabel("Tài khoản:"), g);
        g.gridx=3; g.weightx=1; p.add(cboDepAcc, g);
        g.gridx=4; g.weightx=0; p.add(lblDepBalance, g);

        // Dòng 3: Số tiền
        g.gridx=0; g.gridy=1; p.add(new JLabel("Số tiền:"), g);
        g.gridx=1; g.gridwidth=3; p.add(txtDepAmount, g);

        // Dòng 4: Nút Nạp
        g.gridx=4; g.gridwidth=1; p.add(btnNap, g);

        // Event
        setupComboEvents(cboDepCust, cboDepAcc, lblDepBalance);
        btnNap.addActionListener(e -> processTransaction("NAP", cboDepAcc, txtDepAmount, null));

        return p;
    }

    // --- GIAO DIỆN RÚT TIỀN ---
    private JPanel createWithdrawPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(null, "RÚT TIỀN", TitledBorder.CENTER, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10); g.fill = GridBagConstraints.HORIZONTAL;

        cboWdrCust = new JComboBox<>(); cboWdrAcc = new JComboBox<>();
        txtWdrAmount = new JTextField(); lblWdrBalance = new JLabel("Số dư: 0 đ");
        JButton btnRut = new JButton("Rút"); styleButton(btnRut, new Color(255, 193, 7)); btnRut.setForeground(Color.BLACK);

        // Layout tương tự Nạp
        g.gridx=0; g.gridy=0; p.add(new JLabel("Khách hàng rút:"), g);
        g.gridx=1; g.weightx=1; p.add(cboWdrCust, g);
        g.gridx=2; g.weightx=0; p.add(new JLabel("Tài khoản:"), g);
        g.gridx=3; g.weightx=1; p.add(cboWdrAcc, g);
        g.gridx=4; g.weightx=0; p.add(lblWdrBalance, g);

        g.gridx=0; g.gridy=1; p.add(new JLabel("Số tiền:"), g);
        g.gridx=1; g.gridwidth=3; p.add(txtWdrAmount, g);
        g.gridx=4; g.gridwidth=1; p.add(btnRut, g);

        // Event
        setupComboEvents(cboWdrCust, cboWdrAcc, lblWdrBalance);
        btnRut.addActionListener(e -> processTransaction("RUT", cboWdrAcc, txtWdrAmount, null));

        return p;
    }

    // --- GIAO DIỆN CHUYỂN KHOẢN ---
    private JPanel createTransferPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(null, "CHUYỂN KHOẢN", TitledBorder.CENTER, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10); g.fill = GridBagConstraints.HORIZONTAL;

        cboTraCustSource = new JComboBox<>(); cboTraAccSource = new JComboBox<>();
        cboTraCustTarget = new JComboBox<>(); cboTraAccTarget = new JComboBox<>();
        txtTraAmount = new JTextField();
        lblTraBalanceSource = new JLabel("Số dư: 0 đ"); lblTraBalanceTarget = new JLabel("Số dư: 0 đ");
        JButton btnChuyen = new JButton("Chuyển"); styleButton(btnChuyen, new Color(40, 167, 69));

        // --- CỘT TRÁI (NGƯỜI GỬI) ---
        g.gridx=0; g.gridy=0; p.add(new JLabel("Khách hàng chuyển:"), g);
        g.gridx=1; g.weightx=1; p.add(cboTraCustSource, g);

        g.gridx=0; g.gridy=1; p.add(new JLabel("Khách hàng nhận:"), g);
        g.gridx=1; p.add(cboTraCustTarget, g);

        g.gridx=0; g.gridy=2; p.add(new JLabel("Số tiền:"), g);
        g.gridx=1; p.add(txtTraAmount, g);

        // --- CỘT PHẢI (TÀI KHOẢN) ---
        g.gridx=2; g.gridy=0; g.weightx=0; p.add(new JLabel("Từ tài khoản:"), g);
        g.gridx=3; g.weightx=1; p.add(cboTraAccSource, g);
        g.gridx=4; g.weightx=0; p.add(lblTraBalanceSource, g);

        g.gridx=2; g.gridy=1; g.weightx=0; p.add(new JLabel("Đến tài khoản:"), g);
        g.gridx=3; g.weightx=1; p.add(cboTraAccTarget, g);
        g.gridx=4; g.weightx=0; p.add(lblTraBalanceTarget, g);

        // --- NÚT CHUYỂN (DÀI HẾT CỠ) ---
        g.gridx=2; g.gridy=2; g.gridwidth=3; g.fill = GridBagConstraints.BOTH;
        p.add(btnChuyen, g);

        // Event
        setupComboEvents(cboTraCustSource, cboTraAccSource, lblTraBalanceSource);
        setupComboEvents(cboTraCustTarget, cboTraAccTarget, lblTraBalanceTarget);

        btnChuyen.addActionListener(e -> processTransaction("CHUYEN", cboTraAccSource, txtTraAmount, cboTraAccTarget));

        return p;
    }

    private void loadCustomerData(String keyword) {
        modelCustomer.setRowCount(0);
        try (ResultSet rs = DatabaseHelper.searchCustomers(keyword)) {
            while (rs != null && rs.next()) {
                Vector<Object> v = new Vector<>();
                v.add(rs.getInt("customer_id"));
                v.add(rs.getString("full_name"));
                v.add(rs.getString("phone"));
                v.add(rs.getString("dob"));
                v.add(rs.getString("address"));
                v.add(rs.getString("identity_card"));
                modelCustomer.addRow(v);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- HÀM HIỂN THỊ DỮ LIỆU LÊN BẢNG ---
    private void loadAccountData(Integer customerId) {
        modelAccount.setRowCount(0);
        ResultSet rs = null;
        try {
            if (customerId == null) rs = DatabaseHelper.getAllAccounts();
            else rs = DatabaseHelper.getAccountsByCustomerId(customerId);

            while (rs != null && rs.next()) {
                Vector<Object> v = new Vector<>();
                // 1. Số TK
                v.add(rs.getString("account_id"));
                // 2. BỎ CỘT LOẠI
                // 3. Số dư
                v.add(String.format("%,.0f đ", rs.getDouble("balance")));
                // 4. Trạng thái
                v.add(rs.getString("status"));
                // 5. Thông tin KH
                v.add(rs.getString("full_name"));
                v.add(rs.getString("phone"));
                v.add(rs.getString("dob"));
                v.add(rs.getString("address"));
                v.add(rs.getString("identity_card"));
                modelAccount.addRow(v);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadTransactionData() {
        modelTransaction.setRowCount(0);
        try (ResultSet rs = DatabaseHelper.getAllTransactions()) {
            while (rs != null && rs.next()) {
                Vector<Object> v = new Vector<>();
                v.add(rs.getInt("transaction_id"));
                v.add(rs.getString("account_id"));
                v.add(rs.getString("type"));
                v.add(String.format("%,.0f", rs.getDouble("amount")));
                String related = rs.getString("related_account_id");
                v.add(related != null ? related : "-");
                v.add(rs.getString("message"));
                v.add(rs.getString("created_at"));
                modelTransaction.addRow(v);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void clearForm() {
        txtName.setText(""); txtDob.setText("01-01-2000"); txtAddress.setText("");
        txtPhone.setText(""); txtCardId.setText("");
        selectedCustomerId = -1;
    }
    // Class giúp lưu ID và Tên vào ComboBox
    private static class ComboItem {
        private String key; // ID (VD: customer_id hoặc account_id)
        private String value; // Tên hiển thị (VD: Nguyễn Văn A)

        public ComboItem(String key, String value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() { return key; }
        @Override
        public String toString() { return value; } // Cái này sẽ hiện lên ComboBox
    }
    // --- HÀM HỖ TRỢ LOAD DỮ LIỆU ---

    // 1. Đổ danh sách khách hàng vào ComboBox
    private void loadCustomersToCombo(JComboBox<ComboItem> cbo) {
        cbo.removeAllItems();
        try (ResultSet rs = DatabaseHelper.getAllCustomers()) { // Bạn cần public hàm này ở DatabaseHelper (hoặc dùng searchCustomers(""))
            // Tạm thời dùng searchCustomers("") nếu chưa thêm getAllCustomers
            try(ResultSet rs2 = DatabaseHelper.searchCustomers("")) {
                while (rs2 != null && rs2.next()) {
                    String id = String.valueOf(rs2.getInt("customer_id"));
                    String name = id + " - " + rs2.getString("full_name");
                    cbo.addItem(new ComboItem(id, name));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 2. Khi chọn Khách -> Load Tài khoản tương ứng
    private void setupComboEvents(JComboBox<ComboItem> cboCust, JComboBox<ComboItem> cboAcc, JLabel lblBal) {
        cboCust.addActionListener(e -> {
            ComboItem item = (ComboItem) cboCust.getSelectedItem();
            if (item != null) {
                cboAcc.removeAllItems();
                try (ResultSet rs = DatabaseHelper.getAccountsByCustomerId(Integer.parseInt(item.getKey()))) {
                    while (rs != null && rs.next()) {
                        String accId = rs.getString("account_id");
                        double bal = rs.getDouble("balance");
                        // Format hiển thị: 1902... - 500,000 đ
                        String display = accId + " - " + String.format("%,.0f đ", bal);
                        cboAcc.addItem(new ComboItem(accId, display));
                    }
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        // Khi chọn Tài khoản -> Cập nhật Label số dư (để tiện xem)
        cboAcc.addActionListener(e -> {
            ComboItem item = (ComboItem) cboAcc.getSelectedItem();
            if (item != null) {
                String text = item.toString();
                if(lblBal != null) lblBal.setText("Số dư: " + text.substring(text.indexOf("-")+2));
            }
        });
    }
    // 3. Xử lý logic giao dịch
    private void processTransaction(String type, JComboBox<ComboItem> cboSource, JTextField txtAmt, JComboBox<ComboItem> cboTarget) {
        try {
            ComboItem srcItem = (ComboItem) cboSource.getSelectedItem();
            if (srcItem == null) { JOptionPane.showMessageDialog(this, "Chưa chọn tài khoản!"); return; }

            // Lấy ID tài khoản nguồn
            String srcId = srcItem.getKey();

            // --- [MỚI] KIỂM TRA TÀI KHOẢN NGUỒN CÓ BỊ KHÓA KHÔNG ---
            if (DatabaseHelper.isAccountLocked(srcId)) {
                JOptionPane.showMessageDialog(this,
                        "Giao dịch bị từ chối!\nTài khoản nguồn [" + srcId + "] đang bị KHÓA.",
                        "Cảnh báo", JOptionPane.ERROR_MESSAGE);
                return; // Dừng lại ngay, không làm gì nữa
            }

            double amount = Double.parseDouble(txtAmt.getText().replace(",", ""));
            if (amount <= 0) { JOptionPane.showMessageDialog(this, "Số tiền phải > 0"); return; }

            boolean success = false;

            if ("NAP".equals(type)) {
                success = DatabaseHelper.performDeposit(srcId, amount);
            } else if ("RUT".equals(type)) {
                double currentBal = parseBalanceFromCombo(srcItem);
                if (currentBal < amount) { JOptionPane.showMessageDialog(this, "Không đủ số dư!"); return; }

                success = DatabaseHelper.performWithdraw(srcId, amount);
            } else if ("CHUYEN".equals(type)) {
                ComboItem targetItem = (ComboItem) cboTarget.getSelectedItem();
                if (targetItem == null) { JOptionPane.showMessageDialog(this, "Chưa chọn người nhận!"); return; }
                String targetId = targetItem.getKey();

                if (srcId.equals(targetId)) { JOptionPane.showMessageDialog(this, "Không thể chuyển cho chính mình!"); return; }

                // --- [MỚI] KIỂM TRA TÀI KHOẢN NHẬN CÓ BỊ KHÓA KHÔNG ---
                // (Tùy nghiệp vụ: Có ngân hàng cho phép nhận tiền vào TK khóa, có bên thì không.
                // Ở đây mình chặn luôn cho chặt chẽ).
                if (DatabaseHelper.isAccountLocked(targetId)) {
                    JOptionPane.showMessageDialog(this,
                            "Giao dịch thất bại!\nTài khoản người nhận [" + targetId + "] đang bị KHÓA.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double currentBal = parseBalanceFromCombo(srcItem);
                if (currentBal < amount) { JOptionPane.showMessageDialog(this, "Không đủ số dư!"); return; }

                success = DatabaseHelper.performTransfer(srcId, targetId, amount);
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "Giao dịch thành công!");
                txtAmt.setText("");

                // Refresh số dư
                if (cboSource.getItemCount() > 0) cboSource.setSelectedIndex(cboSource.getSelectedIndex());
                if (cboTarget != null && cboTarget.getItemCount() > 0) cboTarget.setSelectedIndex(cboTarget.getSelectedIndex());

                loadTransactionData();
            } else {
                JOptionPane.showMessageDialog(this, "Giao dịch thất bại! Có lỗi hệ thống.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền nhập vào không hợp lệ!");
        }
    }
    private double parseBalanceFromCombo(ComboItem item) {
        try {
            String text = item.toString();
            String balPart = text.substring(text.indexOf("-") + 1).replace("đ", "").replace(",", "").trim();
            return Double.parseDouble(balPart);
        } catch (Exception e) { return 0; }
    }
    // ========================================================================
    // TAB SAO KÊ
    // ========================================================================
    private JPanel createStatementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        // --- 1. PHẦN TRÊN: NHẬP SỐ TK & NÚT BẤM ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(Color.WHITE);

        topPanel.add(new JLabel("Số TK:"));
        txtStatementAccId = new JTextField(15);
        txtStatementAccId.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topPanel.add(txtStatementAccId);

        JButton btnView = new JButton("Xem sao kê");
        styleButton(btnView, new Color(23, 162, 184));
        topPanel.add(btnView);

        JButton btnExport = new JButton("Xuất CSV");
        styleButton(btnExport, new Color(23, 162, 184));
        topPanel.add(btnExport);

        panel.add(topPanel, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        String[] cols = {"ID", "Loại", "Số tiền", "TK Đối ứng", "Ngày", "Mô tả"};
        modelStatement = new DefaultTableModel(cols, 0);
        tblStatement = new JTable(modelStatement);
        tblStatement.setRowHeight(28);

        // --- STYLE HEADER (Tiêu đề bảng) ---
        JTableHeader header = tblStatement.getTableHeader();
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(new Color(0, 102, 204));
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setHorizontalAlignment(JLabel.CENTER); // Tiêu đề vẫn nên để giữa cho đẹp
                l.setOpaque(true);
                return l;
            }
        });

        // --- CẤU HÌNH CĂN LỀ DỮ LIỆU (CELL RENDERER) ---

        // Tạo renderer CĂN TRÁI (Left Align)
        javax.swing.table.DefaultTableCellRenderer leftRenderer = new javax.swing.table.DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        // Căn trái cho các cột thông tin text/ID
        tblStatement.getColumnModel().getColumn(0).setCellRenderer(leftRenderer); // ID
        tblStatement.getColumnModel().getColumn(1).setCellRenderer(leftRenderer); // Loại
        tblStatement.getColumnModel().getColumn(3).setCellRenderer(leftRenderer); // TK Đối ứng
        tblStatement.getColumnModel().getColumn(4).setCellRenderer(leftRenderer); // Ngày
        tblStatement.getColumnModel().getColumn(5).setCellRenderer(leftRenderer); // Mô tả
        tblStatement.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);

        panel.add(new JScrollPane(tblStatement), BorderLayout.CENTER);

        // --- XỬ LÝ SỰ KIỆN ---
        btnView.addActionListener(e -> {
            String accId = txtStatementAccId.getText().trim();
            loadStatementData(accId);
        });

        btnExport.addActionListener(e -> exportTableToCSV(tblStatement, "SaoKe_" + txtStatementAccId.getText()));

        return panel;
    }

    // --- HÀM LOAD DỮ LIỆU SAO KÊ ---
    private void loadStatementData(String accId) {
        // 1. Luôn xóa sạch bảng trước khi tải dữ liệu mới
        modelStatement.setRowCount(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        // Biến ResultSet để hứng dữ liệu
        ResultSet rs = null;

        try {
            // --- LOGIC MỚI Ở ĐÂY ---
            if (accId.isEmpty()) {
                // Nếu ô nhập trống -> Lấy TẤT CẢ lịch sử
                rs = DatabaseHelper.getAllTransactions();
            } else {
                // Nếu có nhập số TK -> Lọc theo tài khoản đó
                rs = DatabaseHelper.getTransactionsByAccount(accId);
            }
            // -----------------------

            boolean hasData = false;
            while (rs != null && rs.next()) {
                hasData = true;
                Vector<Object> v = new Vector<>();

                v.add(rs.getInt("transaction_id")); // ID

                // Loại giao dịch
                String rawType = rs.getString("type");
                String displayType = rawType;
                if ("NAP_TIEN".equals(rawType)) displayType = "NẠP";
                else if ("RUT_TIEN".equals(rawType)) displayType = "RÚT";
                else if ("CHUYEN_DI".equals(rawType)) displayType = "CHUYỂN ĐI";
                else if ("NHAN_TIEN".equals(rawType)) displayType = "NHẬN TIỀN";
                v.add(displayType);

                v.add(String.format("%,.0f đ", rs.getDouble("amount"))); // Số tiền

                // TK Đối ứng
                String related = rs.getString("related_account_id");
                v.add(related != null ? related : "-");

                // Ngày giờ
                Timestamp ts = rs.getTimestamp("created_at");
                String strNgay = "";
                if (ts != null) strNgay = dateFormat.format(ts);
                v.add(strNgay);

                v.add(rs.getString("message")); // Mô tả

                modelStatement.addRow(v);
            }

            // Chỉ thông báo lỗi nếu nhập mã TK cụ thể mà không tìm thấy
            // Còn nếu load full mà không có dữ liệu thì thôi (bảng trắng)
            if (!hasData && !accId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy giao dịch nào cho tài khoản: " + accId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn: " + e.getMessage());
        } finally {
            // Đóng ResultSet thủ công vì ta khai báo bên ngoài try-with-resources
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // --- HÀM HỖ TRỢ XUẤT CSV (Dùng chung cho cả các nút khác nếu cần) ---
    private void exportTableToCSV(JTable table, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file CSV");
        fileChooser.setSelectedFile(new java.io.File(defaultFileName + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().endsWith(".csv")) fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".csv");

            try (java.io.FileWriter fw = new java.io.FileWriter(fileToSave);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {

                // Header
                for (int i = 0; i < table.getColumnCount(); i++) bw.write(table.getColumnName(i) + ",");
                bw.newLine();

                // Data
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        Object val = table.getValueAt(i, j);
                        String data = (val == null) ? "" : val.toString().replace(",", "");
                        bw.write(data + ",");
                    }
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "Xuất file thành công!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage()); }
        }
    }
}