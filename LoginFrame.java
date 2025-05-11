package view;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.formdev.flatlaf.FlatLightLaf;

import dao.UserDAO;
import model.User;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private UserDAO userDAO;

    public LoginFrame() {
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.out.println("Không thể thiết lập FlatLaf: " + ex.getMessage());
        }
        userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng nhập");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new java.awt.Color(245, 247, 255));
        panel.setBorder(BorderFactory.createLineBorder(new java.awt.Color(180, 180, 200), 2));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel userLabel = new JLabel("Tên đăng nhập:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setBorder(BorderFactory.createLineBorder(new java.awt.Color(200, 200, 220)));
        panel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setBorder(BorderFactory.createLineBorder(new java.awt.Color(200, 200, 220)));
        panel.add(passwordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new java.awt.Color(245, 247, 255));
        loginButton = new JButton("Đăng nhập", getIcon("login"));
        registerButton = new JButton("Đăng ký", getIcon("register"));
        loginButton.setBackground(new java.awt.Color(60, 130, 220));
        loginButton.setForeground(java.awt.Color.WHITE);
        registerButton.setBackground(new java.awt.Color(100, 180, 100));
        registerButton.setForeground(java.awt.Color.WHITE);
        loginButton.setFocusPainted(false);
        registerButton.setFocusPainted(false);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setToolTipText("Đăng nhập vào hệ thống");
        registerButton.setToolTipText("Đăng ký tài khoản mới");

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        add(panel);
        setBackground(new java.awt.Color(245, 247, 255));
        getContentPane().setBackground(new java.awt.Color(245, 247, 255));
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        User user = userDAO.login(username, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
            new MainFrame(user).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Đăng nhập thất bại!");
        }
    }

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (userDAO.register(username, password)) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
        } else {
            JOptionPane.showMessageDialog(this, "Đăng ký thất bại!");
        }
    }

    private javax.swing.Icon getIcon(String name) {
        switch (name) {
            case "login": return javax.swing.UIManager.getIcon("FileView.computerIcon");
            case "register": return javax.swing.UIManager.getIcon("FileView.directoryIcon");
            default: return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}