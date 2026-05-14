# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — Build the WAR with Maven + Java 8
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-8 AS build

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 — Run on WildFly 18 (Java EE 8 / Java 8)
# ─────────────────────────────────────────────────────────────────────────────
FROM jboss/wildfly:18.0.1.Final

# Copy built WAR
COPY --from=build /build/target/hhs-case-management-j8.war \
     /opt/jboss/wildfly/standalone/deployments/

# Copy datasource CLI script
COPY --from=build /build/src/main/scripts/datasource.cli /tmp/datasource.cli

USER root
RUN mkdir -p /opt/jboss/hhsdb_j8 && chown jboss:jboss /opt/jboss/hhsdb_j8
USER jboss

# Point H2 at the container path
RUN sed -i 's|~/hhsdb_j8/hhsdb_j8|/opt/jboss/hhsdb_j8/hhsdb_j8|g' /tmp/datasource.cli

CMD ["/bin/bash", "-c", \
     "/opt/jboss/wildfly/bin/standalone.sh \
        -b 0.0.0.0 \
        -bmanagement 0.0.0.0 \
        -c standalone-full.xml \
        --read-only-server-config=false & \
      sleep 20 && \
      /opt/jboss/wildfly/bin/jboss-cli.sh \
        --connect \
        --file=/tmp/datasource.cli && \
      wait"]
