import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates a realistic list of customers for a morning rush.
 *
 * Arrivals are random, but the average arrival rate changes over time:
 * steady at the start, busiest in the middle (the peak of the rush), then
 * easing off. Order times are random too, with a minimum of 30 seconds.
 *
 * Giving the generator a seed makes the "random" customers repeatable, so the
 * same rush can be replayed with different numbers of staff.
 */
public class CustomerGenerator {
    public static final double RUSH_LENGTH_MINUTES = 180;

    private static final double PEAK_RATE = 1.4;   // customers per minute at the busiest time
    private static final double MIN_SERVICE = 0.5; // minutes
    private static final double MEAN_EXTRA_SERVICE = 1.5; // average service = 0.5 + 1.5 = 2 min

    /** Average customers arriving per minute at time t (in minutes). */
    static double arrivalRate(double t) {
        if (t < 60) return 0.7;
        if (t < 120) return PEAK_RATE;
        return 0.8;
    }

    public static List<Customer> generate(long seed, double durationMinutes) {
        Random random = new Random(seed);
        List<Customer> customers = new ArrayList<>();
        double t = 0;
        int id = 1;
        while (true) {
            // Generate candidate arrivals at the peak rate, then keep each one with
            // probability rate(t)/peakRate. This produces the changing rate correctly.
            t += exponential(random, 1.0 / PEAK_RATE);
            if (t >= durationMinutes) break;
            if (random.nextDouble() < arrivalRate(t) / PEAK_RATE) {
                double service = MIN_SERVICE + exponential(random, MEAN_EXTRA_SERVICE);
                customers.add(new Customer(id++, t, service));
            }
        }
        return customers;
    }

    /** A random wait drawn from an exponential distribution with the given mean. */
    private static double exponential(Random random, double mean) {
        return -mean * Math.log(1.0 - random.nextDouble());
    }
}
