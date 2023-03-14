# Genetic Algorithms Platform
This is platform for running, benchmarking & experimenting with mTSP problems using
genetic algorithms.

## Frontend (SolidJS)
Initialized with:
```shell
npx degit solidjs/templates/ts frontend
```
Run with:
```shell
npm run dev # (starts on port 3000)
```

## REST API (Starlette)
Implemented with async web framework Starlette and Sqlite database as storage backend.
```shell
uvicorn backend.main:app --reload --port 8080
```

## Runner (Java)
Python REST backend manages running Java processes because they are more performant
and Python is better suited for REST API. Python spawns java subprocess and manages
its lifecycle.
To debug a websocket messages, use wscat:
```shell
wscat --no-check -c wss://127.0.0.1/api/stream
```

REST fully trusts data comming from runner processes.

```shell
mvn archetype:generate -DgroupId=hr.fer.bernardcrnkovic.mtsp -DartifactId=mtsp-backend -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
```

## Reverse proxy
Using Caddy to keep frontend and backend behind same host:
```shell
caddy run # (see Caddyfile)
```

## Problems
TSPLib problems are availabe [here](http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/tsp/).
Three size categories were used in this project:
~ 30 nodes
~ 150 nodes
~ 1000 nodes

