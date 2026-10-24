# Sequent

A Java command-line task scheduler. It reads tasks and their prerequisites from a CSV file, validates the input, builds a task-dependency graph, and prints an execution order in which every task comes after all of its prerequisites.

It uses Kahn’s algorithm for topological sorting and a priority queue to choose among tasks that are ready to run.

## Features

- Parses tasks from a CSV file and reports malformed rows with line numbers.
- Validates duplicate IDs, unknown prerequisites, self-dependencies, and repeated prerequisites.
- Detects circular dependencies and reports tasks that could not be scheduled.
- Supports priority, deadline, and task-ID-based ordering.

## Compile and run

Requires Java 11 or newer (the code uses `String.isBlank`). From the project root (Linux, macOS, or Git Bash on Windows):

```bash
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out main.java.Main
```

The program currently reads its input from `src/task.csv` (the path is fixed in `Main`).

## CSV format

First line is a header and is skipped (not validated). Blank lines are ignored. Each remaining line has exactly six comma-separated columns:

| Column | Rules                                         |
|---|-----------------------------------------------|
| `id` | Non-empty, unique, case-sensitive             |
| `title` | Non-empty, must not contain commas            |
| `priority` | `LOW`, `MEDIUM` or `HIGH` (case-insensitive)  |
| `duration` | Positive whole number of hours                |
| `deadline` | `YYYY-MM-DD`, or blank for no deadline        |
| `prerequisites` | Task IDs separated by `\|`, or blank for none |

Example (`tests/fixtures/valid.csv`):

```
id,title,priority,duration,deadline,prerequisites
T1,Setup,HIGH,2,2026-11-01,
T2,Design,MEDIUM,3,,T1
T10,Docs,MEDIUM,1,,T1
T3,Backend,HIGH,5,2026-11-10,T2
T4,Frontend,HIGH,4,2026-11-05,T2
T5,Integrate,HIGH,2,,T3|T4
T6,Review,LOW,1,2026-11-20,T5
T7,Release,LOW,1,2026-11-15,T5
T8,Cleanup,LOW,1,,
```

Resulting order: `T1, T10, T2, T4, T3, T5, T7, T6, T8`.

## Validation and errors

The program stops at the first error it finds and prints an error message to the terminal.

| Stage                            | Error prefix               | Examples                                                                                                                                                                                                                             |
|----------------------------------|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Parsing (per row, in file order) | `CSV error:`               | `Line 4: Expected 6 columns but found 5 columns.`, `Line 3: Invalid Priority "URGENT".`, `Line 2: Duration hours must be greater than zero.`, `Line 5: Invalid Deadline "31-12-2026".`, `Line 6: A Prerequisite ID cannot be empty.` |
| Graph validation                 | `Graph error:`             | `Duplicate Task ID: T2`, `Task T2 cannot depend on itself.`, `Task T3 references unknown prerequisite: T9`, `Task T2 lists prerequisite T1 more than once.`                                                                          |
| Scheduling                       | `Scheduling error:`        | `Circular dependency detected. 3 of 3 tasks cannot be scheduled: [T1, T2, T3]`                                                                                                                                                       |
| File access                      | `Could not read the file:` | missing or unreadable `src/task.csv`                                                                                                                                                                                                 |


## Scheduling order

A task is *ready* once all of its prerequisites have been scheduled.

Among ready tasks, Sequent chooses the next task using:

1. Priority: `HIGH`, then `MEDIUM`, then `LOW`.
2. Earlier deadline first; tasks without a deadline are scheduled last.
3. Task ID in string order (so `T10` comes before `T2`).

Prerequisites always come first, regardless of priority: a `LOW` prerequisite is scheduled before a `HIGH` task that depends on it. This keeps the output dependency-safe and deterministic.

## Circular dependencies

If Sequent cannot schedule every task, it reports a circular dependency. The error lists all tasks that could not be scheduled.

Some of those tasks may be part of the cycle, while others may simply depend on a task in the cycle. See `tests/fixtures/cycle-with-downstream.csv` for an example.

## Complexity

With `V` tasks and `E` prerequisite links:

- Parsing: `O(V + E)`.
- Validation and graph building: `O(V + E)` expected, using hash lookups.
- Scheduling: `O(V log V + E)`. Each task enters and leaves the priority queue once, and each link causes one *in-degree* update.
- Space: `O(V + E)`.

## Design decisions

- Tasks are stored in a `HashMap` for quick lookup by ID.
- **Kahn’s algorithm** is used for topological sorting. It is iterative, so long dependency chains do not risk stack overflow, and a cycle is detected when fewer tasks are scheduled than the total number of tasks.
- A `PriorityQueue` selects the next ready task, making the output deterministic through priority, deadline, and task-ID ordering.
- `Task` holds only CSV data; scheduling state such as in-degree and dependents is kept in `Scheduler`, so the same task list can be scheduled more than once.
- Separate error messages distinguish CSV parsing, invalid dependency graphs, and circular dependencies.

## Known limitations

- CSV parsing uses a simple comma split, so quoted fields and commas inside titles are not supported.
- The header row is skipped.
- Only the first error is reported.
- The scheduler produces a single sequential order; duration is displayed but does not affect scheduling.
- Priority is considered before deadline, so the output is a deterministic ordering rather than an optimal schedule.
- The cycle error lists all tasks that could not be scheduled, including tasks that depend on the cycle.
- The input path is fixed to `src/task.csv`.
- Automated unit tests are planned; current regression checks use CSV fixtures.

## Running the fixtures

`tests/fixtures/` contains the input CSV files. `tests/expected/` contains the expected standard output `(.out)` and error output `(.err)` for each fixture.

The runner temporarily replaces `src/task.csv` with a fixture, runs the program, compares the output, and restores the original file.

```bash
bash tests/run-fixtures.sh valid                # check one fixture: prints PASS or FAIL
bash tests/run-fixtures.sh valid --save         # regenerate its expected output
for f in valid duplicate-id unknown-prerequisite self-dependency pure-cycle cycle-with-downstream; do
  bash tests/run-fixtures.sh "$f"
done
```

| Fixture                 | Expected result                                                              |
|-------------------------|------------------------------------------------------------------------------|
| `valid`                 | Full schedule; no error output                                               |
| `duplicate-id`          | `Graph error: Duplicate Task ID: T2`                                         |
| `unknown-prerequisite`  | `Graph error: Task T3 references unknown prerequisite: T9`                   |
| `self-dependency`       | `Graph error: Task T2 cannot depend on itself.`                              |
| `pure-cycle`            | `Scheduling error: ... 3 of 3 tasks cannot be scheduled: [T1, T2, T3]`       |
| `cycle-with-downstream` | `Scheduling error: ... 5 of 8 tasks cannot be scheduled: [T3, T4, T5, T6, T7]` |