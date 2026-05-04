## HMS Backend (Spring Boot + JWT + MySQL)

### Requirements
- Java 17+
- MySQL 8+
- Maven (`mvn`) installed

### Configure
Edit `src/main/resources/application.properties`:
- `spring.datasource.username`
- `spring.datasource.password`
- `app.jwt.secret`

### Run

```bash
mvn spring-boot:run
```

Backend starts at `http://localhost:8080`.

### Auth
- JWT is stored in **HttpOnly cookie** named `HMS_TOKEN`.
- Frontend must call APIs with `withCredentials: true`.

