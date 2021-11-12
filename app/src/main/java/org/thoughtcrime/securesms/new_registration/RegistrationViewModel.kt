package org.thoughtcrime.securesms.new_registration

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.conversationlist.model.CallHistoryData
import org.thoughtcrime.securesms.conversationlist.model.CallHistoryItem
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.DateUtils
import java.util.*

@SuppressLint("LogNotSignal")
class RegistrationViewModel(
  app: Application,
  private val callsHistoryRepository: RegistrationRepository
) : AndroidViewModel(app) {

  val callsHistoryData: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()
  val loading: MutableLiveData<Boolean> = MutableLiveData()

  fun getCallsHistory() = viewModelScope.launch {
    fetchCallsHistory()
  }

  fun deleteCallsHistory() = viewModelScope.launch {
    clearCallsHistory()
  }

  fun deleteCallsById(ids: MutableList<String>) = viewModelScope.launch {
    deleteCall(ids)
  }

  private fun deleteCall(ids: MutableList<String>) {
    try {
      loading.postValue(true)
      callsHistoryRepository.deleteCalls(ids)
    } catch (t: Throwable) {
      Log.e("CallsHistoryViewModel", "deleteCalls by id $ids", t)
    } finally {
      loading.postValue(false)
    }
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
            it.id,
            RecipientId.from(it.recipientId),
            it.recipientName,
            CallType.TYPE.getResByType(it.callType),
            DateUtils.getBriefRelativeTimeSpanString(
              ApplicationDependencies.getApplication(),
              Locale.getDefault(),
              it.callTimestamp
            ),
            CallType.TYPE.getRotationByType(it.callType),
            it.recipientAvatar
          )
        )
      }
    }


  enum class CallType(val res: Int, val type: Int, val rotation: Boolean) {
    INCOMING(R.drawable.ic_calls_outgoing_call, 1, true),
    OUTGOING(R.drawable.ic_calls_outgoing_call, 2, false),
    MISSED(R.drawable.ic_calls_decline_call, 3, true),
    TYPE(-1, -1, false);

    fun getResByType(type: Int): Int = values().find { it.type == type }?.res ?: -1
    fun getRotationByType(type: Int): Boolean = values().find { it.type == type }?.rotation ?: false
  }
}
