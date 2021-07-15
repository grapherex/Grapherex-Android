package org.thoughtcrime.securesms.conversationlist

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.calls_fragment.*
import org.thoughtcrime.securesms.*
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.sms.MessageSender

class CallsHistoryFragment : BaseFragment() {

  private lateinit var viewModel: CallsHistoryViewModel

  private val callsAdapter by lazy {
    CallsHistoryAdapter(callClickListener = {
      requestPermissionsForCall(it)
    })
  }

  override fun initToolbar() {
    fragmentToolbar = toolbar as Toolbar
  }

  override val toolbarTitleRes: Int = R.string.CallsFragment_screen_title

  override val layoutRes = R.layout.calls_fragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
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
    rvCalls.apply {
      adapter = callsAdapter
    }

    initializeViewModel()

    fabInvite.setOnClickListener {
      MainNavigator.get(requireActivity()).goToCallsContacts()
    }

    srlRefresh.setOnRefreshListener {
      srlRefresh.isRefreshing = false
      viewModel.getCallsHistory()
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.getCallsHistory()
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.pop_up_delete_all_calls, menu)
    menu.showIcons()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.delete_calls -> {
        callsAdapter.clear()
        viewModel.deleteCallsHistory()
      }
    }
    return true
  }

  class ViewModelProviderFactory(
    val app: Application,
    val callsHistoryRepository: CallsHistoryRepository
  ) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(CallsHistoryViewModel::class.java)) {
        return CallsHistoryViewModel(app, callsHistoryRepository) as T
      }
      throw IllegalArgumentException("Unknown class name")
    }
  }

  private fun initializeViewModel() {
    val repository = CallsHistoryRepository(requireContext())
    val factory =
      ViewModelProviderFactory(
        ApplicationContext.getInstance(
          requireContext()
        ), repository
      )
    viewModel = ViewModelProvider(this, factory).get(CallsHistoryViewModel::class.java)

    viewModel.callsHistoryData.observe(this, { list ->
      callsAdapter.setData(list)
    })
  }
}
