FROM maven:3.8-openjdk-17-slim as maven
COPY ./ /app/
WORKDIR /app/fhir-eicr-r4
RUN mvn clean install -DskipTests
WORKDIR /app/fhir-eicr-validator
RUN mvn clean install -DskipTests
WORKDIR /app/fhir-router
RUN mvn clean install -DskipTests
FROM tomcat:10.1-jre17-temurin-noble@sha256:d1d6ff43d0f04cb57de203a88db1f07b335fefcf4ff86b50b9a676ad7722d8e5
RUN apt-get update && apt-get upgrade -y && apt-get install --only-upgrade -y openssl libssl3t64 curl libcurl4t64 libc6 libc-bin perl libgnutls30t64 libgssapi-krb5-2 libssh-4 libpng16-16t64 dpkg libpam-modules libpam-modules-bin gnupg2 libexpat1 libnghttp2-14 libtasn1-6 libbrotli1 zlib1g && apt-get clean && rm -rf /var/lib/apt/lists/*
RUN rm -rf $CATALINA_HOME/webapps/*
COPY --from=maven /app/fhir-eicr-*/target/*.war $CATALINA_HOME/webapps/
COPY --from=maven /app/fhir-router*/target/*.war $CATALINA_HOME/webapps/
