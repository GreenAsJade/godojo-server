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

## Running

```
./gradlew build bootRun
```
fires up the server and loads some initial data if there is none.

## Docker

```
./gradlew build docker
```

creates a docker container.

To run a specific prebuilt version:

```
docker run -d -p 8081:8081 greenasjade/godojo:0.8.0
```




