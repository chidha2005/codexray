# CodeXRay MVP

CodeXRay MVP is a runnable full-stack app:

- Spring Boot backend for Java static code anatomy analysis
- React + Material UI frontend for pasting Java code and viewing analyzer output

## Requirements

- Java 21+
- Maven 3.9+
- Node.js 20+
- npm 10+

## Run backend

```bash
cd codexray-api
mvn clean spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

## Run UI

Open another terminal:

```bash
cd codexray-ui
npm install
npm run dev
```

UI runs on:

```text
http://localhost:5173
```

## API

```http
POST http://localhost:8080/api/v1/analyze/java
```

Request:

```json
{
  "sourceCode": "public class Demo { }"
}
```

## MVP features

- Java source parsing
- Class extraction
- Method extraction
- Field extraction
- Parameter/local variable extraction
- Object creation extraction
- Loop/condition/method-call extraction
- Basic memory risk detection
- Visual UI panels for summary, risks, classes, methods, variables, object allocations, loops, and method calls

## Current limitations

- Static analysis only
- No runtime execution yet
- No JOL/JFR measurement yet
- Memory numbers are qualitative risk indicators, not measured heap bytes

## Next MVPs

- MVP-2: Stack/heap memory model + Mermaid diagrams
- MVP-3: AI explanation and refactoring recommendations
- MVP-4: Runtime profiler with sandbox + JOL/JFR
