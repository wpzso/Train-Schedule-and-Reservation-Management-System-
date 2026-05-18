package controllers;

import models.Schedule;
import models.Train;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ScheduleController {

    // ─── Train CRUD ──────────────────────────────────────────────────────────

    public boolean addTrain(String trainName, int capacity) {
        String sql = "INSERT INTO Trains (train_name, total_capacity) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trainName);
            pstmt.setInt(2, capacity);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Add train error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTrain(int trainId) {
        String sql = "DELETE FROM Trains WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trainId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Delete train error: " + e.getMessage());
            return false;
        }
    }

    public List<Train> getAllTrains() {
        List<Train> trains = new ArrayList<>();
        String sql = "SELECT * FROM Trains";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trains.add(new Train(
                        rs.getInt("id"),
                        rs.getString("train_name"),
                        rs.getInt("total_capacity")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Get trains error: " + e.getMessage());
        }
        return trains;
    }

    // ─── Schedule CRUD ────────────────────────────────────────────────────────

    /**
     * Adds a schedule with a time-conflict check.
     * The ticket price is computed automatically from the departure time
     * (300-500 SAR, multiples of 10, morning trips cost more).
     * Business Rule: A train cannot have two schedules at the exact same departure time.
     * @return null on success, or an error message string if conflict/failure.
     */
    public String addSchedule(int trainId, String fromLocation, String toLocation,
                              String departureTime) {

        // Price is derived from the departure time, not entered by the admin.
        double price = PricingService.priceFor(departureTime);

        // 1. Check for time conflict
        String conflictCheck = "SELECT COUNT(*) FROM Schedules WHERE train_id = ? AND departure_time = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(conflictCheck)) {
            pstmt.setInt(1, trainId);
            pstmt.setString(2, departureTime);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return "TIME CONFLICT: This train already has a schedule at " + departureTime + ". Please choose a different time.";
            }
        } catch (SQLException e) {
            return "Database error during conflict check: " + e.getMessage();
        }

        // 2. Insert if no conflict. route_name kept for backward compatibility.
        String routeName = fromLocation + " \u2192 " + toLocation;
        String sql = """
                INSERT INTO Schedules
                    (train_id, route_name, from_location, to_location, price, departure_time)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trainId);
            pstmt.setString(2, routeName);
            pstmt.setString(3, fromLocation);
            pstmt.setString(4, toLocation);
            pstmt.setDouble(5, price);
            pstmt.setString(6, departureTime);
            pstmt.executeUpdate();
            return null; // null = success
        } catch (SQLException e) {
            return "Failed to add schedule: " + e.getMessage();
        }
    }

    public boolean deleteSchedule(int scheduleId) {
        String sql = "DELETE FROM Schedules WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Delete schedule error: " + e.getMessage());
            return false;
        }
    }

    private Schedule mapSchedule(ResultSet rs) throws SQLException {
        Schedule sc = new Schedule(
                rs.getInt("id"),
                rs.getInt("train_id"),
                rs.getString("route_name"),
                rs.getString("departure_time")
        );
        sc.setFromLocation(rs.getString("from_location"));
        sc.setToLocation(rs.getString("to_location"));
        sc.setPrice(rs.getDouble("price"));
        sc.setTrainName(rs.getString("train_name"));
        sc.setTotalCapacity(rs.getInt("total_capacity"));
        return sc;
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        String sql = """
                SELECT s.id, s.train_id, s.route_name, s.from_location, s.to_location,
                       s.price, s.departure_time, t.train_name, t.total_capacity
                FROM Schedules s
                JOIN Trains t ON s.train_id = t.id
                ORDER BY s.departure_time
                """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                schedules.add(mapSchedule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get schedules error: " + e.getMessage());
        }
        return schedules;
    }

    /**
     * Finds schedules matching origin, destination and a given date.
     * datePrefix is matched against the start of departure_time
     * (so a date like "2025-08-15" matches "2025-08-15 08:30").
     * Any of the filters may be left blank to be ignored.
     */
    public List<Schedule> searchSchedules(String fromLocation, String toLocation, String datePrefix) {
        List<Schedule> schedules = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT s.id, s.train_id, s.route_name, s.from_location, s.to_location,
                       s.price, s.departure_time, t.train_name, t.total_capacity
                FROM Schedules s
                JOIN Trains t ON s.train_id = t.id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (fromLocation != null && !fromLocation.isBlank()) {
            sql.append(" AND LOWER(s.from_location) = LOWER(?)");
            params.add(fromLocation.trim());
        }
        if (toLocation != null && !toLocation.isBlank()) {
            sql.append(" AND LOWER(s.to_location) = LOWER(?)");
            params.add(toLocation.trim());
        }
        if (datePrefix != null && !datePrefix.isBlank()) {
            sql.append(" AND s.departure_time LIKE ?");
            params.add(datePrefix.trim() + "%");
        }
        sql.append(" ORDER BY s.departure_time");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                schedules.add(mapSchedule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Search schedules error: " + e.getMessage());
        }
        return schedules;
    }

    /** Distinct origin cities (for the From dropdown). */
    public List<String> getFromLocations() {
        return distinctColumn("from_location");
    }

    /** Distinct destination cities (for the To dropdown). */
    public List<String> getToLocations() {
        return distinctColumn("to_location");
    }

    private List<String> distinctColumn(String column) {
        Set<String> values = new LinkedHashSet<>();
        String sql = "SELECT DISTINCT " + column + " AS v FROM Schedules "
                + "WHERE " + column + " <> '' ORDER BY v";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String v = rs.getString("v");
                if (v != null && !v.isBlank()) values.add(v);
            }
        } catch (SQLException e) {
            System.err.println("Distinct " + column + " error: " + e.getMessage());
        }
        return new ArrayList<>(values);
    }
}