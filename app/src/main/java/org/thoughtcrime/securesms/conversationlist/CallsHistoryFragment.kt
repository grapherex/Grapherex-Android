package org.thoughtcrime.securesms.conversationlist

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import kotlinx.android.synthetic.main.calls_fragment.*
import org.signal.glide.Log
import org.thoughtcrime.securesms.*
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.sms.MessageSender

@SuppressLint("LogNotSignal")
class CallsHistoryFragment : BaseFragment() {

  private lateinit var viewModel: CallsHistoryViewModel
  private var tracker: SelectionTracker<String>? = null

  private val callsAdapter by lazy {
    CallsHistoryAdapter(
      callClickListener = {
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
    initializeViewModel()
    viewModel.getCallsHistory()
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

    initSelection()
    initSelectionTracker()

    fabInvite.setOnClickListener {
      MainNavigator.get(requireActivity()).goToCallsContacts()
    }

    srlRefresh.setOnRefreshListener {
      srlRefresh.isRefreshing = false
      viewModel.getCallsHistory()
    }
  }

  private fun initSelection() {
    selectionToolbar.apply {
      inflateMenu(R.menu.calls_selection)
      setOnMenuItemClickListener {
        when (it.itemId) {
          R.id.action_delete -> {
            AlertDialog.Builder(requireContext())
              .setTitle(R.string.CallsHistoryFragment_delete_calls_title)
              .setMessage(R.string.CallsHistoryFragment_delete_calls_message)
              .setPositiveButton(R.string.delete) { _: DialogInterface?, _: Int ->
                val items = tracker?.selection?.map { id -> id.toString() }
                  ?.toMutableList() ?: mutableListOf()
                viewModel.deleteCallsById(items)
                callsAdapter.deleteItems(items)
                tracker?.clearSelection()
              }
              .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                tracker?.clearSelection()
              }
              .show()
            true
          }
          else -> false
        }
      }

      // use navigationIcon as close icon
      navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_close_14)
      setNavigationOnClickListener {
        tracker?.clearSelection()
      }
    }
  }

  private fun initSelectionTracker() {
    val tracker = this.tracker ?: run {
      SelectionTracker.Builder(
        "mySelection",
        rvCalls,
        CallItemKeyProvider(callsAdapter),
        CallsItemDetailsLookup(rvCalls),
        StorageStrategy.createStringStorage()
      ).withSelectionPredicate(
        SelectionPredicates.createSelectAnything()
      ).build().also {
        this.tracker = it
      }
    }

    callsAdapter.tracker = tracker

    tracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {

      override fun onSelectionChanged() {
        Log.e("FUCK", "onSelectionChanged tracker.hasSelection() = ${tracker.hasSelection()}")
        selectionToolbar.visible(tracker.hasSelection())
        fragmentToolbar?.visible(!tracker.hasSelection())
        selectionToolbar.title = tracker.selection.size().toString()
        callsAdapter.showSelection(tracker.hasSelection())
      }
    })
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
    private val callsHistoryRepository: CallsHistoryRepository
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

  private class CallItemKeyProvider(private val adapter: CallsHistoryAdapter) :
    ItemKeyProvider<String>(
      SCOPE_MAPPED
    ) {
    override fun getKey(position: Int): String {
      return adapter.items[position].callId.toString()
    }

    override fun getPosition(key: String): Int {
      return adapter.items.indexOfFirst { it.callId.toString() == key }
    }
  }
}
