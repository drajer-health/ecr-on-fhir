package com.drajer.EicrFhirvalidator.component;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.model.FhirPublication;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.IgLoader;
import org.hl7.fhir.validation.ValidationEngine;
import org.hl7.fhir.validation.instance.InstanceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
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
	 * @throws Exception If the validator cannot be created
	 */
	public Validator() throws Exception {

	}


	/**
	 * Creates the HL7 Validator to which can then be used for validation.
	 *
	 * @param igFiles The igFile the validator is loaded with.
	 * @throws Exception If the validator cannot be created
	 */
	public Validator(List<ImplementationGuide> igFiles) throws Exception {
		final String fhirSpecVersion = "4.0";
		final String definitions = VersionUtilities.packageForVersion(fhirSpecVersion) + "#"
				+ VersionUtilities.getCurrentVersion(fhirSpecVersion);
		final String txServer = null; //getTxServerUrl();
		final String txLog = null;
		final String fhirVersion = "4.0.1";
		logger.info("initializing hl7Validator inside  Validator");
		validator = new ValidationEngine(definitions);
		validator.setIgs(igFiles);

		validator.connectToTSServer(txServer, txLog, FhirPublication.fromCode(fhirVersion));
		validator.setDoNative(true);
		validator.setAnyExtensionsAllowed(false);
		validator.prepare();
	}


	public OperationOutcome validate(byte[] resource, String profile) throws Exception {
		ArrayList<String> profiles = new ArrayList<>(Arrays.asList(profile.split(",")));
		try {
			Manager.FhirFormat fmt = FormatUtilities.determineFormat(resource);
			List<ValidationMessage> messages = new ArrayList<>();
			return validator.validate(resource, fmt, profiles, messages);

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("error while validating resource", e);
		}
		return null;
	}

	private String getTxServerUrl() {
		if (disableTxValidation()) {
			return null;
		}

		if (System.getenv("TX_SERVER_URL") != null) {
			return System.getenv("TX_SERVER_URL");
		} else {
			return "http://tx.fhir.org";
		}
	}

	private boolean disableTxValidation() {
		return System.getenv("DISABLE_TX") != null;
	}



}