import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Simple tests with no external libraries. Each test checks the simulation
 * against an answer worked out by hand.
 *
 * Run with:  java SimulationTests
 */
public class SimulationTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        oneBaristaMakesSecondCustomerWait();
        twoBaristasMeansNoWaiting();
        departureHandledBeforeArrivalAtSameTime();
        emptyDayDoesNotCrash();
        lineIsServedInOrderOfArrival();
        eventsComeOutInTimeOrder();
        generatorIsRepeatableForSameSeed();
        generatedArrivalsAreSortedAndInsideRush();
        moreStaffNeverIncreasesAverageWait();
        invalidStaffCountIsRejected();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    // --- tests -----------------------------------------------------------

    static void oneBaristaMakesSecondCustomerWait() {
        // A arrives at 0 and takes 5 min. B arrives at 1 and must wait until 5 -> waits 4.
        List<Customer> customers = List.of(new Customer(1, 0, 5), new Customer(2, 1, 3));
        SimulationResult r = new Simulation(1).run(customers);
        check("one barista: customers served", r.getCustomersServed() == 2);
        check("one barista: average wait is 2.0", close(r.getAverageWait(), 2.0));
        check("one barista: max wait is 4.0", close(r.getMaxWait(), 4.0));
        check("one barista: longest line is 1", close(r.getMaxQueueLength(), 1));
    }

    static void twoBaristasMeansNoWaiting() {
        List<Customer> customers = List.of(new Customer(1, 0, 5), new Customer(2, 1, 3));
        SimulationResult r = new Simulation(2).run(customers);
        check("two baristas: nobody waits", close(r.getAverageWait(), 0.0) && close(r.getMaxWait(), 0.0));
    }

    static void departureHandledBeforeArrivalAtSameTime() {
        // A finishes at exactly 2.0, the moment B arrives, so B should not wait.
        List<Customer> customers = List.of(new Customer(1, 0, 2), new Customer(2, 2, 1));
        SimulationResult r = new Simulation(1).run(customers);
        check("same-time departure: B does not wait", close(r.getMaxWait(), 0.0));
    }

    static void emptyDayDoesNotCrash() {
        SimulationResult r = new Simulation(2).run(new ArrayList<>());
        check("empty day: zero customers and zero wait",
                r.getCustomersServed() == 0 && close(r.getAverageWait(), 0.0));
    }

    static void lineIsServedInOrderOfArrival() {
        // One barista. A 0->4, B arrives 1 (service 2), C arrives 2 (service 2).
        // B starts at 4 (waits 3), C starts at 6 (waits 4). Average wait = 7/3.
        List<Customer> customers = List.of(
                new Customer(1, 0, 4), new Customer(2, 1, 2), new Customer(3, 2, 2));
        SimulationResult r = new Simulation(1).run(customers);
        check("FIFO line: average wait is 7/3", close(r.getAverageWait(), 7.0 / 3.0));
        check("FIFO line: max wait is 4.0", close(r.getMaxWait(), 4.0));
        check("FIFO line: longest line is 2", close(r.getMaxQueueLength(), 2));
    }

    static void eventsComeOutInTimeOrder() {
        Customer c = new Customer(1, 0, 1);
        PriorityQueue<Event> events = new PriorityQueue<>();
        events.add(new Event(5.0, EventType.ARRIVAL, c, 0));
        events.add(new Event(2.0, EventType.ARRIVAL, c, 1));
        events.add(new Event(2.0, EventType.DEPARTURE, c, 2));
        check("events: earliest first", close(events.peek().getTime(), 2.0));
        check("events: departure before arrival at same time",
                events.poll().getType() == EventType.DEPARTURE);
        check("events: arrival at 2.0 comes next", events.poll().getType() == EventType.ARRIVAL);
        check("events: latest last", close(events.poll().getTime(), 5.0));
    }

    static void generatorIsRepeatableForSameSeed() {
        List<Customer> a = CustomerGenerator.generate(42, 180);
        List<Customer> b = CustomerGenerator.generate(42, 180);
        boolean same = a.size() == b.size();
        for (int i = 0; same && i < a.size(); i++) {
            same = close(a.get(i).getArrivalTime(), b.get(i).getArrivalTime())
                    && close(a.get(i).getServiceTime(), b.get(i).getServiceTime());
        }
        check("generator: same seed gives same customers", same);
    }

    static void generatedArrivalsAreSortedAndInsideRush() {
        List<Customer> customers = CustomerGenerator.generate(7, 180);
        boolean ok = !customers.isEmpty();
        double previous = 0;
        for (Customer c : customers) {
            if (c.getArrivalTime() < previous || c.getArrivalTime() >= 180
                    || c.getServiceTime() < 0.5) {
                ok = false;
            }
            previous = c.getArrivalTime();
        }
        check("generator: arrivals sorted, inside the rush, orders take at least 30 seconds", ok);
    }

    static void moreStaffNeverIncreasesAverageWait() {
        boolean ok = true;
        for (long seed = 1; seed <= 30; seed++) {
            List<Customer> customers = CustomerGenerator.generate(seed, 180);
            double previous = Double.MAX_VALUE;
            for (int staff = 1; staff <= 5; staff++) {
                double wait = new Simulation(staff).run(customers).getAverageWait();
                if (wait > previous + 1e-9) ok = false;
                previous = wait;
            }
        }
        check("more staff never makes the average wait longer (30 rushes, 1-5 staff)", ok);
    }

    static void invalidStaffCountIsRejected() {
        boolean rejected = false;
        try {
            new Simulation(0);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check("zero staff is rejected", rejected);
    }

    // --- tiny test helpers ----------------------------------------------

    static boolean close(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }
}
