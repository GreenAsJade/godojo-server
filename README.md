# godojo-server
Server for OGS Joseki features

## Dependencies

It expects a Neo4j server installed and running.

To use docker, start the container afresh and set the password:

```
docker run -d  -p 7474:7474 -p 7687:7687 --volume=/home/ec2-user/neo4j/data:/data  --volume=/home/ec2-user/neo4j/logs:/logs  --ulimit=nofile=40000:40000 neo4j:3.5
curl -H "Content-Type: application/json" -X POST -d '{"password":"secret"}' -u neo4j:neo4j http://localhost:7474/user/neo4j/password
```

`application.properties` holds the credentials for godojo to connect to the Neo4j server.

### Versions

```
$ $JAVA_HOME/bin/java -version
java version "1.8.0_144"
Java(TM) SE Runtime Environment (build 1.8.0_144-b01)
Java HotSpot(TM) 64-Bit Server VM (build 25.144-b01, mixed mode)

$ gradle --version

------------------------------------------------------------
Gradle 5.0
------------------------------------------------------------

Build time:   2018-11-26 11:48:43 UTC
Revision:     7fc6e5abf2fc5fe0824aec8a0f5462664dbcd987

Kotlin DSL:   1.0.4
Kotlin:       1.3.10
Groovy:       2.5.4
Ant:          Apache Ant(TM) version 1.9.13 compiled on July 10 2018
JVM:          1.8.0_144 (Oracle Corporation 25.144-b01)
OS:           Mac OS X 10.14.4 x86_64

```

## Building

```
./gradlew build
```

Note that the sanity build testcase requires a neo4j server running.


## Running

```
./gradlew build bootRun
```
fires up the server and loads some initial data if there is none.

Note: before production deployment, `application.properties` needs to get the neo credentials from
the environment.

## Docker

```
./gradlew build docker
```

creates a docker container.

To run a specific prebuilt version:

```
docker run -d -p 8081:8081 greenasjade/godojo:0.8.0
```

Note: for production deployment, you need to pass in the neo credentials as a command line
environment variable, once that is implemented (see above)


