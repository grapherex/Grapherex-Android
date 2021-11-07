package org.thoughtcrime.securesms.conversationlist.model

data class CallHistoryData(
  val id: Int,
  val recipientId: Long,
  val recipientName: String,
  val callType: Int,
  val callTimestamp: Long,
  val recipientAvatar: String)
