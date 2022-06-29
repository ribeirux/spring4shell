FROM openjdk:11-jdk-slim as builder

WORKDIR /usr/src/app

COPY mvnw ./
COPY .mvn/wrapper .mvn/wrapper

# Copy all pom.xml files
COPY pom.xml ./pom.xml

# Resolve all project dependencies
RUN ./mvnw --no-transfer-progress dependency:go-offline

COPY . ./

RUN ./mvnw clean package -P war

FROM tomcat:9.0.60-jdk11-openjdk-slim-buster

COPY --from=builder /usr/src/app/target/demo.war /usr/local/tomcat/webapps/demo.war

WORKDIR /usr/local/tomcat/webapps/

ENV JPDA_ADDRESS="*:8000"
ENV JPDA_TRANSPORT="dt_socket"

EXPOSE 8000 8080

ENTRYPOINT ["catalina.sh", "jpda", "run"]
