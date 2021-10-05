package org.thoughtcrime.securesms.conversation;


import org.thoughtcrime.securesms.database.model.MessageRecord;

public class IdentityMismatchFailureEvent {
    private MessageRecord messageRecord;

    public IdentityMismatchFailureEvent(MessageRecord messageRecord) {
        this.messageRecord = messageRecord;
    }

    public MessageRecord getMessageRecord() {
        return messageRecord;
    }
}
