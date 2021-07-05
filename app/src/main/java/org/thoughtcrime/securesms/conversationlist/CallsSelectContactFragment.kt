package org.thoughtcrime.securesms.conversationlist

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.calls_select_contact_fragment.*
import org.thoughtcrime.securesms.ApplicationContext
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.WebRtcCallActivity
import org.thoughtcrime.securesms.contacts.ContactRepository
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.sms.MessageSender

class CallsSelectContactFragment : BaseFragment() {

  private lateinit var viewModel: CallsSelectContactViewModel

  private val cursorContactAdapter: CursorContactAdapter by lazy {
    CursorContactAdapter(callClickListener = {
      requestPermissionsForCall(it)
    })
  }

  override fun initToolbar() {
    fragmentToolbar = toolbar as Toolbar
  }

  override val toolbarTitleRes: Int = R.string.SelectCallContactFragment_screen_title

  override val layoutRes = R.layout.calls_select_contact_fragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  private fun setupViewModel() {
    val repository = ContactRepository(requireContext())
    val factory =
      ViewModelProviderFactory(ApplicationContext.getInstance(requireContext()), repository)
    viewModel = ViewModelProvider(this, factory).get(CallsSelectContactViewModel::class.java)
    receiveCursor()
    receiveLoadingState()
  }

  private fun receiveCursor() {
    viewModel.cursorData.observe(this, { cursor ->
      cursorContactAdapter.changeCursor(cursor)
    })
  }

  private fun receiveLoadingState() {
    viewModel.loading.observe(this, { loading ->
      pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
    })
  }

  private fun requestPermissionsForCall(recipient: Recipient) {
    Permissions.with(requireActivity())
      .request(Manifest.permission.RECORD_AUDIO)
      .ifNecessary()
      .withRationaleDialog(
        getString(
          R.string.ConversationActivity__to_call_s_signal_needs_access_to_your_microphone,
          recipient.getDisplayName(requireActivity())
        ),
        R.drawable.ic_mic_solid_24
      )
      .withPermanentDenialDialog(
        getString(
          R.string.ConversationActivity__to_call_s_signal_needs_access_to_your_microphone,
          recipient.getDisplayName(requireActivity())
        )
      )
      .onAllGranted {
        ApplicationDependencies.getSignalCallManager().startOutgoingAudioCall(recipient)
        MessageSender.onMessageSent()
        val activityIntent = Intent(requireActivity(), WebRtcCallActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(activityIntent)
      }
      .execute()
  }

  override fun updateView() {
    rvCallsContacts.apply {
      adapter = cursorContactAdapter
    }
    setupViewModel()
  }

  class ViewModelProviderFactory(
    val app: Application,
    val contactRepository: ContactRepository
  ) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(CallsSelectContactViewModel::class.java)) {
        return CallsSelectContactViewModel(app, contactRepository) as T
      }
      throw IllegalArgumentException("Unknown class name")
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.contacts_search, menu)

    val searchViewItem = menu.findItem(R.id.menu_contact_search)
    val searchView = searchViewItem.actionView as SearchView
    val queryListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String): Boolean {
        viewModel.getContacts(query)
        return true
      }

      override fun onQueryTextChange(query: String): Boolean {
        viewModel.getContacts(query)
        return true
      }
    }

    searchViewItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
      override fun onMenuItemActionExpand(item: MenuItem): Boolean {
        searchView.setOnQueryTextListener(queryListener)
        return true
      }

      override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
        searchView.setOnQueryTextListener(null)
        return true
      }
    })
  }
}
