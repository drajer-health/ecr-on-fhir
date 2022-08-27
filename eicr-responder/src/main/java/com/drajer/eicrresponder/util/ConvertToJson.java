package com.drajer.eicrresponder.util;

import java.io.InputStream;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;



public class ConvertToJson {
	protected FhirContext r4Context = FhirContext.forR4();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public Object getBundle(InputStream inputStream1) {
		Object bundleObj = null;
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream inputStream = classLoader.getResourceAsStream("EICR_FHIR.xml");			
			IParser jsonParser = r4Context.newJsonParser();
			jsonParser.setPrettyPrint(true);
			// Convert XML to JSON
			IParser ip = r4Context.newJsonParser(), op = r4Context.newXmlParser();
			IBaseResource ri = op.parseResource(inputStream);
			String output = ip.encodeResourceToString(ri);
//			logger.info("json string print:::::"+output.toString());
//			CommonUtil.saveFile(CommonUtil.getTempFilePath()+CommonUtil.getUUID()+".json", output);
			bundleObj = jsonParser.parseResource(Bundle.class, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bundleObj;
	}	
}
