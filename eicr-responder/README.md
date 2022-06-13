# eicr-responder
Prerequisites:
1.	Java 8
2.	Apache Tomcat 7 or 8
3.	PostgresSql Database 10.x
4.	Maven 3.3.x
5.	GIT

Clone the Repository
Clone the respository using the below command in command prompt

```git clone https://github.com/drajer-health/ecr-on-fhir.git```

Installation Instructions
Postgres Configuration:
Load Schema and data into database
Create the database by running the below command in command prompt. Enter the password for postgres database when prompted. Default password will be ‘postgres’

```$ createdb -h <host> -p 5432 -U postgres <database_name>```

Create Build:
Build ecr responder Backend Service:
Change the follweiong configurations in the file application.properties located under src/main/resources 

```
phaui.endpoint=http://<tomcathost>:<tomcatport>/api/
responder.endpoint=http://<tomcathost>:<tomcatport>/eicrresponder/api/
responder.storetos3=no

# AWS s3 Settings
s3.accessKeyId=<access key>
s3.secretKey=<access key>
s3.bucketName=<s3 bucket name>
s3.region=<reagion>
```

```
Mac/linux OS
export DB_HOST="jdbc:postgresql://<dbhost>"
export DB_PORT="<port>"
export DB_USERNAME="<username>"
export DB_PASSWORD="<password>"

```

```
Windows OS
set DB_HOST=jdbc:postgresql://<dbhost>
set DB_PORT=<port>"
set DB_USERNAME="<username>"
set DB_PASSWORD="<password>"

```
$ mvn clean install
```


This will generate a war file under target/eicrresponder.war Copy this to your tomcat webapp directory for deployment.

$ mvn clean install
```
This will generate a war file under target/eicrresponder.war. Copy this to your tomcat webapp directory for deployment.

Start Tomcat Service 
If the tomcat is started successfully then you should be able to access below endpoints
ENDPOINTS:

```
1.http://<tomcathost>:<tomcatport>/eicrresponder/api/phalists

Request Method: POST
Request Headers:

1.	Content-Type: application/json

