package org.thoughtcrime.securesms.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.thoughtcrime.securesms.database.helpers.SQLCipherOpenHelper;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientId;
import org.thoughtcrime.securesms.util.CursorUtil;

public class CallsHistoryDatabase extends Database {

    public static final String TABLE_NAME = "calls_history";

    public static final String ID = "_id";
    public static final String RECIPIENT_ID = "recipient_id";
    public static final String RECIPIENT_NAME = "recipient_name";
    public static final String CALL_TYPE = "call_type";
    public static final String CALL_TIMESTAMP = "call_timestamp";
    public static final String RECIPIENT_AVATAR = "recipient_avatar";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY, " +
            RECIPIENT_NAME + " TEXT, " + RECIPIENT_ID + " INTEGER, " + CALL_TYPE + " INTEGER, " + CALL_TIMESTAMP + " INTEGER, "
            + RECIPIENT_AVATAR + " TEXT);";

    public CallsHistoryDatabase(Context context, SQLCipherOpenHelper databaseHelper) {
        super(context, databaseHelper);
    }

    private void fillDatabase(Context context) {
        deleteAllCall();
        Cursor cursor = DatabaseFactory.getSmsDatabase(context).getAllMessagesWithCallType();

        while (cursor.moveToNext()) {
            long recipientId = CursorUtil.requireLong(cursor, "address");
            int callType = CursorUtil.requireInt(cursor, "type");
            long timestamp = CursorUtil.requireLong(cursor, "date");

            Recipient recipient = Recipient.live(RecipientId.from(recipientId)).get();

            addCall(recipientId, recipient.getDisplayName(context), callType, timestamp, recipient.getProfileAvatar());
        }
    }

    public void addCall(long recipientId, String recipientName, int callType, long callTimestamp, String recipientAvatar) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues(5);
            values.put(RECIPIENT_ID, recipientId);
            values.put(RECIPIENT_NAME, recipientName);
            values.put(CALL_TYPE, callType);
            values.put(CALL_TIMESTAMP, callTimestamp);
            values.put(RECIPIENT_AVATAR, recipientAvatar == null ? "" : recipientAvatar);

            db.insert(TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Cursor fetchCallsHistory() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, null, null);
    }

    public void deleteAllCall() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }
}
