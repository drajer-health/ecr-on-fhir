FROM openjdk:8-jdk-alpine

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} eicr_responder_service.jar

ENTRYPOINT ["java","-jar","/eicr_responder_service.jar"]