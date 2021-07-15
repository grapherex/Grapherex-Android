package org.thoughtcrime.securesms.conversationlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_call.view.*
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.conversationlist.model.CallHistoryItem
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.AvatarUtil

class CallsHistoryAdapter(private val callClickListener: (Recipient) -> Unit) :
  RecyclerView.Adapter<CallsHistoryAdapter.CallsHolder>() {

  private val items = mutableListOf<CallHistoryItem>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallsHolder {
    return CallsHolder(
      LayoutInflater
        .from(parent.context)
        .inflate(R.layout.item_call, parent, false)
    )
  }

  override fun onBindViewHolder(holder: CallsHolder, position: Int) {
    holder.bind(items[position])
  }

  fun setData(data: List<CallHistoryItem>) {
    items.clear()
    items.addAll(data)
    notifyDataSetChanged()
  }

  override fun getItemCount(): Int {
    return items.size
  }

  fun clear() {
    items.clear()
    notifyDataSetChanged()
  }

  inner class CallsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: CallHistoryItem) {

      itemView.tvDate.text = item.callDate

      itemView.tvName.text = item.recipientName
      val recipient = Recipient.live(item.recipientId)

      AvatarUtil.loadIconIntoImageView(recipient.get(), itemView.ivAvatarPhoto)

      itemView.ivCall.setOnClickListener {
        callClickListener(recipient.get())
      }
      itemView.ivCallsType.rotationY = if (item.callTypeRotationNeed) 180f else 0f
      itemView.ivCallsType.setBackgroundResource(item.callTypeRes)
    }
  }
}