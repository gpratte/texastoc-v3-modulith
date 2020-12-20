# texastoc-v3
Refactor version 2 to version 3.

Version 3 will employ a modular monolith architecture.

Break the code into modules by
determining the bounded context of the data.
use Spring Data JDBC for accessing the aggregate
use Java packaging to encapsulate a module
use ArchUnit to enforce package separation
access modules via a single module interface (sideway calls)
use Spring Integration for messaging


# Profiles, Building and Running
The spring boot application can be run
* in IntelliJ (embedded tomcat)
* maven command line (embedded tomcat)
* as a war deployed to an installed tomcat (not embedded)
* as a jar using the webapp-runner.jar which is similar to an installed tomcat (not embedded)

There are two profile settings for building and running the Spring Boot Server: a the maven profile setting and a spring profile setting.
To make things confusing they
* often have the same name.
* the absence of a maven profile will fallback to a default profile
* the absence of a spring profile can cause a runtime configuration change

### Maven Profile
Maven profiles are defined in the <profiles> section in the pom.xml. There are three profiles defined: dev, dev-msql and prod.

The dev profile is set as the default and hence is used if no profile is specified (or if the dev profile is specified). A profile can be specified on the command line by using the "-P" flag (e.g. -P dev). A profile can be specified in IntelliJ by selecting a profile from the list of Profiles in the maven tool window.

The maven dev profile
  1. brings in H2 as a dependency
  2. brings in tomcat as a dependency (i.e. embedded)
  3. All database tables will be created and seeded. The tables and seed data can be found in the *create_toc_schema.sql* file.

The maven dev-msql profile
  1. brings in MySQL as a dependency
  2. brings in tomcat as a dependency (i.e. embedded)

The maven prod profile
  1. brings in MySQL as a dependency
  2. brings in tomcat as a runtime dependency (i.e. not embedded)


### Spring Profile
A spring profile, if set, can be used to conditionally include spring configuration, beans, ... . The same is true for the absence of a spring profile.

When running the mvn command line tool the active spring profile can be set using the -Dspring-boot.run.profiles=abc command line argument.

To set the spring runtime profile in IntelliJ use the "VM options" with the value "-Dspring.profiles.active=abc" (without the double quotes).


### Running the dev server
Running the maven dev profile uses an embedded, in-memory H2 database and an embedded tomcat server. For this to work the maven profile must be 'dev' and no spring profile is required.

To run in IntelliJ select dev from the maven profiles in the maven tool window. Right click on the Application class and select Run from the popup window.

To run with `mvn` all you have to do is type `mvn -pl application spring-boot:run`

The `-pl application` part of the command instructs maven to run the application module which is where the server code is. There is another module called integration which, when run, runs the integration tests.

The war can be run in webapp-runner. This is how the server is deployed to Heroku.
* Build a dev war file by typing `mvn -pl application clean package`
* Run the server by typing `java -jar application/target/dependency/webapp-runner.jar application/target/texastoc-v2-application-1.0.war`

The dev server is just that - it is meant for development. There is really not reason to run it in an installed tomcat.


### Running the dev-mysql server
Running the maven dev-mysql profile which uses a MySQL database as an embedded tomcat server. For this to work the maven profile must be 'dev-mysql' and the spring profile must be 'mysql'.

Remember to have a running MySQL database for the server to use.

To run in IntelliJ select dev-mysql from the maven profiles in the maven tool window. Set the VM options with the value "-Dspring.profiles.active=mysql" (without the double quotes). Right click on the Application class and select Run from the popup window.

To run with `mvn` type `mvn -Dspring-boot.run.profiles=mysql -P dev-mysql -pl application spring-boot:run`

There is really no reason to run the dev-mysql server in an installed tomcat.

### Running the prod server
Running the maven prod profile uses a MySQL database and runs as a war file deployed to an installed (i.e. not embedded) tomcat. For this to work the maven profile must be 'prod' and the spring profile must be 'mysql'.

Remember to have a running MySQL database for the server to use.

Running the prod server should not be done with IntelliJ or from mvn.

A war file must be built by typing `mvn -P prod -pl application clean package`

The war can be run in webapp-runner by typing `java -jar -Dspring.profiles.active=mysql application/target/dependency/webapp-runner.jar application/target/texastoc-v2-application-1.0.war`

For production deploy the war to a tomcat installation and remember to set "-Dspring.profiles.active=mysql" (without the double quotes). The variable can be set in the setenv.sh file as follows:
* JAVA_OPTS="-Dspring.profiles.active=mysql"


### Build and Run commands
Run the dev server: `mvn -pl application spring-boot:run`

Run the dev server in debug mode: `mvn -pl application spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8787"`

Run the dev-mysql server: `mvn -P dev-mysql -Dspring-boot.run.profiles=mysql -pl application spring-boot:run`

For the dev war
* Build a dev war: `mvn -pl application clean package`
* Run the dev war: `java -jar application/target/dependency/webapp-runner.jar application/target/texastoc-v2-application-1.0.war`
* Deploy the dev war to Heroku: `mvn -pl application clean heroku:deploy-war`
* Tail the Heroku logs: `heroku logs --app texastoc-server --tail`

For the prod war
* Build a prod war: `mvn -P prod -pl application clean package`
* Run the prod war: `java -jar -Dspring.profiles.active=mysql application/target/dependency/webapp-runner.jar application/target/texastoc-v2-application-1.0.war`
* Deploying the prod war instruction are still a TODO

# Connect to the H2 server
When running the dev server the H2 database can be access as follows:
* open `http://localhost:8080/h2-console` url.
* set the JDBC URL to `jdbc:h2:mem:testdb`
* User Name `sa`
* Leave the password empty
* Click Connect

# Run tests
You can run the tests in IntelliJ or from the command line.

To run in IntelliJ right click on the java folder and choose _Run 'All Tests'_
* application -> src -> test -> java

To run all the tests from the command line type
* mvn test


# WebSocket
On branch 54-clock-web-socket added a websocket to the server. In the future this websocket
will be used to communicate a running clock to the client.

The client is going to first use polling so the websocket requirement has be put on hold.

# SSL certificate
Using LetsEncrypt for the SSL certificate.

To generate/renew

```
certbot certonly \
  --manual \
  --preferred-challenges=dns \
  --email <my email> \
  --agree-tos \
  --config-dir ./config \
  --logs-dir ./logs \
  --work-dir ./work \
  --cert-name texastoc.com \
  -d texastoc.com \
  -d www.texastoc.com
```

# Branches
The branch labels are prefixed in the order they were developer (e.g. 01-, 02, ...).

Choose the branch from the github list of branches to see the readme for that branch.

To see the code for a branch compare the branch to the previous branch.

## Current Branch: 07-module-package
Created the com.texastoc.module package and moved all the modules (game, player, ...) there.
