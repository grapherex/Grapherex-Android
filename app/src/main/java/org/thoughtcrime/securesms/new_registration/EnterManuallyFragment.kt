package org.thoughtcrime.securesms.new_registration

import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.enter_manually_fragment.*
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.R

class EnterManuallyFragment : BaseFragment() {

  override val layoutRes = R.layout.enter_manually_fragment

  override fun updateView() {
    btnSend.setOnClickListener {
      findNavController().navigate(EnterManuallyFragmentDirections.actionEnterPinFragment())
    }
    etCompanyName.addTextChangedListener {
      manageSendBtnState()
    }
    etId.addTextChangedListener {
      manageSendBtnState()
    }
  }

  private fun manageSendBtnState() {
    btnSend.isEnabled = etCompanyName.text?.isNotEmpty() == true && etId.text?.isNotEmpty() == true
  }
}
