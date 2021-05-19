# ecr-on-fhir
Prerequisites:
1.	Java 8
2.	Apache Tomcat 7 or 8
3.	PostgresSql Database 10.x
4.	Maven 3.3.x
5.	GIT

Clone the Repository
Clone the respository using the below command in command prompt
`git clone https://github.com/drajer-health/ecr-on-fhir.git`

Installation Instructions
Postgres Configuration:
Load Schema and data into database
Create the database by running the below command in command prompt. Enter the password for postgres database when prompted. Default password will be ‘postgres’
`$ createdb -h localhost -p 5432 -U postgres <database_name>`

Create Build:
Build ECR on FHIR Backend Service:
Change the database configurations in the file application.properties located under src/main/resources 
jdbc.url=jdbc:postgresql://localhost:5432/<database_name>
jdbc.username=<username>
jdbc.password=<password>

update the ‘validator.ednpoint’ property in application.properties file to point the fhir-validator running in your local or any external system
validator.endpoint=http://localhost:8080/fhir-validator/r4/resource/validate

Then navigate to  fhir-eicr-r4 service directory `/ecr-on-fhir/fhir-eicr-r4 / ` and run Maven build to build application war file.
$ mvn clean install


This will generate a war file under target/fhir-eicr-r4.war. Copy this to your tomcat webapp directory for deployment.
Build FHIR ValidatorService:
Navigate to  fhir-eicr-validator service directory `/ecr-on-fhir/fhir-eicr-validator/ ` and run Maven build to build application war file.
$ mvn clean install

This will generate a war file under target/fhir-eicr-validator.war. Copy this to your tomcat webapp directory for deployment.

Start Tomcat Service 
If the tomcat is started successfully then you should be able to access below endpoints
ENDPOINTS:

1.http://localhost:<tomcatport>/<Service_name>/fhir/$process-message

Request Method: POST
Request Headers:

1.	Content-Type: application/json

Request Body: <Bundle Resource>

The above requests will provide the response in Bundle Resource after validating the input Bundle

![image](https://user-images.githubusercontent.com/4450817/118827705-9df4dd00-b88a-11eb-93f0-d8bc541801ff.png)
