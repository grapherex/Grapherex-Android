package org.thoughtcrime.securesms.conversationlist

import android.app.Application
import android.database.Cursor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.thoughtcrime.securesms.contacts.ContactRepository


class CallsSelectContactViewModel(
  app: Application,
  private val contactRepository: ContactRepository
) : AndroidViewModel(app) {

  val cursorData: MutableLiveData<Cursor> = MutableLiveData()
  val loading: MutableLiveData<Boolean> = MutableLiveData()

  init {
    getContacts("")
  }

  fun getContacts(query:String) = viewModelScope.launch {
    fetchContacts(query)
  }

  private fun fetchContacts(query:String) {
    loading.postValue(true)
    try {
      cursorData.postValue(contactRepository.querySignalContacts(query, false))
      loading.postValue(false)
    } catch (t: Throwable) {
    }
  }
}
