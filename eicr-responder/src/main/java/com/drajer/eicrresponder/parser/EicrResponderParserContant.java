package com.drajer.eicrresponder.parser;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EicrResponderParserContant {

	private static final Logger logger = LoggerFactory.getLogger(EicrResponderParserContant.class);
	private static final EicrResponderParserContant constants = new EicrResponderParserContant();

	public static final XPathExpression EICR_PHA_TEMPLATE_EXP;
	public static final XPathExpression EICR_PARTICIPANT_EXP;
	public static final String RR_XML = "RR_FHIR.xml";
	public static final String EICR_FHIR_XML = "EICR_FHIR.xml";
	public static final String RR_JSON = "RR_FHIR.json";
	public static final String EICR_FHIR_JSON = "EICR_FHIR.json";	
	public static final String META_DATA_JSON = "metadata.json";
	public static final String JUD_PARTICIPANT_ROLE = "participantRole";
	public static final String JUD_ADDR = "addr";
	public static final String JUD_STATE = "state";
	public static final String META_DATA_FILE = "metadatafile";

	static {
		try {
			final XPath cdaXPath = XPathFactory.newInstance().newXPath();
			EICR_PHA_TEMPLATE_EXP = cdaXPath.compile("//templateId[@root='2.16.840.1.113883.10.20.15.2.4.2']/..");
			EICR_PARTICIPANT_EXP = cdaXPath
					.compile("//participant/templateId[@root='2.16.840.1.113883.10.20.15.2.3.19']");

		} catch (XPathExpressionException e) {
			logger.error("Failed to resolve CDA xPath", e);
			throw new IllegalStateException(e);
		}
	}

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
