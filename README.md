# Generic Backend
Generic Java backend for a mobile game.

- compiled for Java 17
- http server listens on port 8080 by default
- port and backlog parameter can be overriden with command-line arguments like this :
```
  $ .\gradlew run --args="-port <port_number> -backlog <backlog_value>"
```

Some design considerations:
- given that all data is stored in memory and there is no disk/network IO, we can consider all tasks to be CPU bound
- in order to optimize CPU usage, the http server is configured to use a fixed thread pool with number of threads equal to available physical or virtual cores
- in case of high load, the **`<backlog_value>`** param can limit the number of queued requests when all the threads are busy
- using default value for **`<backlog_value>`**, but allow to be overriden for purpose of load testing and finding the optimal value
- http routing and message handling is decoupled from business logic
- all the API logic is contained inside the /api package and the business logic is contained inside the /service package
- most of the classes depend only on interfaces, in order to respect the dependency inversion principle and for ease of testing
- unit tests are limited to the Service classes, since API testing would be difficult to implement