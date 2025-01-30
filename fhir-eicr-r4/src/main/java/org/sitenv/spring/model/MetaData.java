package org.sitenv.spring.model;

import java.util.List;
public class MetaData {

    private String messageId;
    private String senderUrl;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderUrl() {
        return senderUrl;
    }

    public void setSenderUrl(String senderUrl) {
        this.senderUrl = senderUrl;
    }


    @Override
    public String toString() {
        return "MetaData [RawFHIR-T-PH-ECR.messageId=" + messageId + ", RawFHIR-T-PH-ECR.senderUrl=" + senderUrl
                + "]";
    }

}