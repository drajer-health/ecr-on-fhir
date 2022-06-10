package com.drajer.EicrFhirvalidator.component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.model.FhirPublication;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.utilities.npm.ToolsVersion;
import org.hl7.fhir.utilities.json.JSONUtil;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.ValidationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class Validator {

	private final Logger logger = LoggerFactory.getLogger(Validator.class);

	private static ValidationEngine hl7Validator;

	public Validator() {
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
		final String txServer = getTxServerUrl();
		final String txLog = null;
		final String fhirVersion = "4.0.1";
		logger.info("initializing hl7Validator inside  Validator");
		hl7Validator = new ValidationEngine(definitions);
		hl7Validator.setIgs(igFiles);
		hl7Validator.connectToTSServer(txServer, txLog, FhirPublication.fromCode(fhirVersion));
		hl7Validator.setDoNative(false);
		hl7Validator.setAnyExtensionsAllowed(true);
		hl7Validator.prepare();
	}

	public OperationOutcome validate(byte[] resource, String profile) throws Exception {
		ArrayList<String> patientProfiles = new ArrayList<>(Arrays.asList(profile.split(",")));
		try {
			Manager.FhirFormat fmt = FormatUtilities.determineFormat(resource);

			return hl7Validator.validate(null, resource, fmt, patientProfiles, null);


		} catch (Exception e) {
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


	/**
	 * Load a profile into the validator.
	 * 
	 * @param profile the profile to be loaded
	 */
	public void loadProfile(Resource profile) {
		SimpleWorkerContext context = hl7Validator.getContext();
		context.cacheResource(profile);
	}

	/**
	 * Load a profile from a file.
	 *
	 * @param src the file path
	 * @throws IOException if the file fails to load
	 */
	public void loadProfileFromFile(String src) throws IOException {
		byte[] resource = loadResourceFromFile(src);
		Manager.FhirFormat fmt = FormatUtilities.determineFormat(resource);
		Resource profile = FormatUtilities.makeParser(fmt).parse(resource);
		loadProfile(profile);
	}

	private byte[] loadResourceFromFile(String src) throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		URL file = classLoader.getResource(src);
		return IOUtils.toByteArray(file);
	}

}