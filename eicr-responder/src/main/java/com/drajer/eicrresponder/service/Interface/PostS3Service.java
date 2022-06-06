package com.drajer.eicrresponder.service.Interface;

import com.drajer.eicrresponder.model.ResponderRequest;

public interface PostS3Service {
	String[] postToS3( ResponderRequest responderRequest,String folderName);
}
