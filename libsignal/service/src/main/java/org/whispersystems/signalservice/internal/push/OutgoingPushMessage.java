/**
 * Copyright (C) 2014-2016 Open Whisper Systems
 * <p>
 * Licensed according to the LICENSE file in this repository.
 */

package org.whispersystems.signalservice.internal.push;


import com.fasterxml.jackson.annotation.JsonProperty;

public class OutgoingPushMessage {

    @JsonProperty
    private final int type;
    @JsonProperty
    private final int destinationDeviceId;
    @JsonProperty
    private final int destinationRegistrationId;
    @JsonProperty
    private final String content;
    @JsonProperty
    private final boolean online;
    @JsonProperty
    private final String pushType;

    public OutgoingPushMessage(int type,
                               int destinationDeviceId,
                               int destinationRegistrationId,
                               String content,
                               boolean online,
                               PushType pushType
    ) {
        this.type = type;
        this.destinationDeviceId = destinationDeviceId;
        this.destinationRegistrationId = destinationRegistrationId;
        this.content = content;
        this.online = online;

        this.pushType = pushType.equals(PushType.VOIP_SOUND) || pushType.equals(PushType.VOIP_VIDEO) ||pushType.equals(PushType.PUSH_GROUP) ? pushType.name() : pushType.equals(PushType.CALL)||pushType.equals(PushType.READ) ? PushType.NONE.name() : !online ? PushType.PUSH.name() : PushType.NONE.name();
    }

    @Override
    public String toString() {
        return "OutgoingPushMessage{" +
                "type=" + type +
                ", destinationDeviceId=" + destinationDeviceId +
                ", destinationRegistrationId=" + destinationRegistrationId +
                ", content='" + content + '\'' +
                ", online=" + online +
                ", pushType='" + pushType + '\'' +
                '}';
    }
}
