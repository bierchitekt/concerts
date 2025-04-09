# concerts

Getting upcoming concerts in munich. Getting the artist genre using spotify api and lastfm api 

These venues are currently downloaded:
- Backstage
- Circus Krone
- Feierwerk
- Event Fabrik
- Kult9
- Muffathalle
- Olympiapark
- Strom
- Theaterfabrik
- Zenith

# Installation

You need 
java >= 21
postgresql >= 17 with pg_trgm extension

# Spotify and LastFM

add an api key for spotify and lastfm. Create an account, create a key and paste it in application.properties

# Start postgres docker container
You need a running postgres DB to run the project. To use docker, use
```
  cd setup
  docker compose up -d
```

# Run the project
```
./mvnw spring-boot:run 
```

# Build the project

Docker must be started to run the tests. The test are using testcontainers so you don't have to start postgres before.

```
./mvnw package
```

# Run the project

```
java -jar target/concerts-0.0.1-SNAPSHOT.jar
```
