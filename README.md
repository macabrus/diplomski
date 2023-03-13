# Frontend (SolidJS)
Initialized with:
```shell
npx degit solidjs/templates/ts frontend
```

# REST API (Starlette)
Implemented with async web framework Starlette and Sqlite database as storage backend.

# Runner (Java)
Python REST backend manages running Java processes because they are more performant
and Python is better suited for REST API. Communication is done using ZMQ library.
Python acts as ZMQ broker and passes information for Java processes to connect back to
when spawning them. Java process receives ZMQ task and starts processing until
pause message arrives. After pausing the task execution, it is serialized and sent
back to the REST backend where it is stored to the database. Message broker
abstraction allows us to run Java processes on different machines from REST backend.

REST fully trusts data comming from runner processes.

```shell
mvn archetype:generate -DgroupId=hr.fer.bernardcrnkovic.mtsp -DartifactId=mtsp-backend -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
```



# Problems
TSPLib problems are availabe [here](http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/tsp/).
Three size categories were used in this project:
~ 30 nodes
~ 150 nodes
~ 1000 nodes

