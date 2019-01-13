# godojo-server
Server for OGS Joseki features

This is a prototype.

It expects a Neo4j server installed and running.  I used the default instrutions for Neo4j to achieve that.

./gradlew build bootRun

fires up the server and loads some dummy data.

The Neo4j sever is configured with the default (insecure) password, and this server configured to run on port 8081, both in application.properties.

