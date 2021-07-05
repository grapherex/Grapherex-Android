package org.thoughtcrime.securesms.conversationlist

import android.database.Cursor
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_call.view.*
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.contacts.ContactRepository
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.AvatarUtil
import org.thoughtcrime.securesms.util.CursorUtil

class CursorContactAdapter(private val callClickListener: (Recipient) -> Unit) :
  RecyclerView.Adapter<CursorContactAdapter.CursorContactHolder>() {

  private var cursor: Cursor? = null
  private var valid = false

  private val observer: DataSetObserver by lazy {
    AdapterDataSetObserver()
  }

  fun changeCursor(cursor: Cursor?) {
    val old = swapCursor(cursor)
    old?.close()
  }

  fun swapCursor(newCursor: Cursor?): Cursor? {
    if (newCursor === cursor) {
      return null
    }
    val oldCursor = cursor
    oldCursor?.unregisterDataSetObserver(observer)
    valid = newCursor?.let {
      cursor = it
      cursor?.registerDataSetObserver(observer)
      true
    } ?: false

    notifyDataSetChanged()
    return oldCursor
  }

  override fun getItemCount(): Int = if (!valid) 0 else cursor?.count ?: 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursorContactHolder =
    CursorContactHolder(
      LayoutInflater
        .from(parent.context)
        .inflate(R.layout.item_call_contact, parent, false)
    )

  override fun onBindViewHolder(holder: CursorContactHolder, position: Int) {
    holder.bind(getCursorAtPositionOrThrow(position))
  }

  inner class CursorContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(cursor: Cursor?) {
      cursor?.let {
        val rawId = CursorUtil.requireString(cursor, ContactRepository.ID_COLUMN)
        rawId?.let {
          val recipient = Recipient.live(RecipientId.from(it))
          itemView.tvName.text = recipient.get().getDisplayName(itemView.context)
          AvatarUtil.loadIconIntoImageView(recipient.get(), itemView.ivAvatarPhoto)

          itemView.setOnClickListener {
            callClickListener(recipient.get())
          }
        }
      }
    }
  }

  private fun getCursorAtPositionOrThrow(position: Int): Cursor? {
    check(valid) { "this should only be called when the cursor is valid" }
    cursor?.moveToPosition(position)
    return cursor
  }

  inner class AdapterDataSetObserver : DataSetObserver() {
    override fun onChanged() {
      super.onChanged()
      valid = true
    }

    override fun onInvalidated() {
      super.onInvalidated()
      valid = false
    }
  }
}