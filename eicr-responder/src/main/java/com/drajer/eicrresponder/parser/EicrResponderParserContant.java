package com.drajer.eicrresponder.parser;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class EicrResponderParserContant {

	private static final EicrResponderParserContant constants = new EicrResponderParserContant();

	public static final String RR_XML = "RR_FHIR.xml";
	public static final String EICR_FHIR_XML = "EICR_FHIR.xml";
	public static final String RR_JSON = "RR_FHIR.json";
	public static final String EICR_FHIR_JSON = "EICR_FHIR.json";
	public static final String META_DATA_JSON = "MetaData.json";
	public static final String JUD_PARTICIPANT_ROLE = "code";
	public static final String JUD_ADDR = "address";
	public static final String JUD_STATE = "state";
	public static final String META_DATA_FILE = "metadatafile";
	public static final String JURD_ORGANIZATION = "organization";
	public static final String JURD_SYSTEM_CODE = "2.16.840.1.114222.4.5.232";
	public static final String JURD_CODE_RR7 = "RR7";
	public static final String JURD_CODE_RR8 = "RR8";
	public static final String ACCESS_TOKEN ="access_token";

	private EicrResponderParserContant() {
	}

	public EicrResponderParserContant getInstance() {
		return constants;
	}

	NamespaceContext ctx = new NamespaceContext() {
		public String getNamespaceURI(String prefix) {
			if (prefix.contentEquals("hl7")) {
				return "urn:hl7-org:v3";
			} else if (prefix.contentEquals("hl7:sdtc")) {
				return "urn:hl7-org:v3:sdtc";
			} else
				return null;
		}

		@SuppressWarnings({ "rawtypes" })
		public Iterator getPrefixes(String val) {
			return null;
		}

		public String getPrefix(String uri) {
			return null;
		}
	};
}
