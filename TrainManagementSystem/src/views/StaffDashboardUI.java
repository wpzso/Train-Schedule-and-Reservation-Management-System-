package views;

import controllers.BookingController;
import controllers.ScheduleController;
import models.Booking;
import models.Schedule;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class StaffDashboardUI extends JFrame {

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
    private static final Color SEAT_FREE    = new Color(34, 197, 94, 180);
    private static final Color SEAT_TAKEN   = new Color(239, 68, 68, 200);
    private static final Color SEAT_SELECTED= new Color(56, 189, 248, 220);

    private final User currentUser;
    private final ScheduleController scheduleCtrl = new ScheduleController();
    private final BookingController bookingCtrl   = new BookingController();

    // ── Search form state ─────────────────────────────────────────────────────
    private JComboBox<String> fromCombo;
    private JComboBox<String> toCombo;
    private JTextField dateField;

    // ── Trip results ──────────────────────────────────────────────────────────
    private JTable tripsTable;
    private DefaultTableModel tripsModel;
    private List<Schedule> currentTrips;     // backing list for tripsTable
    private Schedule selectedSchedule;       // the trip the user picked

    // ── Booking form state ────────────────────────────────────────────────────
    private JTextField passengerNameField;
    private JTextField passengerEmailField;
    private JSpinner seatSpinner;
    private JLabel statusLabel;
    private JLabel capacityLabel;
    private JLabel selectedTripLabel;

    // ── Seat Map ──────────────────────────────────────────────────────────────
    private JPanel seatMapPanel;
    private int selectedSeat = -1;

    // ── All Bookings table ────────────────────────────────────────────────────
    private JTable bookingsTable;
    private DefaultTableModel bookingsModel;

    public StaffDashboardUI(User user) {
        this.currentUser = user;
        initUI();
        refreshLocationCombos();
        refreshBookingsTable();
    }

    private void initUI() {
        setTitle("TrainMS — Staff Dashboard (" + currentUser.getName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
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
        JLabel badge = new JLabel(" STAFF ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(new Color(134, 239, 172));
        badge.setOpaque(true);
        badge.setBackground(new Color(21, 128, 61, 60));
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));
        left.add(icon); left.add(title); left.add(badge);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel userInfo = new JLabel("👤  " + currentUser.getName());
        userInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userInfo.setForeground(TEXT_MUTED);
        JButton logoutBtn = buildSmallButton("Logout", DANGER_BTN);
        logoutBtn.addActionListener(e -> { dispose(); new LoginUI().setVisible(true); });
        right.add(userInfo); right.add(logoutBtn);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Main Content: Split Pane ──────────────────────────────────────────────
    private JComponent buildMainContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(560);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setBackground(BG_DARK);
        split.setContinuousLayout(true);
        return split;
    }

    // ── LEFT: Search + Trips + Booking form + Seat Map ────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 8));

        panel.add(buildSearchSection());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildTripsSection());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildBookingSection());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildSeatMapSection());

        JScrollPane scroll = new JScrollPane(panel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Search section: From / To / Date ──────────────────────────────────────
    private JPanel buildSearchSection() {
        JPanel form = buildSectionPanel("🔎  Find Trips");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = gbc();

        fromCombo = new JComboBox<>();
        fromCombo.setEditable(true);
        styleComboBox(fromCombo);

        toCombo = new JComboBox<>();
        toCombo.setEditable(true);
        styleComboBox(toCombo);

        dateField = styledTextField();
        dateField.setToolTipText("yyyy-MM-dd  (leave blank for any date)");
        // Always store only the date part — strip any time component
        String todayStr = controllers.PricingService.today();
        if (todayStr.length() > 10) todayStr = todayStr.substring(0, 10);
        dateField.setText(todayStr);

        JLabel todayLbl = new JLabel("\uD83D\uDCC5  Today: " + controllers.PricingService.todayPretty());
        todayLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        todayLbl.setForeground(TEXT_MUTED);

        // "All Dates" button to clear the date filter
        JButton clearDateBtn = buildSmallButton("All Dates", new Color(51, 65, 85));
        clearDateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearDateBtn.addActionListener(e -> dateField.setText(""));

        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dateRow.setOpaque(false);
        dateField.setPreferredSize(new Dimension(160, 34));
        dateRow.add(dateField);
        dateRow.add(clearDateBtn);

        addRow(form, gc, 0, "From", fromCombo);
        addRow(form, gc, 1, "To",   toCombo);
        addRow(form, gc, 2, "Date", dateRow);
        addRow(form, gc, 3, "",     todayLbl);

        JButton searchBtn = buildPrimaryButton("\uD83D\uDD0E  Search Trips");
        searchBtn.addActionListener(e -> doSearch());

        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        form.add(searchBtn, gc);
        return form;
    }

    // ── Trips results table ───────────────────────────────────────────────────
    private JPanel buildTripsSection() {
        JPanel section = buildSectionPanel("🚆  Available Trips  (click a row to select)");
        section.setLayout(new BorderLayout(0, 8));

        String[] cols = {"ID", "Train", "From", "To", "Departure", "Price (SAR)"};
        tripsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tripsTable = buildStyledTable(tripsModel);
        tripsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onTripSelected();
        });

        JScrollPane sp = new JScrollPane(tripsTable);
        sp.setPreferredSize(new Dimension(520, 150));
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.getViewport().setBackground(BG_PANEL);
        section.add(sp, BorderLayout.CENTER);
        return section;
    }

    // ── Booking form ──────────────────────────────────────────────────────────
    private JPanel buildBookingSection() {
        JPanel form = buildSectionPanel("🎫  Create New Booking");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = gbc();

        selectedTripLabel = new JLabel("No trip selected yet.");
        selectedTripLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectedTripLabel.setForeground(TEXT_MUTED);

        capacityLabel = new JLabel("\u2014");
        capacityLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        capacityLabel.setForeground(TEXT_MUTED);

        passengerNameField = styledTextField();
        passengerNameField.setToolTipText("Enter the full name of the passenger");

        passengerEmailField = styledTextField();
        passengerEmailField.setToolTipText("e.g. passenger@example.com");

        seatSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        styleSpinner(seatSpinner);
        seatSpinner.addChangeListener(e -> highlightSelectedSeat());

        statusLabel = buildStatusLabel();

        JButton bookBtn = buildPrimaryButton("🎫  Confirm Booking");
        bookBtn.addActionListener(e -> attemptBooking());

        addRow(form, gc, 0, "Selected Trip",   selectedTripLabel);
        addRow(form, gc, 1, "Availability",    capacityLabel);
        addRow(form, gc, 2, "Passenger Name",  passengerNameField);
        addRow(form, gc, 3, "Passenger Email", passengerEmailField);
        addRow(form, gc, 4, "Seat Number",     seatSpinner);

        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2;
        form.add(statusLabel, gc);
        gc.gridy = 6;
        form.add(bookBtn, gc);
        return form;
    }

    // ── Seat Map ──────────────────────────────────────────────────────────────
    private JPanel buildSeatMapSection() {
        JPanel seatSection = buildSectionPanel("🗺  Seat Map  (🟢 Free  🔴 Taken  🔵 Selected)");
        seatSection.setLayout(new BorderLayout());
        seatMapPanel = new JPanel();
        seatMapPanel.setBackground(BG_PANEL);
        seatMapPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));

        JScrollPane seatScroll = new JScrollPane(seatMapPanel);
        seatScroll.setPreferredSize(new Dimension(520, 200));
        seatScroll.setBorder(null);
        seatScroll.getViewport().setBackground(BG_PANEL);
        seatSection.add(seatScroll, BorderLayout.CENTER);
        return seatSection;
    }

    // ── RIGHT: All Bookings table ─────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(16, 8, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("All Bookings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        JButton refreshBtn = buildSmallButton("⟳  Refresh", ACCENT);
        refreshBtn.addActionListener(e -> refreshBookingsTable());
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        String[] cols = {"ID","Passenger","Email","Train","From","To","Departure","Seat","Price"};
        bookingsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingsTable = buildStyledTable(bookingsModel);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(BG_PANEL);

        JPanel tableSection = buildSectionPanel("📋  Booking Records");
        tableSection.setLayout(new BorderLayout(0, 8));
        tableSection.add(scrollPane, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(tableSection, BorderLayout.CENTER);
        return panel;
    }

    // ── Event Handlers ────────────────────────────────────────────────────────

    private String comboText(JComboBox<String> combo) {
        Object sel = combo.getEditor().getItem();
        return sel == null ? "" : sel.toString().trim();
    }

    private void doSearch() {
        String from = comboText(fromCombo);
        String to   = comboText(toCombo);
        // Strip time component if user accidentally included it (keep yyyy-MM-dd only)
        String date = dateField.getText().trim();
        if (date.length() > 10) date = date.substring(0, 10);

        currentTrips = scheduleCtrl.searchSchedules(from, to, date);
        tripsModel.setRowCount(0);
        selectedSchedule = null;
        selectedTripLabel.setText("No trip selected yet.");
        selectedTripLabel.setForeground(TEXT_MUTED);
        capacityLabel.setText("\u2014");
        seatMapPanel.removeAll();
        seatMapPanel.revalidate();
        seatMapPanel.repaint();

        if (currentTrips.isEmpty()) {
            showStatus("No trips found for the given criteria.", false);
            return;
        }
        for (Schedule s : currentTrips) {
            tripsModel.addRow(new Object[]{
                    s.getId(), s.getTrainName(), s.getFromLocation(),
                    s.getToLocation(), s.getDepartureTime(),
                    String.format("%.2f", s.getPrice())
            });
        }
        showStatus("Found " + currentTrips.size() + " trip(s). Select one to book.", true);
    }

    private void onTripSelected() {
        int row = tripsTable.getSelectedRow();
        if (row == -1 || currentTrips == null || row >= currentTrips.size()) return;

        selectedSchedule = currentTrips.get(row);

        selectedTripLabel.setText(selectedSchedule.getFromLocation() + " \u2192 "
                + selectedSchedule.getToLocation() + "  |  "
                + selectedSchedule.getDepartureTime() + "  |  "
                + String.format("%.2f SAR", selectedSchedule.getPrice()));
        selectedTripLabel.setForeground(ACCENT);

        List<Integer> bookedSeats = bookingCtrl.getBookedSeats(selectedSchedule.getId());
        int capacity  = selectedSchedule.getTotalCapacity();
        int available = capacity - bookedSeats.size();

        capacityLabel.setText("Capacity: " + capacity + "  |  Booked: "
                + bookedSeats.size() + "  |  Available: " + available);
        capacityLabel.setForeground(available > 0 ? SUCCESS_COLOR : ERROR_COLOR);

        ((SpinnerNumberModel) seatSpinner.getModel()).setMaximum(capacity);
        seatSpinner.setValue(1);
        selectedSeat = 1;

        buildSeatMap(capacity, bookedSeats);
    }

    private void buildSeatMap(int capacity, List<Integer> bookedSeats) {
        seatMapPanel.removeAll();
        for (int i = 1; i <= capacity; i++) {
            final int seatNum = i;
            boolean taken = bookedSeats.contains(seatNum);

            JButton seatBtn = new JButton(String.valueOf(seatNum));
            seatBtn.setPreferredSize(new Dimension(44, 36));
            seatBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            seatBtn.setForeground(Color.WHITE);
            seatBtn.setBorderPainted(false);
            seatBtn.setFocusPainted(false);

            if (taken) {
                seatBtn.setBackground(SEAT_TAKEN);
                seatBtn.setCursor(Cursor.getDefaultCursor());
                seatBtn.setToolTipText("Seat " + seatNum + " — TAKEN");
                seatBtn.setEnabled(false);
            } else {
                seatBtn.setBackground(seatNum == selectedSeat ? SEAT_SELECTED : SEAT_FREE);
                seatBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                seatBtn.setToolTipText("Seat " + seatNum + " — Click to select");
                seatBtn.addActionListener(e -> {
                    selectedSeat = seatNum;
                    seatSpinner.setValue(seatNum);
                    rebuildSeatColors();
                });
            }
            seatMapPanel.add(seatBtn);
        }
        seatMapPanel.revalidate();
        seatMapPanel.repaint();
    }

    private void rebuildSeatColors() {
        if (selectedSchedule == null) return;
        List<Integer> bookedSeats = bookingCtrl.getBookedSeats(selectedSchedule.getId());

        Component[] comps = seatMapPanel.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JButton) {
                JButton btn = (JButton) comps[i];
                int num = i + 1;
                if (!bookedSeats.contains(num)) {
                    btn.setBackground(num == selectedSeat ? SEAT_SELECTED : SEAT_FREE);
                }
            }
        }
        seatMapPanel.repaint();
    }

    private void highlightSelectedSeat() {
        selectedSeat = (int) seatSpinner.getValue();
        rebuildSeatColors();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");
    }

    private void attemptBooking() {
        if (selectedSchedule == null) {
            showStatus("Please search and select a trip first.", false); return;
        }
        String passenger = passengerNameField.getText().trim();
        String email     = passengerEmailField.getText().trim();
        int seat = (int) seatSpinner.getValue();

        if (passenger.isEmpty()) {
            showStatus("Passenger name is required.", false); return;
        }
        if (email.isEmpty()) {
            showStatus("Passenger email is required.", false); return;
        }
        if (!isValidEmail(email)) {
            showStatus("Please enter a valid email address.", false); return;
        }

        String error = bookingCtrl.createBooking(
                selectedSchedule.getId(), passenger, email, seat);

        if (error == null) {
            showStatus("✓  Booking confirmed for " + passenger + " — Seat " + seat
                    + "  (" + String.format("%.2f SAR", selectedSchedule.getPrice()) + ")", true);
            passengerNameField.setText("");
            passengerEmailField.setText("");
            onTripSelected(); // refresh seat map / availability
            refreshBookingsTable();
        } else {
            JOptionPane.showMessageDialog(this, error, "Booking Failed", JOptionPane.ERROR_MESSAGE);
            showStatus("✗  " + error, false);
        }
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    private void refreshLocationCombos() {
        fromCombo.removeAllItems();
        fromCombo.addItem("");
        for (String s : scheduleCtrl.getFromLocations()) fromCombo.addItem(s);

        toCombo.removeAllItems();
        toCombo.addItem("");
        for (String s : scheduleCtrl.getToLocations()) toCombo.addItem(s);
    }

    private void refreshBookingsTable() {
        bookingsModel.setRowCount(0);
        for (Booking b : bookingCtrl.getAllBookings()) {
            bookingsModel.addRow(new Object[]{
                b.getId(), b.getPassengerName(), b.getPassengerEmail(),
                b.getTrainName(), b.getFromLocation(), b.getToLocation(),
                b.getDepartureTime(), b.getSeatNumber(),
                String.format("%.2f", b.getPrice())
            });
        }
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────

    private JPanel buildSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(BG_PANEL);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        "  " + title + "  ",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        TEXT_MUTED
                ),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return p;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;
        return gc;
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        panel.add(field, gc);
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

    private <T> void styleComboBox(JComboBox<T> box) {
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        header.setBackground(BG_DARK);
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
        btn.setPreferredSize(new Dimension(200, 38));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(ACCENT); }
        });
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

    private void showStatus(String msg, boolean success) {
        statusLabel.setForeground(success ? SUCCESS_COLOR : ERROR_COLOR);
        statusLabel.setText(msg);
    }
}