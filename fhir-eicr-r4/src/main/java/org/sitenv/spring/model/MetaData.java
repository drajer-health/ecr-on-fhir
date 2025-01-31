package org.sitenv.spring.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
public class MetaData {

    @JsonProperty("RawFHIR-T-PH-ECR.messageId")
    private String messageId;

    @JsonProperty("RawFHIR-T-PH-ECR.senderUrl")
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
