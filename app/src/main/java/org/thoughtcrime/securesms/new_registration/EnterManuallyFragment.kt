package org.thoughtcrime.securesms.new_registration

import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.enter_manually_fragment.*
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.R


class EnterManuallyFragment : BaseFragment() {

  override val layoutRes = R.layout.enter_manually_fragment

  override fun updateView() {
    etCompanyName.addTextChangedListener {
      manageSendBtnState()
    }

    etId.addTextChangedListener {
      manageSendBtnState()
    }

    etId.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_SEND) {
        sendCredentials()
      }
      false
    }
    btnSend.setOnClickListener {
      sendCredentials()
    }
  }

  private fun manageSendBtnState() {
    btnSend.isEnabled = etCompanyName.text?.isNotEmpty() == true && etId.text?.isNotEmpty() == true
  }

  private fun sendCredentials() {
    //TODO need to implement
    Toast.makeText(requireContext(), "Action send request", Toast.LENGTH_LONG).show()
    findNavController().navigate(EnterManuallyFragmentDirections.actionEnterPinFragment())
  }
}
