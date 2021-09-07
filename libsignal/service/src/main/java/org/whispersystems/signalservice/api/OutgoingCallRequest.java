package org.whispersystems.signalservice.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OutgoingCallRequest {

    @JsonProperty
    private OutgoingCallMessage message;

    public OutgoingCallRequest(String sender, int callType) {
        this.message = new OutgoingCallMessage(sender, callType);
    }

    static class  OutgoingCallMessage{
        @JsonProperty
        private OutgoingCallAps aps = new OutgoingCallAps();
        @JsonProperty
        private String sender;
        @JsonProperty
        private int callType;

        public OutgoingCallMessage(String sender, int callType) {
            this.sender = sender;
            this.callType = callType;
        }
    }


    static class OutgoingCallAps {
        @JsonProperty
        private String sound = "default";
        @JsonProperty
        private OutgoingCallAlert alert = new OutgoingCallAlert();
    }

    static class OutgoingCallAlert {
        @JsonProperty("loc-key")
        private String locKey = "APN_Message";
    }
}
