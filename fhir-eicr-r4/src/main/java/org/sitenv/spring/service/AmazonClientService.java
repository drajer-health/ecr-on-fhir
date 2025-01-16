package org.sitenv.spring.service;

import org.sitenv.spring.model.MetaData;

/**
 * Interface for Amazon S3 Client Service.
 * Provides methods to handle uploading of FHIR bundles and metadata to an S3 bucket.
 */
public interface AmazonClientService {

	/**
	 * Uploads a FHIR bundle as an XML file to an S3 bucket.
	 *
	 * @param persistenceId The unique identifier for the file to be uploaded.
	 * @param xml           The FHIR bundle content in XML format.
	 * @return A string indicating success or failure of the upload operation.
	 */
	String uploadBundleS3bucketXml(String persistenceId, String xml);

	/**
	 * Uploads a FHIR bundle as an JSON file to an S3 bucket.
	 *
	 * @param persistenceId The unique identifier for the file to be uploaded.
	 * @param jsonStr           The FHIR bundle in JSON format.
	 * @return A string indicating success or failure of the upload operation.
	 */
	String uploadBundleS3bucketJson(String persistenceId, String jsonStr);

	/**
	 * Uploads a FHIR bundle as an XML file to an S3 bucket.
	 *
	 * @param persistenceId The unique identifier for the file to be uploaded.
	 * @param xml           The FHIR bundle content in XML format.
	 * @return A string indicating success or failure of the upload operation.
	 */
	String uploadOperationOutcomeS3bucketXml(String persistenceId, String xml);

	/**
	 * Uploads a FHIR bundle as an JSON file to an S3 bucket.
	 *
	 * @param persistenceId The unique identifier for the file to be uploaded.
	 * @param jsonStr           The FHIR bundle in JSON format.
	 * @return A string indicating success or failure of the upload operation.
	 */
	String uploadOperationOutcomeS3bucketJson(String persistenceId, String jsonStr);


	/**
	 * Uploads metadata as a JSON file to an S3 bucket.
	 *
	 * @param persistenceId The unique identifier for the file to be uploaded.
	 * @param metaData      The metadata object to be serialized and uploaded.
	 * @return A string indicating success or failure of the upload operation.
	 */
	String uploadMetaDataS3bucket(String persistenceId, MetaData metaData);
}
