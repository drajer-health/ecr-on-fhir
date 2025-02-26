package com.drajer.fhir.router.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drajer.fhir.router.service.FhirServiceImpl;

@RestController
public class FHIRDummyController {
@Autowired FhirServiceImpl fhirServiceImpl ;
    @PostMapping("/submitfhir")
    public ResponseEntity<String> returnSuccess(String message) {
    	String fhirEicrMessage = "";

        InputStream inputStream =getFileFromResourceAsStream("FHIR-eICR.xml");
        System.out.println("inputStream ::::::"+inputStream);
        
        try {
			fhirEicrMessage = IOUtils.toString(inputStream,StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("error while converting inputStream ::::::"+e.getMessage());
		}
    	
    	System.out.println("fhirEicrMessage before submit to fhir :"+fhirEicrMessage);
    	
    	String fhirUrl = "https://4578di0evk.execute-api.us-east-1.amazonaws.com/v1/$process-message";
    	
    	fhirUrl = "http://3.218.246.252:8082/r4/fhir/$process-message";
    	
    	fhirUrl = "http://localhost:8080/fhirsuccess/$process-message";
    	fhirServiceImpl.submitToFhir(fhirUrl,  "token test", fhirEicrMessage);
        return new ResponseEntity<>("Success :::::", HttpStatus.OK);
    }    
    
    
    private InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }
}
