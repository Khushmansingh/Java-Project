# F1 Team Management & Transfer Market System

Small Java 17 console project using MySQL and JDBC.

## MySQL setup

1. Install MySQL Server. The project includes MySQL Connector/J in the `lib` folder.
2. Run `schema.sql` in MySQL Workbench or with `mysql -u root -p < schema.sql`.
3. Open `src/Database.java` and set `URL`, `USER`, and `PASSWORD` for your local MySQL account. The default URL is `jdbc:mysql://localhost:3306/f1_management`, the user is `root`, and the sample password is `password`.

The schema inserts three repeatable example drivers and the Red Falcon team. The application reads demo data from MySQL; it does not create the story data in memory.

## Compile and run

Windows PowerShell example (replace the JAR path as needed):

```powershell
javac -cp ".;lib\mysql-connector-j-26.10.0.jar" -d out src\*.java
java -cp "out;lib\mysql-connector-j-26.10.0.jar" Main
```

On macOS/Linux use `:` instead of `;` in the classpath and `/` in place of the Windows path separators. Run the schema once before starting the program. CSV import reads a path entered at the prompt; `drivers.csv` is included as a sample. Java source is in `src`, the JDBC library is in `lib`, and compiled classes go in `out`.

## Example workflow

Run the schema, start Main, create an offer for team 1 with minimum rating 80, a future deadline, and salary/transfer amounts within the team budget. Check IDs 1, 2, and 3 for eligibility. IDs 1 and 2 should pass and ID 3 should fail. Submit requests for IDs 1 and 2, shortlist the request for ID 1, then sign it. Signing updates the driver's team, subtracts salary and transfer amount from the team budget, and updates the request in one transaction. The team has two seats; use a second signed driver to fill its remaining seat.

Signing is serialized with a transaction and team-row lock. The request uniqueness rule is enforced in MySQL as well as checked by the service.

## Test sheet

| ID | Test | Expected result | Actual | Pass/Fail |
|---|---|---|---|---|
| T01 | Register a complete driver with rating 0 | Driver is stored | | |
| T02 | Register a driver with rating above 100 | Validation rejects the driver | | |
| T03 | Register a second driver with an existing ID | Duplicate ID message | | |
| T04 | Register email differing only by letter case | Duplicate email message | | |
| T05 | Search for an absent driver ID | Clear not-found message | | |
| T06 | Update an existing driver profile | Profile changes without a duplicate | | |
| T07 | Add a team ID already in use | Duplicate record/database constraint message | | |
| T08 | Check rating equal to minimum rating | ELIGIBLE | | |
| T09 | Check rating below minimum rating | NOT ELIGIBLE with reason | | |
| T10 | Submit request for unfit driver | Request rejected | | |
| T11 | Submit request after offer deadline | Closed/past-deadline message | | |
| T12 | Submit the same driver and offer twice | Duplicate request rejected | | |
| T13 | Transition PENDING to SIGNED directly | Transition rejected | | |
| T14 | Shortlist, then sign an eligible request | Driver assigned and budget reduced atomically | | |
| T15 | Import included drivers.csv | Three rows imported, invalid row rejected with row number | | |
| T16 | Run concurrency demo | At most two reservation successes | | |

## Milestone map

| Milestone | Files |
|---|---|
| 1 OOP and validation | Driver.java, Team.java, TransferOffer.java, TransferRequest.java, Eligible.java, CustomExceptions.java |
| 2 Collections | DriverService.java, TransferService.java, ReportService.java |
| 3 Multithreading | SlotManager.java, TransferTask.java, Main.java |
| 4 Producer-consumer and executor | TransferTask.java |
| 5 JDBC and transactions | Database.java, DriverRepository.java, TeamRepository.java, schema.sql |
| 6 Lambdas and streams | ReportService.java, TransferService.java |
| 7 Generics and files | Repository.java, DriverRepository.java, TeamRepository.java, CsvHelper.java |
| 8 Integration | Main.java and all supporting files |

## Notes

Use only demo or otherwise appropriate credentials in `src/Database.java`. The tables include foreign keys and checks; MySQL 8.0.16+ enforces CHECK constraints.
