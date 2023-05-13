# Customer API

API used to maintain a small set of customer data intended for marketing purposes.

## Starting the application

### Prerequisites
- Java 17
- Docker

### Start Docker containers
```
docker-compose up -d
```
### Start Service
```
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

See postman collection for available API endpoints.