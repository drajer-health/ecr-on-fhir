# ecr-on-fhir
Prerequisites:
1.	Java 17
2.	Apache Tomcat 10 or higher
3.	PostgresSql Database 10.x
4.	Maven 3.3.x
5.	GIT

Clone the Repository
Clone the repository using the below command in command prompt

```git clone https://github.com/drajer-health/ecr-on-fhir.git```

Installation Instructions

Create Build:
Build ECR on FHIR Backend Service:
Change the database configurations in the file application.properties located under src/main/resources

```
jdbc.url=jdbc:postgresql://localhost:5432/<database_name>
jdbc.username=<username>
jdbc.password=<password>

```
Update the config to point to Key Cloak Server

```
keycloak.auth.server=http://<<Key Cloak Server:port>>

```
Update the configuration to point to AWS S3 configurations

```
# AWS s3 Settings
s3.accessKeyId=<< S3 Access Key>>
s3.secretKey=<< Secret Key >>
s3.bucketName=<< Bucket Name >>
s3.region=<< Region >>

```

update the ‘validator.ednpoint’ property in application.properties file to point the fhirvalidator running in your local or any external system

```validator.endpoint=http://localhost:8080/fhirvalidator/r4/resource/validate```

Then navigate to  fhir-eicr-r4 service directory `/ecr-on-fhir/fhir-eicr-r4 / ` and run Maven build to build application war file.

```
$ mvn clean install
```


This will generate a war file under target/r4.war. Copy this to your tomcat webapp directory for deployment.

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
1.http://localhost:<tomcatport>/<Service_name>/fhir/$process-message

Request Method: POST
Request Headers:

1.	Content-Type: application/json

Request Body: <Bundle Resource>
```

The above requests will provide the response in Bundle Resource after validating the input Bundle
