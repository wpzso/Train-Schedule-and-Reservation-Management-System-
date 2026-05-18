package controllers;

import java.sql.*;

public class DBConnection {

    private static final String DB_URL = "jdbc:sqlite:train_management.db";
    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initializeDatabase() {
        String createUsers = """
                CREATE TABLE IF NOT EXISTS Users (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    name     TEXT    NOT NULL,
                    email    TEXT    NOT NULL UNIQUE,
                    password TEXT    NOT NULL,
                    role     TEXT    NOT NULL CHECK(role IN ('ADMIN','STAFF'))
                );
                """;

        String createTrains = """
                CREATE TABLE IF NOT EXISTS Trains (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    train_name     TEXT    NOT NULL,
                    total_capacity INTEGER NOT NULL
                );
                """;

        // Includes all columns from the start
        String createSchedules = """
                CREATE TABLE IF NOT EXISTS Schedules (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    train_id       INTEGER NOT NULL,
                    route_name     TEXT    NOT NULL,
                    from_location  TEXT    NOT NULL DEFAULT '',
                    to_location    TEXT    NOT NULL DEFAULT '',
                    price          REAL    NOT NULL DEFAULT 0,
                    departure_time TEXT    NOT NULL,
                    FOREIGN KEY (train_id) REFERENCES Trains(id)
                );
                """;

        String createBookings = """
                CREATE TABLE IF NOT EXISTS Bookings (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    schedule_id      INTEGER NOT NULL,
                    passenger_name   TEXT    NOT NULL,
                    passenger_email  TEXT    NOT NULL DEFAULT '',
                    seat_number      INTEGER NOT NULL,
                    FOREIGN KEY (schedule_id) REFERENCES Schedules(id)
                );
                """;

        String seedAdmin = """
                INSERT OR IGNORE INTO Users (name, email, password, role)
                VALUES ('System Admin', 'admin@train.com', 'admin123', 'ADMIN');
                """;

        String seedStaff = """
                INSERT OR IGNORE INTO Users (name, email, password, role)
                VALUES ('John Staff', 'staff@train.com', 'staff123', 'STAFF');
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsers);
            stmt.execute(createTrains);
            stmt.execute(createSchedules);
            stmt.execute(createBookings);
            stmt.execute(seedAdmin);
            stmt.execute(seedStaff);

            // ── Migrations: safely add columns if they don't exist yet ────────
            // This handles existing databases created before these columns were added.
            runMigration(conn, "ALTER TABLE Schedules ADD COLUMN from_location TEXT NOT NULL DEFAULT ''");
            runMigration(conn, "ALTER TABLE Schedules ADD COLUMN to_location   TEXT NOT NULL DEFAULT ''");
            runMigration(conn, "ALTER TABLE Schedules ADD COLUMN price         REAL NOT NULL DEFAULT 0");
            runMigration(conn, "ALTER TABLE Bookings  ADD COLUMN passenger_email TEXT NOT NULL DEFAULT ''");

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Runs an ALTER TABLE statement and silently ignores "duplicate column" errors,
     * so migrations are safe to run on both fresh and existing databases.
     */
    private static void runMigration(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            // SQLite throws "duplicate column name" if the column already exists — ignore it.
            if (!e.getMessage().toLowerCase().contains("duplicate column")) {
                System.err.println("Migration warning: " + e.getMessage());
            }
        }
    }
}