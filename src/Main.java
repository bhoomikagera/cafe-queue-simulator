import java.util.ArrayList;
import java.util.List;

/**
 * Runs the café simulation for several staffing levels and prints a summary.
 *
 * Usage:
 *   java Main                  compares 1, 2, 3 and 4 staff over 50 simulated rushes
 *   java Main 2 3 5            compares the staffing levels you list
 *   java Main 2 3 5 --runs 100 same, but averaged over 100 simulated rushes
 */
public class Main {
    private static final int DEFAULT_RUNS = 50;
    private static final double TARGET_AVERAGE_WAIT = 2.0;   // minutes
    private static final double TARGET_PERCENT_OVER_5 = 10.0; // percent

    public static void main(String[] args) {
        List<Integer> staffLevels = new ArrayList<>();
        int runs = DEFAULT_RUNS;

        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--runs")) {
                    runs = Integer.parseInt(args[++i]);
                } else {
                    staffLevels.add(Integer.parseInt(args[i]));
                }
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: java Main [staff counts...] [--runs N]");
            System.exit(1);
        }
        if (staffLevels.isEmpty()) {
            staffLevels = List.of(1, 2, 3, 4);
        }
        if (runs < 1 || staffLevels.stream().anyMatch(s -> s < 1)) {
            System.err.println("Staff counts and --runs must be at least 1.");
            System.exit(1);
        }

        // Same customers for every staffing level in each run, so the comparison is fair.
        List<List<SimulationResult>> resultsByLevel = new ArrayList<>();
        for (int s = 0; s < staffLevels.size(); s++) {
            resultsByLevel.add(new ArrayList<>());
        }
        for (int run = 1; run <= runs; run++) {
            List<Customer> customers =
                    CustomerGenerator.generate(run, CustomerGenerator.RUSH_LENGTH_MINUTES);
            for (int s = 0; s < staffLevels.size(); s++) {
                resultsByLevel.get(s).add(new Simulation(staffLevels.get(s)).run(customers));
            }
        }

        List<SimulationResult> averages = new ArrayList<>();
        for (List<SimulationResult> results : resultsByLevel) {
            averages.add(SimulationResult.average(results));
        }

        printTable(averages, runs);
        printSummary(averages);
    }

    private static void printTable(List<SimulationResult> averages, int runs) {
        System.out.println("Cafe rush-hour simulation: 3-hour morning rush, averaged over " + runs + " runs");
        System.out.println();
        System.out.printf("%-6s %-10s %-10s %-10s %-12s %-12s %-10s%n",
                "Staff", "Customers", "Avg wait", "Max wait", "Wait > 5min", "Staff busy", "Max line");
        System.out.println("-".repeat(76));
        for (SimulationResult r : averages) {
            System.out.printf("%-6d %-10d %-10s %-10s %-12s %-12s %-10.1f%n",
                    r.getStaffCount(), r.getCustomersServed(),
                    String.format("%.1f min", r.getAverageWait()),
                    String.format("%.1f min", r.getMaxWait()),
                    String.format("%.1f%%", r.getPercentWaitingOver5()),
                    String.format("%.0f%%", r.getUtilization() * 100),
                    r.getMaxQueueLength());
        }
        System.out.println();
    }

    private static void printSummary(List<SimulationResult> averages) {
        System.out.printf("Summary (goal: average wait of %.0f minutes or less and at most %.0f%% of customers waiting over 5 minutes)%n",
                TARGET_AVERAGE_WAIT, TARGET_PERCENT_OVER_5);

        SimulationResult best = null;
        for (SimulationResult r : averages) {
            boolean meetsGoal = r.getAverageWait() <= TARGET_AVERAGE_WAIT
                    && r.getPercentWaitingOver5() <= TARGET_PERCENT_OVER_5;
            if (meetsGoal && (best == null || r.getStaffCount() < best.getStaffCount())) {
                best = r;
            }
        }
        if (best == null) {
            System.out.println("None of the staffing levels tested met the goal.");
        } else {
            System.out.printf("The smallest team that meets the goal is %d staff (average wait %.1f min, %.0f%% busy).%n",
                    best.getStaffCount(), best.getAverageWait(), best.getUtilization() * 100);
        }
    }
}
