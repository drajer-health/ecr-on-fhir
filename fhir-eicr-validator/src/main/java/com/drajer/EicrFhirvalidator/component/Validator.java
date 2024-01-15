package com.drajer.EicrFhirvalidator.component;

import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.model.FhirPublication;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.IgLoader;
import org.hl7.fhir.validation.ValidationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Validator {

	private final Logger logger = LoggerFactory.getLogger(Validator.class);

	private static ValidationEngine validator;

	/**
	 * Creates the HL7 Validator to which can then be used for validation.
	 *
	 * @param igFiles The igFile the validator is loaded with.
	 * @throws Exception If the validator cannot be created
	 */
	public Validator() throws Exception {
		final String fhirSpecVersion = "4.0";
		final String definitions = VersionUtilities.packageForVersion(fhirSpecVersion) + "#"
				+ VersionUtilities.getCurrentVersion(fhirSpecVersion);
		final String txServer = getTxServerUrl();
		final String txLog = null;
		final String fhirVersion = "4.0.1";

		logger.info("initializing hl7Validator inside  Validator");
		validator = new ValidationEngine(definitions);

		FhirPublication ver = FhirPublication.fromCode(fhirVersion);

		IgLoader igLoader = new IgLoader(validator.getPcm(), validator.getContext(), validator.getVersion());
		igLoader.loadIg(validator.getIgs(), validator.getBinaries(), "hl7.fhir.us.ecr#2.1.1",true);
		igLoader.loadIg(validator.getIgs(), validator.getBinaries(), "hl7.fhir.us.medmorph#0.2.0",true);
				
		/**
		igLoader.loadIg(validator.getIgs(), validator.getBinaries(), "hl7.terminology", false);
		igLoader.loadIg(validator.getIgs(), validator.getBinaries(), "hl7.fhir.us.ecr",false);
		igLoader.loadIg(validator.getIgs(), validator.getBinaries(), "hl7.fhir.us.medmorph",false);
		igLoader.loadIg(validator.getIgs(), validator.getBinaries(), "hl7.fhir.us.odh",false);
		igLoader.loadIg(validator.getIgs(), validator.getBinaries(), "hl7.fhir.us.core",false);
		igLoader.loadIg(validator.getIgs(), validator.getBinaries(), "hl7.fhir.us.vr-common-library",false);
		**/
		validator.connectToTSServer(txServer, txLog, FhirPublication.fromCode(fhirVersion));
		validator.setDoNative(false);
		validator.setAnyExtensionsAllowed(false);
		validator.prepare();
	}



	public OperationOutcome validate(byte[] resource, String profile) throws Exception {
		ArrayList<String> profiles = new ArrayList<>(Arrays.asList(profile.split(",")));
		try {
			Manager.FhirFormat fmt = FormatUtilities.determineFormat(resource);

			List<ValidationMessage> messages = new ArrayList<>();
			return validator.validate(resource, FormatUtilities.determineFormat(resource), profiles, messages);

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("error while validating resource", e);
		}
		return null;
	}

	private String getTxServerUrl() {
		if (System.getenv("TX_SERVER_URL") != null) {
			return System.getenv("TX_SERVER_URL");
		} else {
			return "http://tx.fhir.org";
		}
	}

}
