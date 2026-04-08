# Enterprise Service Request Management System (Java Prototype)

This is a beginner-friendly console application that implements an internal enterprise ticket/request system.

The project was designed to match your assignment goals:

- analyze the business problem
- define requirements
- create an object-oriented design
- implement a working Java prototype
- store data persistently
- include basic testing

## 1) Business Problem

Many companies handle internal requests through emails and spreadsheets. This causes:

- lost requests
- unclear responsibilities
- poor status transparency
- inconsistent response times
- weak management overview

This prototype centralizes those requests in one system.

## 2) Supported Roles

### Employee

- create request
- view own requests
- search own requests
- add comments to own requests
- close own request only when it is already RESOLVED

### Service Agent

- view all requests
- assign a request to themselves
- update status of requests assigned to themselves
- add comments

### Manager

- view all requests
- assign requests to any agent
- update status
- add comments
- view summary dashboard (status distribution + agent workload)

## 3) Mandatory Functions Coverage

The system supports:

- role-based access simulation (login as Employee / Service Agent / Manager)
- creating requests
- assigning category and priority
- request status updates with lifecycle rules
- viewing and searching requests
- persistent data storage on disk
- input validation and error handling

## 4) Request Lifecycle

Implemented statuses:

- NEW
- ASSIGNED
- IN_PROGRESS
- WAITING_FOR_INFORMATION
- RESOLVED
- CLOSED

Transition rules:

- NEW -> ASSIGNED
- ASSIGNED -> IN_PROGRESS / WAITING_FOR_INFORMATION / RESOLVED
- IN_PROGRESS -> WAITING_FOR_INFORMATION / RESOLVED
- WAITING_FOR_INFORMATION -> IN_PROGRESS / RESOLVED
- RESOLVED -> CLOSED / IN_PROGRESS (reopen)
- CLOSED -> no next status

Role-specific rule examples:

- Employee can only close their own resolved requests (RESOLVED -> CLOSED)
- Service Agent can only update requests assigned to themselves

## 5) Object-Oriented Design

Main classes and responsibilities:

- Role: user roles (EMPLOYEE, SERVICE_AGENT, MANAGER)
- RequestCategory: categories for requests
- Priority: request priority levels
- RequestStatus: lifecycle statuses and transition validation
- ActivityLogEntry: one history event with timestamp, author, and message
- ServiceRequest: core domain object for one request/ticket
- DataStore: reads/writes requests to file (serialization)
- RequestService: business logic and validation layer
- TicketSystemApp: console user interface
- TicketSystemBasicTests: simple test runner without external frameworks

This design keeps responsibilities separated:

- UI logic stays in TicketSystemApp
- business rules stay in RequestService
- persistence stays in DataStore

## 6) Project Structure

src/ticketsystem/

- ActivityLogEntry.java
- DataStore.java
- Priority.java
- RequestCategory.java
- RequestService.java
- RequestStatus.java
- Role.java
- ServiceRequest.java
- TicketSystemApp.java
- TicketSystemBasicTests.java

Runtime data file:

- data/requests.ser

## 7) How To Compile and Run

From the repository root:

Compile:

```bash
mkdir -p out
javac -d out src/ticketsystem/*.java
```

Run the application:

```bash
java -cp out ticketsystem.TicketSystemApp
```

Run basic tests:

```bash
java -cp out ticketsystem.TicketSystemBasicTests
```

## 8) Notes For Class Defense

If your instructor asks "Why this design?", you can explain:

1. We separated concerns into UI, business logic, and persistence.
2. We used enums to make statuses, categories, priorities, and roles type-safe.
3. We implemented lifecycle transition checks in one place (RequestStatus.canTransitionTo).
4. We enforced role permissions in RequestService.
5. We added activity logs so each request has traceability.
6. We used simple file persistence (serialization) to keep scope manageable for Java basics.

## 9) Possible Improvements (Future Work)

- replace serialization with a database
- add password-based authentication
- move to a web UI
- add JUnit test suite
- add reporting by category and average resolution time