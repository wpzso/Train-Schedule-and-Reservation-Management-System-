package views;

import controllers.AuthController;
import controllers.BookingController;
import controllers.ScheduleController;
import models.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class AdminDashboardUI extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(15, 23, 42);
    private static final Color BG_PANEL     = new Color(30, 41, 59);
    private static final Color ACCENT       = new Color(56, 189, 248);
    private static final Color ACCENT_HOVER = new Color(14, 165, 233);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_MUTED   = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);
    private static final Color TABLE_ALT    = new Color(22, 33, 53);
    private static final Color ERROR_COLOR  = new Color(248, 113, 113);
    private static final Color SUCCESS_COLOR= new Color(74, 222, 128);
    private static final Color DANGER_BTN   = new Color(220, 38, 38);
    private static final Color HEADER_BG    = new Color(15, 23, 42);

    private final User currentUser;
    private final AuthController authCtrl         = new AuthController();
    private final ScheduleController scheduleCtrl = new ScheduleController();
    private final BookingController bookingCtrl   = new BookingController();

    private JTable usersTable, trainsTable, schedulesTable, bookingsTable;
    private DefaultTableModel usersModel, trainsModel, schedulesModel, bookingsModel;

    private JTabbedPane tabbedPane;

    public AdminDashboardUI(User user) {
        this.currentUser = user;
        initUI();
        refreshAllTabs();
    }

    private void initUI() {
        setTitle("TrainMS — Admin Dashboard (" + currentUser.getName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Top Bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("🚄 ");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        JLabel title = new JLabel("TrainMS  ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        JLabel badge = new JLabel(" ADMIN ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(ACCENT);
        badge.setOpaque(true);
        badge.setBackground(new Color(14, 116, 144, 60));
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));

        left.add(icon);
        left.add(title);
        left.add(badge);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel userInfo = new JLabel("👤  " + currentUser.getName());
        userInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userInfo.setForeground(TEXT_MUTED);

        JButton logoutBtn = buildSmallButton("Logout", DANGER_BTN);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginUI().setVisible(true);
        });

        right.add(userInfo);
        right.add(logoutBtn);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Main Content (Tabbed Pane) ────────────────────────────────────────────
    private JComponent buildContent() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_DARK);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBorder(new EmptyBorder(16, 16, 16, 16));

        UIManager.put("TabbedPane.background", BG_DARK);
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected", BG_PANEL);
        UIManager.put("TabbedPane.contentAreaColor", BG_PANEL);

        tabbedPane.addTab("👤  Users",     buildUsersTab());
        tabbedPane.addTab("🚂  Trains",    buildTrainsTab());
        tabbedPane.addTab("🗓  Schedules", buildSchedulesTab());
        tabbedPane.addTab("🎫  Bookings",  buildBookingsTab());

        return tabbedPane;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TAB: USERS
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildUsersTab() {
        JPanel panel = buildTabPanel();
        panel.setLayout(new BorderLayout(0, 16));

        JPanel form = buildSectionPanel("Add New User");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = formGbc();

        JTextField nameField  = styledTextField();
        JTextField emailField = styledTextField();
        JPasswordField passField  = styledPasswordField();
        JComboBox<String> roleBox = styledComboBox(new String[]{"STAFF","ADMIN"});

        addFormRow(form, gc, 0, "Full Name",  nameField);
        addFormRow(form, gc, 1, "Email",      emailField);
        addFormRow(form, gc, 2, "Password",   passField);
        addFormRow(form, gc, 3, "Role",       roleBox);

        JLabel statusLbl = buildStatusLabel();

        JButton addBtn = buildPrimaryButton("＋  Add User");
        addBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword()).trim();
            String role  = (String) roleBox.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                showStatus(statusLbl, "All fields are required.", false);
                return;
            }
            boolean ok = authCtrl.addUser(name, email, pass, role);
            if (ok) {
                showStatus(statusLbl, "✓  User added successfully.", true);
                nameField.setText(""); emailField.setText(""); passField.setText("");
                refreshUsersTable();
            } else {
                showStatus(statusLbl, "✗  Failed — email may already exist.", false);
            }
        });

        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2; gc.anchor = GridBagConstraints.WEST;
        form.add(statusLbl, gc);
        gc.gridy = 5; form.add(addBtn, gc);

        String[] cols = {"ID","Name","Email","Role"};
        usersModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = buildStyledTable(usersModel);

        JButton delBtn = buildDangerButton("🗑  Delete Selected User");
        delBtn.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a user to delete."); return; }
            int id = (int) usersModel.getValueAt(row, 0);
            if (id == currentUser.getId()) {
                JOptionPane.showMessageDialog(this, "You cannot delete your own account.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { authCtrl.deleteUser(id); refreshUsersTable(); }
        });

        JPanel tableSection = buildSectionPanel("All Users");
        tableSection.setLayout(new BorderLayout(0, 8));
        tableSection.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        tableSection.add(delBtn, BorderLayout.SOUTH);

        panel.add(form, BorderLayout.NORTH);
        panel.add(tableSection, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TAB: TRAINS
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildTrainsTab() {
        JPanel panel = buildTabPanel();
        panel.setLayout(new BorderLayout(0, 16));

        JPanel form = buildSectionPanel("Add New Train");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = formGbc();

        JTextField trainNameField  = styledTextField();
        JSpinner capacitySpinner   = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));
        styleSpinner(capacitySpinner);

        addFormRow(form, gc, 0, "Train Name", trainNameField);
        addSpinnerRow(form, gc, 1, "Total Capacity", capacitySpinner);

        JLabel statusLbl = buildStatusLabel();

        JButton addBtn = buildPrimaryButton("＋  Add Train");
        addBtn.addActionListener(e -> {
            String name = trainNameField.getText().trim();
            int cap = (int) capacitySpinner.getValue();
            if (name.isEmpty()) { showStatus(statusLbl, "Train name is required.", false); return; }
            boolean ok = scheduleCtrl.addTrain(name, cap);
            if (ok) {
                showStatus(statusLbl, "✓  Train added successfully.", true);
                trainNameField.setText(""); capacitySpinner.setValue(100);
                refreshTrainsTable();
            } else {
                showStatus(statusLbl, "✗  Failed to add train.", false);
            }
        });

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2; gc.anchor = GridBagConstraints.WEST;
        form.add(statusLbl, gc); gc.gridy = 3; form.add(addBtn, gc);

        String[] cols = {"ID","Train Name","Total Capacity"};
        trainsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        trainsTable = buildStyledTable(trainsModel);

        JButton delBtn = buildDangerButton("🗑  Delete Selected Train");
        delBtn.addActionListener(e -> {
            int row = trainsTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a train."); return; }
            int id = (int) trainsModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this train? Related schedules may be affected.", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { scheduleCtrl.deleteTrain(id); refreshTrainsTable(); }
        });

        JPanel tableSection = buildSectionPanel("All Trains");
        tableSection.setLayout(new BorderLayout(0, 8));
        tableSection.add(new JScrollPane(trainsTable), BorderLayout.CENTER);
        tableSection.add(delBtn, BorderLayout.SOUTH);

        panel.add(form, BorderLayout.NORTH);
        panel.add(tableSection, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TAB: SCHEDULES  (now uses From / To / Price)
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildSchedulesTab() {
        JPanel panel = buildTabPanel();
        panel.setLayout(new BorderLayout(0, 16));

        JPanel form = buildSectionPanel("Add New Schedule");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = formGbc();

        JComboBox<Train> trainCombo = new JComboBox<>();
        styleComboBox(trainCombo);
        refreshTrainCombo(trainCombo);

        JTextField fromField    = styledTextField();
        fromField.setToolTipText("e.g. Riyadh");
        JTextField toField      = styledTextField();
        toField.setToolTipText("e.g. Jeddah");
        JTextField depTimeField = styledTextField();
        depTimeField.setToolTipText("e.g. 2025-08-15 08:30");
        depTimeField.setText("2025-08-15 08:30");

        // Price is computed automatically from the departure time.
        JLabel pricePreview = new JLabel();
        pricePreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pricePreview.setForeground(ACCENT);
        Runnable updatePrice = () -> {
            double p = controllers.PricingService.priceFor(depTimeField.getText().trim());
            pricePreview.setText(String.format("%.0f SAR  (auto — earlier = pricier)", p));
        };
        updatePrice.run();
        depTimeField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updatePrice.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updatePrice.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePrice.run(); }
        });

        addComboRow(form, gc, 0, "Train",          trainCombo);
        addFormRow(form, gc,  1, "From",            fromField);
        addFormRow(form, gc,  2, "To",              toField);
        addFormRow(form, gc,  3, "Departure Time",  depTimeField);
        addFormRow(form, gc,  4, "Price",           pricePreview);

        JLabel statusLbl = buildStatusLabel();

        JButton addBtn = buildPrimaryButton("＋  Add Schedule");
        addBtn.addActionListener(e -> {
            Train t = (Train) trainCombo.getSelectedItem();
            String from = fromField.getText().trim();
            String to   = toField.getText().trim();
            String time  = depTimeField.getText().trim();

            if (t == null || from.isEmpty() || to.isEmpty() || time.isEmpty()) {
                showStatus(statusLbl, "Train, From, To and Departure Time are required.", false); return;
            }

            String err = scheduleCtrl.addSchedule(t.getId(), from, to, time);
            if (err == null) {
                showStatus(statusLbl, "✓  Schedule added successfully.", true);
                fromField.setText(""); toField.setText("");
                depTimeField.setText("2025-08-15 08:30");
                updatePrice.run();
                refreshSchedulesTable();
            } else {
                JOptionPane.showMessageDialog(this, err, "Schedule Conflict", JOptionPane.ERROR_MESSAGE);
                showStatus(statusLbl, "✗  " + err, false);
            }
        });

        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2; gc.anchor = GridBagConstraints.WEST;
        form.add(statusLbl, gc); gc.gridy = 6; form.add(addBtn, gc);

        String[] cols = {"ID","Train","From","To","Price (SAR)","Departure Time","Capacity"};
        schedulesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        schedulesTable = buildStyledTable(schedulesModel);

        JButton delBtn = buildDangerButton("🗑  Delete Selected Schedule");
        delBtn.addActionListener(e -> {
            int row = schedulesTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a schedule."); return; }
            int id = (int) schedulesModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete schedule? Related bookings may be affected.", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { scheduleCtrl.deleteSchedule(id); refreshSchedulesTable(); }
        });

        JPanel tableSection = buildSectionPanel("All Schedules");
        tableSection.setLayout(new BorderLayout(0, 8));
        tableSection.add(new JScrollPane(schedulesTable), BorderLayout.CENTER);
        tableSection.add(delBtn, BorderLayout.SOUTH);

        panel.add(form, BorderLayout.NORTH);
        panel.add(tableSection, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TAB: BOOKINGS
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildBookingsTab() {
        JPanel panel = buildTabPanel();
        panel.setLayout(new BorderLayout(0, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("All Bookings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);

        JButton refreshBtn = buildSmallButton("⟳  Refresh", ACCENT);
        refreshBtn.addActionListener(e -> refreshBookingsTable());

        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        String[] cols = {"ID","Passenger","Email","Train","From","To","Departure","Seat #","Price (SAR)"};
        bookingsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingsTable = buildStyledTable(bookingsModel);

        JButton delBtn = buildDangerButton("🗑  Cancel Selected Booking");
        delBtn.addActionListener(e -> {
            int row = bookingsTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a booking."); return; }
            int id = (int) bookingsModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Cancel this booking?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { bookingCtrl.deleteBooking(id); refreshBookingsTable(); }
        });

        JPanel tableSection = buildSectionPanel("Bookings Overview");
        tableSection.setLayout(new BorderLayout(0, 8));
        tableSection.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        tableSection.add(delBtn, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);
        panel.add(tableSection, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  REFRESH METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private void refreshAllTabs() {
        refreshUsersTable();
        refreshTrainsTable();
        refreshSchedulesTable();
        refreshBookingsTable();
    }

    private void refreshUsersTable() {
        usersModel.setRowCount(0);
        for (User u : authCtrl.getAllUsers())
            usersModel.addRow(new Object[]{u.getId(), u.getName(), u.getEmail(), u.getRole()});
    }

    private void refreshTrainsTable() {
        trainsModel.setRowCount(0);
        for (Train t : scheduleCtrl.getAllTrains())
            trainsModel.addRow(new Object[]{t.getId(), t.getTrainName(), t.getTotalCapacity()});
    }

    private void refreshSchedulesTable() {
        schedulesModel.setRowCount(0);
        for (Schedule s : scheduleCtrl.getAllSchedules())
            schedulesModel.addRow(new Object[]{
                s.getId(), s.getTrainName(), s.getFromLocation(), s.getToLocation(),
                String.format("%.2f", s.getPrice()),
                s.getDepartureTime(), s.getTotalCapacity()
            });
    }

    private void refreshBookingsTable() {
        bookingsModel.setRowCount(0);
        for (Booking b : bookingCtrl.getAllBookings())
            bookingsModel.addRow(new Object[]{
                b.getId(), b.getPassengerName(), b.getPassengerEmail(),
                b.getTrainName(), b.getFromLocation(), b.getToLocation(),
                b.getDepartureTime(), b.getSeatNumber(),
                String.format("%.2f", b.getPrice())
            });
    }

    private void refreshTrainCombo(JComboBox<Train> combo) {
        combo.removeAllItems();
        for (Train t : scheduleCtrl.getAllTrains()) combo.addItem(t);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private JPanel buildTabPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(16, 4, 4, 4));
        return p;
    }

    private JPanel buildSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        "  " + title + "  ",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        TEXT_MUTED
                ),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return p;
    }

    private GridBagConstraints formGbc() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        return gc;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        panel.add(field, gc);
    }

    private void addSpinnerRow(JPanel panel, GridBagConstraints gc, int row, String label, JSpinner spinner) {
        addFormRow(panel, gc, row, label, spinner);
    }

    private void addComboRow(JPanel panel, GridBagConstraints gc, int row, String label, JComboBox<?> combo) {
        addFormRow(panel, gc, row, label, combo);
    }

    private JTextField styledTextField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_DARK);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)
        ));
        f.setPreferredSize(new Dimension(220, 34));
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_DARK);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)
        ));
        f.setPreferredSize(new Dimension(220, 34));
        return f;
    }

    private <T> JComboBox<T> styledComboBox(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        styleComboBox(box);
        return box;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setForeground(TEXT_PRIMARY);
        box.setBackground(BG_DARK);
        box.setPreferredSize(new Dimension(220, 34));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinner.getEditor().getComponent(0).setForeground(TEXT_PRIMARY);
        spinner.getEditor().getComponent(0).setBackground(BG_DARK);
        spinner.setPreferredSize(new Dimension(220, 34));
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(14, 116, 144, 120));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setBackground(sel ? new Color(14, 116, 144, 120) : (row % 2 == 0 ? BG_PANEL : TABLE_ALT));
                setForeground(TEXT_PRIMARY);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        return table;
    }

    private JButton buildPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 36));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    private JButton buildDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(DANGER_BTN);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildSmallButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel buildStatusLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ERROR_COLOR);
        return lbl;
    }

    private void showStatus(JLabel lbl, String msg, boolean success) {
        lbl.setForeground(success ? SUCCESS_COLOR : ERROR_COLOR);
        lbl.setText(msg);
    }
}