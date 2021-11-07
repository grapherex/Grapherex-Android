package org.thoughtcrime.securesms.conversationlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_call.view.*
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.conversationlist.model.CallHistoryItem
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.AvatarUtil
import org.thoughtcrime.securesms.visible

@SuppressLint("NotifyDataSetChanged")
class CallsHistoryAdapter(private val callClickListener: (Recipient) -> Unit) :
  RecyclerView.Adapter<CallsHistoryAdapter.CallsHolder>() {

  val items = mutableListOf<CallHistoryItem>()
  var tracker: SelectionTracker<String>? = null
  private var hasSelection: Boolean = false

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallsHolder {
    return CallsHolder(
      LayoutInflater
        .from(parent.context)
        .inflate(R.layout.item_call, parent, false)
    )
  }

  override fun onBindViewHolder(holder: CallsHolder, position: Int) {
    tracker?.let {
      holder.bind(items[position], it.isSelected(items[position].callId.toString()))
    }
  }

  fun setData(data: List<CallHistoryItem>) {
    items.clear()
    items.addAll(data)
    notifyDataSetChanged()
  }

  fun deleteItems(ids: List<String>) {
    ids.forEach {
      findItemByCallId(it)?.let { callItem ->
        items.remove(callItem)
      }
    }
    notifyDataSetChanged()
  }

  fun showSelection(hasSelection:Boolean) {
    if (this.hasSelection != hasSelection){
      this.hasSelection = hasSelection
      notifyDataSetChanged()
    }
  }

  private fun findItemByCallId(callId: String): CallHistoryItem? =
    items.find { it.callId == callId.toInt() }

  override fun getItemCount(): Int = items.size

  fun clear() {
    items.clear()
    notifyDataSetChanged()
  }

  inner class CallsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: CallHistoryItem, isActivated: Boolean = false) {

      itemView.tvDate.text = item.callDate

      itemView.tvName.text = item.recipientName
      val recipient = Recipient.live(item.recipientId)

      AvatarUtil.loadIconIntoImageView(recipient.get(), itemView.ivAvatarPhoto)

      itemView.ivCall.setOnClickListener {
        callClickListener(recipient.get())
      }

      itemView.cbSelect.visible(hasSelection)
      itemView.cbSelect.isChecked = isActivated

      itemView.ivCallsType.rotationY = if (item.callTypeRotationNeed) 180f else 0f
      itemView.ivCallsType.setBackgroundResource(item.callTypeRes)
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
      object : ItemDetailsLookup.ItemDetails<String>() {
        override fun getPosition(): Int = layoutPosition
        override fun getSelectionKey(): String = items[adapterPosition].callId.toString()
      }
  }
}