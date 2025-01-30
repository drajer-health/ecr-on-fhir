# fhir-router
Prerequisites:
1.	Java 17
2.	Apache Tomcat 7 or 8
3.	Maven 3.3.x
4.	GIT

Clone the Repository
Clone the respository using the below command in command prompt

```git clone https://github.com/drajer-health/ecr-on-fhir.git```

Installation Instructions
Build FHIR Router:
Navigate to  fhir-router service directory `/ecr-on-fhir/fhir-router/ ` and run Maven build to build application war file.


Add following values to environment variables 

```
spring.cloud.aws.credentials.access-key=

spring.cloud.aws.credentials.secret-key=
```

Update following with the SQS names

```

cloud.aws.ehx.queue=

cloud.aws.pha.queue=
```
Update following with the secret names

```

aws.secret.ehx.name=

```

NOTE: make sure ECS container has access to SQS and Secret

```
$ mvn clean install
```

Add the war to ecs container 


