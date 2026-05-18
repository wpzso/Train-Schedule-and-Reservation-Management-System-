package controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Computes ticket prices automatically based on the departure time.
 *
 * Business rules:
 *  - Morning trips (departure before 12:00): price in 410..500 SAR.
 *  - Evening trips (departure 12:00 or later): price in 300..390 SAR.
 *  - All prices are multiples of 10.
 *  - The price looks random but is deterministic (derived from the
 *    departure time), so it stays fixed for each schedule and the
 *    admin preview matches the stored value exactly.
 */
public final class PricingService {

    public static final double MIN_PRICE = 300.0;
    public static final double MAX_PRICE = 500.0;
    public static final int    STEP      = 10;

    private static final DateTimeFormatter[] TIME_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm")
    };

    private PricingService() {}

    /** Today's date as yyyy-MM-dd (used to prefill the search field). */
    public static String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /** Human-friendly today's date, e.g. "Monday, 18 May 2026". */
    public static String todayPretty() {
        return LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"));
    }

    /**
     * Extracts the LocalTime from a departure_time string.
     * Accepts "yyyy-MM-dd HH:mm" or just "HH:mm". Falls back to noon
     * if the string can't be parsed.
     */
    private static LocalTime parseTime(String departureTime) {
        if (departureTime != null) {
            String t = departureTime.trim();
            for (DateTimeFormatter fmt : TIME_FORMATS) {
                try {
                    if (fmt.toString().contains("d")) {
                        return java.time.LocalDateTime.parse(t, fmt).toLocalTime();
                    } else {
                        return LocalTime.parse(t, fmt);
                    }
                } catch (Exception ignored) {
                    // try next format
                }
            }
        }
        return LocalTime.NOON; // safe fallback
    }

    /**
     * Computes the ticket price for a given departure time string.
     *
     * Morning  (departure before 12:00): random price in 410..500
     * Evening  (departure 12:00 or later): random price in 300..390
     *
     * The price looks random but is DETERMINISTIC: it is derived from
     * the departure time itself, so the same departure time always yields
     * the same price. This keeps the admin's live preview identical to
     * the value that actually gets stored, and the price stays fixed for
     * each schedule once saved.
     *
     * All prices are multiples of 10.
     */
    public static double priceFor(String departureTime) {
        LocalTime time = parseTime(departureTime);
        boolean morning = time.getHour() < 12;

        int low, high;
        if (morning) {
            low = 410; high = 500;   // morning band
        } else {
            low = 300; high = 390;   // evening band
        }

        // Number of distinct multiples of 10 in [low, high].
        int steps = (high - low) / STEP + 1;

        // Stable pseudo-random pick seeded by the departure time.
        long seed = (time.getHour() * 60L + time.getMinute());
        seed = seed * 2654435761L;                 // Knuth multiplicative hash
        int index = (int) (Math.floorMod(seed, steps));

        double price = low + index * STEP;

        // Safety clamp
        if (price < MIN_PRICE) price = MIN_PRICE;
        if (price > MAX_PRICE) price = MAX_PRICE;
        return price;
    }
}