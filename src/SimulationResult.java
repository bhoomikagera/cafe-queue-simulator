import java.util.List;

/** The numbers measured from one simulated rush period (or the average of several). */
public class SimulationResult {
    private final int staffCount;
    private final int customersServed;
    private final double averageWait;      // minutes
    private final double maxWait;          // minutes
    private final double percentWaitingOver5; // 0-100
    private final double utilization;      // 0-1, fraction of staff time spent working
    private final double maxQueueLength;

    public SimulationResult(int staffCount, int customersServed, double averageWait,
                            double maxWait, double percentWaitingOver5,
                            double utilization, double maxQueueLength) {
        this.staffCount = staffCount;
        this.customersServed = customersServed;
        this.averageWait = averageWait;
        this.maxWait = maxWait;
        this.percentWaitingOver5 = percentWaitingOver5;
        this.utilization = utilization;
        this.maxQueueLength = maxQueueLength;
    }

    public int getStaffCount() { return staffCount; }
    public int getCustomersServed() { return customersServed; }
    public double getAverageWait() { return averageWait; }
    public double getMaxWait() { return maxWait; }
    public double getPercentWaitingOver5() { return percentWaitingOver5; }
    public double getUtilization() { return utilization; }
    public double getMaxQueueLength() { return maxQueueLength; }

    /** Averages each measurement across many simulated rush periods. */
    public static SimulationResult average(List<SimulationResult> results) {
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Need at least one result to average");
        }
        double served = 0, avgWait = 0, maxWait = 0, over5 = 0, util = 0, maxQueue = 0;
        for (SimulationResult r : results) {
            served += r.customersServed;
            avgWait += r.averageWait;
            maxWait += r.maxWait;
            over5 += r.percentWaitingOver5;
            util += r.utilization;
            maxQueue += r.maxQueueLength;
        }
        int n = results.size();
        return new SimulationResult(results.get(0).staffCount,
                (int) Math.round(served / n), avgWait / n, maxWait / n,
                over5 / n, util / n, maxQueue / n);
    }
}
