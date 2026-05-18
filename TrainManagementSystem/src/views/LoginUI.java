package views;

import controllers.AuthController;
import controllers.DBConnection;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginUI extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(15, 23, 42);      // Slate-900
    private static final Color BG_CARD      = new Color(30, 41, 59);      // Slate-800
    private static final Color ACCENT       = new Color(56, 189, 248);    // Sky-400
    private static final Color ACCENT_HOVER = new Color(14, 165, 233);    // Sky-500
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);   // Slate-50
    private static final Color TEXT_MUTED   = new Color(148, 163, 184);   // Slate-400
    private static final Color BORDER_COLOR = new Color(51, 65, 85);      // Slate-700
    private static final Color ERROR_COLOR  = new Color(248, 113, 113);   // Red-400
    private static final Color SUCCESS_COLOR= new Color(74, 222, 128);    // Green-400

    private JTextField  emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private final AuthController authController;

    public LoginUI() {
        authController = new AuthController();
        DBConnection.initializeDatabase();
        initUI();
    }

    private void initUI() {
        setTitle("Train Management System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Root panel with dark background ──────────────────────────────────
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);

        // ── Card ─────────────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(48, 48, 48, 48)
        ));
        card.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));

        // ── Logo / Icon ───────────────────────────────────────────────────────
        JLabel iconLabel = new JLabel("🚄", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel titleLabel = new JLabel("TrainMS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Divider ───────────────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // ── Email field ───────────────────────────────────────────────────────
        JLabel emailLabel = buildFieldLabel("Email Address");
        emailField = new JTextField();
        styleTextField(emailField);
        emailField.setText("");

        // ── Password field ────────────────────────────────────────────────────
        JLabel passLabel = buildFieldLabel("Password");
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setText("");

        // ── Status label ──────────────────────────────────────────────────────
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Login button ──────────────────────────────────────────────────────
        JButton loginBtn = buildLoginButton();

        // ── Hint label ────────────────────────────────────────────────────────
        JLabel hintLabel = new JLabel("Default — Admin: admin@train.com / admin123");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(TEXT_MUTED);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Assemble card ─────────────────────────────────────────────────────
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(24));
        card.add(sep);
        card.add(Box.createVerticalStrut(28));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(20));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(hintLabel);

        root.add(card);

        // ── Enter key triggers login ──────────────────────────────────────────
        getRootPane().setDefaultButton(loginBtn);
    }

    // ── Builder helpers ───────────────────────────────────────────────────────

    private JLabel buildFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_DARK);
        field.setCaretColor(ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("Sign In");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(ACCENT); }
        });

        btn.addActionListener(e -> attemptLogin());
        return btn;
    }

    private void attemptLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(ERROR_COLOR);
            statusLabel.setText("⚠  Please enter both email and password.");
            return;
        }

        User user = authController.login(email, password);

        if (user == null) {
            statusLabel.setForeground(ERROR_COLOR);
            statusLabel.setText("✗  Invalid credentials. Please try again.");
            passwordField.setText("");
            return;
        }

        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setText("✓  Welcome, " + user.getName() + "!");

        // Open appropriate dashboard after a brief delay for UX
        Timer timer = new Timer(600, evt -> {
            dispose();
            if ("ADMIN".equals(user.getRole())) {
                new AdminDashboardUI(user).setVisible(true);
            } else {
                new StaffDashboardUI(user).setVisible(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}