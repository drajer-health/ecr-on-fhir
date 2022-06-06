package com.drajer.eicrresponder.service.Interface;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Girish Rao
 *
 */
public interface ResponderService {

	ResponseEntity<String> sendResponder(@RequestParam("files") MultipartFile[] files,String folderName);
}