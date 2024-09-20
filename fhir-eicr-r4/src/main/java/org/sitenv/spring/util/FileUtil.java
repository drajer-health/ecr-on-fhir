package org.sitenv.spring.util;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.sitenv.spring.service.AmazonClientServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(AmazonClientServiceImpl.class);

	
	public static String writeFileLocalJson(String fileContent, String keyPrefix) throws Exception {
	    try (ByteArrayInputStream is = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
	         FileOutputStream fos = new FileOutputStream( keyPrefix)) {

	        // Write to local file
	        IOUtils.copy(is, fos);
	        System.out.println("File written to /tmp/" + keyPrefix);

	    } catch (Exception e) {
	    	logger.error("Error Message:    {}", e.getMessage());
          
            return "Fail to upload Service Exception Error Message: " + e.getMessage();
	        
	    }
		return "succes";
	}
	
	public static boolean isDirectoryExists(String directoryPath) {
        Path path = Paths.get(directoryPath);

        // Check if the path exists and is a directory
        return Files.exists(path) && Files.isDirectory(path);
    }
}
