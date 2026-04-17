# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-8 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Runtime stage ----
FROM tomcat:9.0-jdk8-temurin

# Remove apps padrão do Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copia o WAR como ROOT para servir na raiz (/)
COPY --from=build /build/target/tucc-monolito.war /usr/local/tomcat/webapps/ROOT.war

# Configuração padrão da conexão Oracle (sobrescrita por env vars no docker-compose)
ENV ORACLE_HOST=oracle-xe
ENV ORACLE_PORT=1521
ENV ORACLE_DB=XEPDB1
ENV ORACLE_USER=tucc
ENV ORACLE_PASSWORD=tucc

EXPOSE 8080

CMD ["catalina.sh", "run"]
