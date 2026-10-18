/**
 * One customer: when they arrive at the café and how long their order takes
 * to prepare. Times are in minutes from the start of the rush period.
 */
public class Customer {
    private final int id;
    private final double arrivalTime;
    private final double serviceTime;

    public Customer(int id, double arrivalTime, double serviceTime) {
        if (arrivalTime < 0 || serviceTime < 0) {
            throw new IllegalArgumentException("Times cannot be negative");
        }
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
    }

    public int getId() { return id; }
    public double getArrivalTime() { return arrivalTime; }
    public double getServiceTime() { return serviceTime; }
}
