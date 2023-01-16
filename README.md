# Cas Spring-Boot Demo
This project is a `Spring-Boot` simple application where the login is authenticated by a [CAS](https://www.apereo.org/projects/cas) Server (Apereo Central Authentication Service). 
It includes the needed configuration to connect the client application to the Cas Server. The users allowed to log in are stored in a 
`Postgres` database, its properties are also exposed in this tutorial.

So for run the project you must have the server and the client running simultaneously

## Prerequisites

The version used to run the projects are the following:
- Java 17.0.5
- Gradle 5.6.3

We need to deploy both server and client under `HTTPS` protocol due to the exchange of tickets and validation info made it must be under a secure protocol,
for this reason we need to generate a key that will be used by them. We have to go to our pc root directory and find the `etc` folder, usually it is hidden,
for showing it press `CMD + SHIFT + . `. Inside this folder we create a new one named `cas`. Once we have achived it we run the following command:

`keytool -genkey -keyalg RSA -alias thekeystore -keystore thekeystore -storepass changeit -validity 360 -keysize 2048 `

In order for us not to have an SSL handshake error, we should type localhost as the value for first name, last name, organisatin name and 
organisation unit.
Furthermore we have to import the keystore into de TDk we will be usin to run our client app with this command:

`keytool -importkeystore -srckeystore thekeystore -destkeystore $JAVA_HOME/lib/security/cacerts`

The **password** for the source and destination keystore is `changeit`. On Unix systems, we may have to run this command with admin (sudo) privilege. 
After importing, we should restart all instances of Java that's running or restart the system.

Make sure that this route is appropiate because if you are using JRE it may change.

## Getting started
  1. Clone the repository to your local machine using `git clone https://github.com/wearearima/cas-spring-boot-demo.git`
  2. Navigate to the server directory using `cd cas-spring-boot-demo/tutorials/security-modules/cas/cas-server`
  3. Run the command `./gradlew clean copyCasConfiguration build run` to start the CAS server and copy the config of the `cas.properties` file 
  to your root folder. tYour terminal should show this:
  
  ![Server Ready](https://github.com/wearearima/cas-spring-boot-demo/blob/main/static/Screenshot%202023-01-16%20at%2013.35.11.png)
  
  You could access to the server app via `https://localhost:8443`
  
  4. Secondly, locate the `CasSecuredApp.java` file an open it. Depending on your IDE you could double click it and choose the `Run Java` java option
  or a play icon would appear on the right upper corner of your screen.
  5. You could access to the client app via `https://localhost:8900`

##  Configuration
 
Once we have both deployed we are going to explore the differet condifuration made for its proper functioning and how to register a client application
to the CAS Server.

### Cas Server
> Directory: /tutorials/security-modules/cas/cas-server

There are differents ways to register an application; YAML, JSON, MongoDB, LDAP and go on. We have chosen via `JSON`, this file is inside the cas-server
folder in the `/etc/cas/services` directory. The propertie which is outstanding for us is the `serviceId`, this URL is the entry point for client
application into the server.

Then, we have to copy this file in our PC root directory `etc/cas/services` therefore we create the `services` folder.

The file where is all the magic is the `cas.properties` one, we can find it in the /etc/cas/config directory, here we specified the server name, where
we have thekeystore, its password and where or who are the users that have permissions to log in our application. We will go to that with the database
section.
So, in our PC root directory /etc/cas/ we must create the `config` folder and locate this cas.properties file and also the `log4j2.xml` that is in the
same folder.
Do not worry if you change them because when running with the `./gradlew clean copyCasConfiguration build run` command it will refresh those files.

For deploying the app under HTTPS protocol, in our `application.properties` file we have added the location of thekeystore we generated previously.

### Cas Client
> Directory: /tutorials/security-modules/cas/cas-secured-app

The configuration we have made to register this application to the CAS Server is mainly in the `CasSecuredApplication.java` class. If we look into it,
we can notice that there are several methods with the **@Bean** annotation, well this is for the login and logout authentication. Its **important** that 
the `ServiceProperties` method, when we set the service, the URL we pass it as a parameter must match with the URL of the `serviceId` propertie on our
JSON file in the server.

### PostgreSQL Database

In order to store the users we have two options for testing the app, the first one is the simplest, because we only have to replace in our `cas.properties`
file, all the database configuration for this line:
`cas.authn.accept.users=username::password`
That username and password will be vaid credentials to access to our application. If we want to go further we can create a database to store them.
The option I insistenly recommend is a postgres DB with a docker container. The only thing you have to do is create the file `docker-compose.yml` 
wherever you want (it can also be out of this project) and copy this:
````
version: '2'
services:
  cas-postgres:
    image: "postgres:14-alpine"
    restart: 
      always
    ports:
      - 5432:5432
    volumes:
      - "./postgres_data:/var/lib/postgresql/data"
    environment:
      - POSTGRES_DB=cas
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=123456
````

Then, in the terminal, in the directory you have this file you should run `docker-compose up` for creating the database. Once its done, we are going
to create the table users and to insert a pair of them.
Choose the IDE you prefer, or intall DBeaver database tool and execute this query:
````
CREATE TABLE users
(
    id bigint NOT NULL,
    disabled boolean,
    email character varying(40) COLLATE pg_catalog."default",
    first_name character varying(40) COLLATE pg_catalog."default",
    last_name character varying(40) COLLATE pg_catalog."default",
    expired boolean,
    password character varying(100) COLLATE pg_catalog."default",
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email)
)
WITH (
    OIDS = FALSE
);
````

That will create the table and now we insert the user:

````
INSERT INTO users(
 id, disabled, email, first_name, last_name, expired, password)
 VALUES (1, false, 'user1@test.com', 'test', 'user1', false, '$2y$12$7XQUDwK3QE7oBB0wmVpht.aT7gESI205SgWarj15Wz2Jt6OfglbQ.');
````

 The password is encoded by bcryp algorithm.
 
 Now you can login with the test user:
  1. Go to https://localhost:8900/cas.
  2. Click on login url, it should redirect you to the cas server login form.
  3.  Use ‘user1@test.com’ as username and ‘testpass’ as password, you should be redirected to the secured page.
  4. Now you can logout if wished.

  



