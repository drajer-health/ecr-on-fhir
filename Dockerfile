FROM maven:3.3-jdk-8 as maven
COPY ./ /app/
WORKDIR /app/fhir-eicr-r4
RUN mvn clean install
WORKDIR /app/fhir-eicr-validator
RUN mvn clean install
FROM tomcat:8.5.75-jre8-openjdk-slim
RUN rm -rf $CATALINA_HOME/webapps/*
COPY --from=maven /app/fhir-eicr-*/target/*.war $CATALINA_HOME/webapps/