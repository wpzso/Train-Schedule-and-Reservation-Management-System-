package controllers;

import models.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingController {

    /**
     * Creates a booking with double-booking and capacity checks.
     * @return null on success, or a descriptive error message string.
     */
    public String createBooking(int scheduleId, String passengerName, String passengerEmail, int seatNumber) {

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Double-booking check: Is this exact seat already taken on this schedule?
            String seatCheck = "SELECT COUNT(*) FROM Bookings WHERE schedule_id = ? AND seat_number = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(seatCheck)) {
                pstmt.setInt(1, scheduleId);
                pstmt.setInt(2, seatNumber);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return "SEAT TAKEN: Seat #" + seatNumber + " is already booked for this schedule. Please choose a different seat.";
                }
            }

            // 2. Capacity check: Has this schedule reached its train's total capacity?
            String capacityCheck = """
                    SELECT t.total_capacity,
                           (SELECT COUNT(*) FROM Bookings b WHERE b.schedule_id = ?) AS booked_count
                    FROM Schedules s
                    JOIN Trains t ON s.train_id = t.id
                    WHERE s.id = ?
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(capacityCheck)) {
                pstmt.setInt(1, scheduleId);
                pstmt.setInt(2, scheduleId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int capacity = rs.getInt("total_capacity");
                    int booked   = rs.getInt("booked_count");
                    if (booked >= capacity) {
                        return "TRAIN FULL: This schedule has reached its maximum capacity of " + capacity + " seats. No more bookings can be made.";
                    }
                    if (seatNumber < 1 || seatNumber > capacity) {
                        return "INVALID SEAT: Seat number must be between 1 and " + capacity + " for this train.";
                    }
                }
            }

            // 3. All checks passed — insert booking (with email)
            String sql = "INSERT INTO Bookings (schedule_id, passenger_name, passenger_email, seat_number) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, scheduleId);
                pstmt.setString(2, passengerName);
                pstmt.setString(3, passengerEmail == null ? "" : passengerEmail);
                pstmt.setInt(4, seatNumber);
                pstmt.executeUpdate();
                return null; // null = success
            }

        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    public boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM Bookings WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Delete booking error: " + e.getMessage());
            return false;
        }
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
                SELECT bk.id, bk.schedule_id, bk.passenger_name, bk.passenger_email, bk.seat_number,
                       sc.route_name, sc.from_location, sc.to_location, sc.price,
                       sc.departure_time, t.train_name
                FROM Bookings bk
                JOIN Schedules sc ON bk.schedule_id = sc.id
                JOIN Trains t    ON sc.train_id     = t.id
                ORDER BY bk.id
                """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Booking bk = new Booking(
                        rs.getInt("id"),
                        rs.getInt("schedule_id"),
                        rs.getString("passenger_name"),
                        rs.getInt("seat_number")
                );
                bk.setPassengerEmail(rs.getString("passenger_email"));
                bk.setRouteName(rs.getString("route_name"));
                bk.setFromLocation(rs.getString("from_location"));
                bk.setToLocation(rs.getString("to_location"));
                bk.setPrice(rs.getDouble("price"));
                bk.setDepartureTime(rs.getString("departure_time"));
                bk.setTrainName(rs.getString("train_name"));
                bookings.add(bk);
            }
        } catch (SQLException e) {
            System.err.println("Get bookings error: " + e.getMessage());
        }
        return bookings;
    }

    /**
     * Returns all booked seat numbers for a given schedule (used by Staff UI seat picker).
     */
    public List<Integer> getBookedSeats(int scheduleId) {
        List<Integer> seats = new ArrayList<>();
        String sql = "SELECT seat_number FROM Bookings WHERE schedule_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                seats.add(rs.getInt("seat_number"));
            }
        } catch (SQLException e) {
            System.err.println("Get booked seats error: " + e.getMessage());
        }
        return seats;
    }
}