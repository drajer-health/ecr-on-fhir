FROM maven:3.8-openjdk-17-slim as maven
COPY ./ /app/
WORKDIR /app/fhir-eicr-r4
RUN mvn clean install
WORKDIR /app/fhir-eicr-validator
RUN mvn clean install
WORKDIR /app/eicr-responder
RUN mvn clean install
FROM tomcat:10.1.29-jre17
RUN rm -rf $CATALINA_HOME/webapps/*
COPY --from=maven /app/fhir-eicr-*/target/*.war $CATALINA_HOME/webapps/
COPY --from=maven /app/eicr-responder*/target/*.war $CATALINA_HOME/webapps/
COPY --from=maven /app/eicr-responder-UI/dist/ $CATALINA_HOME/webapps/
