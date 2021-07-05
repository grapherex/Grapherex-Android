package org.thoughtcrime.securesms.conversationlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.signal.glide.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.conversationlist.model.CallHistoryData
import org.thoughtcrime.securesms.conversationlist.model.CallHistoryItem
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.DateUtils
import java.util.*


class CallsHistoryViewModel(
  app: Application,
  private val callsHistoryRepository: CallsHistoryRepository
) : AndroidViewModel(app) {

  val callsHistoryData: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()
  val loading: MutableLiveData<Boolean> = MutableLiveData()

  init {
    getCallsHistory()
  }

  fun getCallsHistory() = viewModelScope.launch {
    fetchCallsHistory()
  }

  fun deleteCallsHistory() = viewModelScope.launch {
    clearCallsHistory()
  }

  private fun clearCallsHistory() {
    try {
      loading.postValue(true)
      callsHistoryRepository.clearCallsHistory()
    } catch (t: Throwable) {
      Log.e("CallsHistoryViewModel", "clearCallsHistory", t)
    } finally {
      loading.postValue(false)
    }
  }

  private fun fetchCallsHistory() {
    try {
      loading.postValue(true)
      callsHistoryData.postValue(mapCallHistoryItem(callsHistoryRepository.getCallsHistory()))
    } catch (t: Throwable) {
      Log.e("CallsHistoryViewModel", "fetchCallsHistory", t)
    } finally {
      loading.postValue(false)
    }
  }

  private fun mapCallHistoryItem(callHistoryData: List<CallHistoryData>): List<CallHistoryItem> =
    mutableListOf<CallHistoryItem>().apply {
      callHistoryData.reversed().forEach {
        this.add(
          CallHistoryItem(
            RecipientId.from(it.recipientId),
            it.recipientName,
            CallType.TYPE.getResByType(it.callType),
            DateUtils.getBriefRelativeTimeSpanString(
              ApplicationDependencies.getApplication(),
              Locale.getDefault(),
              it.callTimestamp
            ),
            it.recipientAvatar
          )
        )
      }
    }


 public enum class CallType(val res: Int, val type: Int) {
    INCOMING(R.drawable.ic_calls_incoming_call, 1),
    OUTGOING(R.drawable.ic_calls_outgoing_call, 2),
    MISSED(R.drawable.ic_calls_decline_call, 3),
    CANCELED(-1, 4),
    DECLINE(-1, 5),
    TYPE(-1, -1);

    fun getResByType(type: Int): Int = values().find { it.type == type }?.res ?: -1
  }
}
