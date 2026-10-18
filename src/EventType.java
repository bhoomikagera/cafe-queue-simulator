/**
 * The two kinds of events in the simulation.
 *
 * DEPARTURE is listed first on purpose: if a customer finishes at the exact
 * moment another arrives, the departure is handled first, so the barista is
 * counted as free when the new customer shows up.
 */
public enum EventType {
    DEPARTURE,
    ARRIVAL
}
