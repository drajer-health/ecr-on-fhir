# EicrResponderUI

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 12.2.6.

Clone the Repository
Clone the respository using the below command in command prompt

```git clone https://github.com/drajer-health/ecr-on-fhir.git```

## Deployment to tomcat server

```
cd eicr-responder-UI/dist

cp -R eicr-responder-UI/ /<<tomcatserver>/webapps/eicr-responder-UI/

## Test applicaiton is running 

```
http://<tomcathost>:<tomcatport>/eicr-responder-UI//#/configurepha

http://<tomcathost>:<tomcatport>/eicr-responder-UI/#/phalist

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
