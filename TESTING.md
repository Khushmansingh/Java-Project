# Sportify testing record

Run source compilation and the focused drivers from the project root. The observed verification here used JDK 27. JDBC cases require a running MySQL 8 server, Connector/J, the schema, and valid connection settings in `src/database/DBConnection.java`; that database was not configured during this run.

| ID | Case | Expected | Actual / status |
|---|---|---|---|
| T01 | Valid team and player | Accepted | Pass: `CollectionsDemo` created two teams and three players. |
| T02 | Duplicate team name ignoring case | Rejected | Pass: `CollectionsDemo` checked this. |
| T03 | Duplicate jersey in one team | Rejected | Pass: `CollectionsDemo` observed `DuplicateRecordException`. |
| T04 | Age outside 14–50 | Rejected | Pass: `CollectionsDemo` observed `InvalidInputException` for age 13. |
| T05 | Unknown ID | Friendly error | Pass: `CollectionsDemo` observed `InvalidInputException` for unknown player ID. |
| T06 | Team scheduled against itself | Rejected | Pass: `CollectionsDemo` observed `InvalidInputException` from `Match`. |
| T07 | Team booked twice on one date | Rejected | Not run. |
| T08 | Venue slot already taken | Rejected | Pass within concurrency demo: four of five threads failed to book the slot. |
| T09 | Five concurrent bookings for one slot | Exactly one succeeds | Pass: `ConcurrencyDemo` observed one successful booking. |
| T10 | Enter result twice | Second entry rejected | Pass: `CollectionsDemo` observed `InvalidResultException` on duplicate result. |
| T11 | Draw points | One point each for football | Pass: `CollectionsDemo` confirmed one point per team. |
| T12 | Points table tie-break order | Points, score difference, then name | Pass: `CollectionsDemo` checked score-difference and name tie-breaks. |
| T13 | Queue add/consume | No loss or duplication | Pass: `QueueAndExecutorDemo` observed the queued result completed. |
| T14 | Persistence after restart | Data remains | Not run; MySQL was not configured. |
| T15 | Apostrophe in name | Stored as data | Not run against MySQL. CSV parser sample contains `O'Brien`. |
| T16 | Forced transaction failure | All three table updates roll back | Not run; requires MySQL. `recordResultInDatabase(..., true)` provides the forced failure path. |
| T17 | Wrong password and three-attempt lock | Reject and lock | Not run; requires MySQL admin row. |
| T18 | Malformed CSV | Rejected with row number | Pass: `CsvValidationDemo` observed `Row 2: Expected 6 columns.` |
| T19 | CSV export | File has expected headings | Not run; requires MySQL. |
| T20 | Delete team with players/matches | Graceful block | Pass for team with players in `CollectionsDemo`; matches case not run. |

Observed commands/results:

- Full `javac` compile of all `src/**/*.java`: passed after all milestone additions.
- `java -cp out main.ModelDemo`: printed captain, Goals, and 3 points for a win.
- `java -cp out main.CollectionsDemo`: printed `Collection and rule checks passed...`.
- `java -cp out main.ConcurrencyDemo`: printed one successful booking, the interrupt handling message, and `500 synchronized point updates`.
- `java -cp out main.QueueAndExecutorDemo`: passed; queued result completed and Callable returned 1 completed match.
- `java -cp out main.AnalyticsDemo`: grouped match statuses and wins and printed averages/statistics.
- `java -cp out main.CsvValidationDemo`: passed and reported the malformed second row number.
- The seeded `sportify123` PBKDF2 hash was verified with the Java `PasswordHasher`.
