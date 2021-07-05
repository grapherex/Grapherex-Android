package org.thoughtcrime.securesms.conversationlist

import android.content.Context
import org.thoughtcrime.securesms.conversationlist.model.CallHistoryData
import org.thoughtcrime.securesms.database.CallsHistoryDatabase
import org.thoughtcrime.securesms.database.DatabaseFactory
import org.thoughtcrime.securesms.util.CursorUtil

class CallsHistoryRepository(
  private val context: Context
) {

  private val callsHistoryDatabase: CallsHistoryDatabase by lazy {
    DatabaseFactory.getCallsHistoryDatabase(context)
  }

  fun getCallsHistory(): List<CallHistoryData> {
    val callsHistory = mutableListOf<CallHistoryData>()
    val cursor = callsHistoryDatabase.fetchCallsHistory()
    while (cursor.moveToNext()) {
      val recipientId = CursorUtil.requireLong(cursor, CallsHistoryDatabase.RECIPIENT_ID)
      val recipientName = CursorUtil.requireString(cursor, CallsHistoryDatabase.RECIPIENT_NAME)
      val callType = CursorUtil.requireInt(cursor, CallsHistoryDatabase.CALL_TYPE)
      val callTimestamp = CursorUtil.requireLong(cursor, CallsHistoryDatabase.CALL_TIMESTAMP)
      val recipientAvatar = CursorUtil.requireString(cursor, CallsHistoryDatabase.RECIPIENT_AVATAR)

      callsHistory.add(
        CallHistoryData(
          recipientId,
          recipientName,
          callType,
          callTimestamp,
          recipientAvatar
        )
      )
    }
    return callsHistory
  }

  fun addCallToHistory(
    recipientId: Long,
    recipientName: String,
    callType: Int,
    callTimestamp: Long,
    recipientAvatar: String
  ) {

    callsHistoryDatabase.addCall(
      recipientId,
      recipientName,
      callType,
      callTimestamp,
      recipientAvatar
    )
  }

  fun clearCallsHistory() {
    callsHistoryDatabase.deleteAllCall()
  }
}