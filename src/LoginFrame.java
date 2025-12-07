import javax.swing.*;
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

        // --- Panel 1: Tên đăng nhập ---
        JPanel p1 = new JPanel();

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setPreferredSize(labelSize);
        lblUser.setHorizontalAlignment(SwingConstants.RIGHT);
        p1.add(lblUser);

        txtUser = new JTextField(15);
        p1.add(txtUser);
        add(p1);

        // --- Panel 2: Mật khẩu ---
        JPanel p2 = new JPanel();

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setPreferredSize(labelSize);
        lblPass.setHorizontalAlignment(SwingConstants.RIGHT);
        p2.add(lblPass);

        txtPass = new JPasswordField(15);
        p2.add(txtPass);
        add(p2);

        // --- Panel 3: Nút bấm ---
        JPanel p3 = new JPanel();
        btnLogin = new JButton("Đăng nhập");
        p3.add(btnLogin);
        add(p3);

        // Xử lý sự kiện
        btnLogin.addActionListener(e -> {
            String role = DatabaseHelper.authenticate(txtUser.getText(), new String(txtPass.getPassword()));
            if (role != null && role.equalsIgnoreCase("ADMIN")) {
                new AdminDashboard().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        getRootPane().setDefaultButton(btnLogin);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}