package ui;
import javax.swing.*;
import service.AuthService;

import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("Đăng nhập MiniBank");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1));

        Dimension labelSize = new Dimension(100, 30);

        JPanel p1 = new JPanel();

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setPreferredSize(labelSize);
        lblUser.setHorizontalAlignment(SwingConstants.RIGHT);
        p1.add(lblUser);

        txtUser = new JTextField(15);
        p1.add(txtUser);
        add(p1);

        JPanel p2 = new JPanel();

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setPreferredSize(labelSize);
        lblPass.setHorizontalAlignment(SwingConstants.RIGHT);
        p2.add(lblPass);

        txtPass = new JPasswordField(15);
        p2.add(txtPass);
        add(p2);

        JPanel p3 = new JPanel();
        btnLogin = new JButton("Đăng nhập");
        p3.add(btnLogin);
        add(p3);
        
        btnLogin.addActionListener(e -> {
            // 1. Lấy dữ liệu và loại bỏ khoảng trắng thừa đầu/cuối (trim)
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword()).trim();

            // 2. XỬ LÝ NGOẠI LỆ: Kiểm tra bỏ trống
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!",
                        "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. Nếu đã nhập đủ thì mới kiểm tra đăng nhập
            if (AuthService.authenticate(username, password)) {
                new AdminDashboard().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sai tài khoản hoặc mật khẩu!",
                        "Đăng nhập thất bại",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        getRootPane().setDefaultButton(btnLogin);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}