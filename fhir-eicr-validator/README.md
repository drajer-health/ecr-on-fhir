# ecr-on-fhir
Prerequisites:
1.	Java 8
2.	Apache Tomcat 7 or 8
3.	Maven 3.3.x
4.	GIT

Clone the Repository
Clone the respository using the below command in command prompt

```git clone https://github.com/drajer-health/ecr-on-fhir.git```

Create Build:

Build FHIR ValidatorService:
Navigate to  fhir-eicr-validator service directory `/ecr-on-fhir/fhir-eicr-validator/ ` and run Maven build to build application war file.

```
$ mvn clean install
```

This will generate a war file under target/fhirvalidator.war. Copy this to your tomcat webapp directory for deployment.

Start Tomcat Service 
If the tomcat is started successfully then you should be able to access below endpoints
ENDPOINTS:

```
1.http://localhost:<tomcatport>/fhirvalidator/r4/resource/validate

Request Method: POST
Request Headers:

2.	Content-Type: application/json

Request Body: <Bundle Resource>
```

The above requests will provide the response in Bundle Resource after validating the input Bundle
