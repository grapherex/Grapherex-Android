package org.thoughtcrime.securesms.conversationlist.model

import org.thoughtcrime.securesms.recipients.RecipientId

data class CallHistoryItem(
  val recipientId: RecipientId,
  val recipientName: String,
  val callTypeRes: Int,
  val callDate: String,
  val recipientAvatar: String)
