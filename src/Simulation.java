import java.util.ArrayDeque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Event-driven simulation of a café line.
 *
 * Instead of stepping through time minute by minute, the simulation jumps from
 * one event to the next. A PriorityQueue keeps events sorted by time, and a
 * plain FIFO Queue holds the customers waiting in line.
 *
 *  - ARRIVAL:   if a barista is free, start the order right away;
 *               otherwise the customer joins the back of the line.
 *  - DEPARTURE: a barista finishes and becomes free; if anyone is waiting,
 *               the customer at the front of the line is served next.
 */
public class Simulation {
    private static final double LONG_WAIT_MINUTES = 5.0;

    private final int staffCount;

    public Simulation(int staffCount) {
        if (staffCount < 1) {
            throw new IllegalArgumentException("Need at least one staff member");
        }
        this.staffCount = staffCount;
    }

    public SimulationResult run(List<Customer> customers) {
        PriorityQueue<Event> events = new PriorityQueue<>();
        Queue<Customer> line = new ArrayDeque<>();
        long sequence = 0;

        for (Customer c : customers) {
            events.add(new Event(c.getArrivalTime(), EventType.ARRIVAL, c, sequence++));
        }

        int freeStaff = staffCount;
        int served = 0;
        int waitedOverFive = 0;
        int maxQueue = 0;
        double totalWait = 0;
        double maxWait = 0;
        double busyTime = 0;
        double endTime = 0;

        while (!events.isEmpty()) {
            Event event = events.poll();
            double now = event.getTime();
            endTime = now;

            if (event.getType() == EventType.ARRIVAL) {
                if (freeStaff > 0) {
                    freeStaff--;
                    served++;
                    // No wait: this customer is served immediately.
                    busyTime += event.getCustomer().getServiceTime();
                    events.add(new Event(now + event.getCustomer().getServiceTime(),
                            EventType.DEPARTURE, event.getCustomer(), sequence++));
                } else {
                    line.add(event.getCustomer());
                    maxQueue = Math.max(maxQueue, line.size());
                }
            } else { // DEPARTURE
                freeStaff++;
                if (!line.isEmpty()) {
                    Customer next = line.poll();
                    freeStaff--;
                    served++;
                    double wait = now - next.getArrivalTime();
                    totalWait += wait;
                    maxWait = Math.max(maxWait, wait);
                    if (wait > LONG_WAIT_MINUTES) waitedOverFive++;
                    busyTime += next.getServiceTime();
                    events.add(new Event(now + next.getServiceTime(),
                            EventType.DEPARTURE, next, sequence++));
                }
            }
        }

        if (served == 0) {
            return new SimulationResult(staffCount, 0, 0, 0, 0, 0, 0);
        }
        double utilization = endTime > 0 ? busyTime / (staffCount * endTime) : 0;
        return new SimulationResult(staffCount, served, totalWait / served, maxWait,
                100.0 * waitedOverFive / served, utilization, maxQueue);
    }
}
