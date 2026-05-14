# HHS Client Intake & Case Management System (Java 8)

A Java EE 8 web application demonstrating enterprise-grade client intake and
case management for the Utah Division of Health & Human Services.

Built with:

- **Java 8** — language baseline
- **Java EE 8** — full platform API (javax.* namespace)
- **JSF 2.3 (Mojarra)** — component-based UI layer
- **Enterprise JavaBeans (EJB 3.2)** — stateless session beans for business logic
- **JPA 2.2 + JPQL** — data persistence with named queries
- **Hibernate 5.4** — JPA provider
- **CDI 2.0** — dependency injection and session management
- **H2 (file-based)** — zero-config database for demo purposes
- **WildFly 18** — Java EE 8 compliant application server
- **WCAG 2.1 AA** — accessible markup throughout

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java (JDK) | 8 (1.8) |
| Maven | 3.6+ |
| WildFly | 18.0.1.Final |
| IntelliJ IDEA | 2020.1+ |

---

## Running the Application

### Option 1 — WildFly Maven Plugin (easiest)

```bash
mvn wildfly:run
```

The plugin downloads WildFly 18 automatically on first run (~200MB). The app will be
available at:

```
http://localhost:8080/hhs-case-management-j8/
```

### Option 2 — Deploy to existing WildFly 18 instance

1. Download WildFly 18 from https://www.wildfly.org/downloads/
2. Start WildFly:
   ```bash
   $WILDFLY_HOME/bin/standalone.sh        # macOS / Linux
   $WILDFLY_HOME\bin\standalone.bat       # Windows
   ```
3. Build and deploy:
   ```bash
   mvn clean package wildfly:deploy
   ```

---

## Importing into IntelliJ IDEA

1. Open IntelliJ IDEA
2. Choose **File → Open** and select the `hhs-case-management-j8` folder
3. IntelliJ detects the `pom.xml` automatically — click **Open as Project**
4. Wait for Maven to download dependencies (first run only)
5. To run from IntelliJ:
   - Go to **Run → Edit Configurations**
   - Add a new **Maven** run configuration
   - Set the working directory to the project root
   - Set the command to: `wildfly:run`
   - Click **Run**

---

## Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Administrator | admin@hhs.gov | Admin1234! |
| Caseworker | caseworker@hhs.gov | Case1234! |

---

## Key Differences from the Jakarta EE 10 Version

| Area | Jakarta EE 10 (Java 17) | Java EE 8 (Java 8) |
|------|------------------------|---------------------|
| API namespace | `jakarta.*` | `javax.*` |
| Java version | 17 | 8 |
| Application server | WildFly 31 | WildFly 18 |
| JSF version | Mojarra 4.0 (JSF 4.0) | Mojarra 2.3 (JSF 2.3) |
| JPA version | JPA 3.1 | JPA 2.2 |
| EJB version | EJB 4.0 | EJB 3.2 |
| CDI version | CDI 4.0 | CDI 2.0 |
| Hibernate version | 6.4 | 5.4 |
| Maven plugin | wildfly-maven-plugin 4.x | wildfly-maven-plugin 2.x |
| XHTML namespaces | `jakarta.faces.*` | `http://xmlns.jcp.org/jsf/*` |
| String.isBlank() | Yes (Java 11+) | Replaced with trim().isEmpty() |

---

## Running Tests

```bash
# Run all tests — no WildFly needed
mvn test

# Run with verbose output
mvn test -e
```

---

## Security Notes

This is a **demonstration application**. Before production deployment:

1. Replace H2 with Oracle or PostgreSQL
2. Replace SHA-256 password hashing with bcrypt (jBCrypt library)
3. Enable HTTPS and set `<secure>true</secure>` on session cookies
4. Change `javax.faces.PROJECT_STAGE` to `Production` in `web.xml`
5. Remove the demo credentials notice from `login.xhtml`
6. Integrate with your agency's LDAP / Active Directory
