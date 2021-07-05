package org.thoughtcrime.securesms.conversationlist.model

data class CallHistoryData(
  val recipientId: Long,
  val recipientName: String,
  val callType: Int,
  val callTimestamp: Long,
  val recipientAvatar: String)
