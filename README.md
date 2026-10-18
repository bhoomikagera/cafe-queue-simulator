# Café Queue Simulator

A Java program that simulates a busy café line during a 3-hour morning rush and
compares how different numbers of staff affect customer wait times.

## Why I built it
I work in food service, and during a rush the line can grow quickly. I wanted to
see whether I could model that with code and answer a practical question: how many
people does it take to keep waits short?

## How it works
This is an **event-driven simulation**. Instead of ticking through time minute by
minute, the program jumps from one event to the next, where an event is either a
customer arriving or a customer finishing their order.

- A **PriorityQueue** keeps the events sorted by time, so the earliest event is
  always handled next.
- A plain **FIFO Queue** holds customers waiting in line.
- On an **arrival**, the customer is served right away if a staff member is free;
  otherwise they join the back of the line.
- On a **departure**, that staff member becomes free and the customer at the front
  of the line (if any) is served next.

Customers are generated randomly. The arrival rate changes during the rush
(steady, then a busy peak in the middle, then easing off), and order times are
random with an average of 2 minutes. For each simulated rush, the **same customers**
are used for every staffing level, so the comparison is fair. The results are
averaged over 50 simulated rushes.

## Results
Default run (1 to 4 staff, 50 simulated rushes, about 174 customers each):

| Staff | Avg wait | Max wait | Waiting over 5 min | Staff busy |
|-------|----------|----------|--------------------|------------|
| 1 | 85.1 min | 174.7 min | 93.6% | 99% |
| 2 | 13.5 min | 30.5 min | 65.9% | 88% |
| 3 | 1.6 min | 7.1 min | 10.2% | 64% |
| 4 | 0.3 min | 2.8 min | 0.2% | 48% |

With a goal of an average wait of 2 minutes or less and at most 10% of customers
waiting over 5 minutes, the smallest team that meets it is **4 staff**. Three staff
come very close (the average wait is fine, but 10.2% of customers wait over
5 minutes).

## How to run
You need a JDK installed (check with `javac -version`). From the project folder:

    mkdir -p out
    javac -d out src/*.java

Run the simulation (1 to 4 staff by default):

    java -cp out Main

Compare other staffing levels, or change the number of simulated rushes:

    java -cp out Main 2 3 5
    java -cp out Main 2 3 5 --runs 100

Run the tests:

    java -cp out SimulationTests

## Files
- `src/Customer.java` - a customer's arrival time and order time
- `src/EventType.java`, `src/Event.java` - arrival and departure events, ordered by time
- `src/Simulation.java` - the event-driven simulation for one staffing level
- `src/CustomerGenerator.java` - creates random customers for a rush
- `src/SimulationResult.java` - the measurements (average wait, max wait, and so on)
- `src/Main.java` - runs the scenarios and prints the table and summary
- `src/SimulationTests.java` - 18 checks against answers worked out by hand

## Limitations
- The arrival rates and order times are assumptions, not data from a real café.
- Every staff member is assumed to work at the same speed, and customers never
  leave the line, even if it is very long.
- The results show how staffing affects waits in this model. They aren't a
  recommendation for a real shop.

## Ideas for improvement
- Use real arrival data, or let customers give up when the line is too long
- Add separate roles (register versus drinks) with two queues
- Draw a chart of wait time against staff count
