/**
 * Something that happens at a specific time: a customer arriving or a
 * customer finishing their order. Events are stored in a PriorityQueue,
 * so the simulation always handles the earliest event next.
 */
public class Event implements Comparable<Event> {
    private final double time;
    private final EventType type;
    private final Customer customer;
    private final long sequence; // breaks ties so ordering is always predictable

    public Event(double time, EventType type, Customer customer, long sequence) {
        this.time = time;
        this.type = type;
        this.customer = customer;
        this.sequence = sequence;
    }

    public double getTime() { return time; }
    public EventType getType() { return type; }
    public Customer getCustomer() { return customer; }

    /** Earlier time first; then departures before arrivals; then creation order. */
    @Override
    public int compareTo(Event other) {
        int byTime = Double.compare(this.time, other.time);
        if (byTime != 0) return byTime;
        int byType = this.type.compareTo(other.type);
        if (byType != 0) return byType;
        return Long.compare(this.sequence, other.sequence);
    }
}
