package org.thoughtcrime.securesms.new_registration

import kotlinx.android.synthetic.main.enter_pin_fragment.*
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.registration.VerificationCodeView
import org.thoughtcrime.securesms.components.registration.VerificationPinKeyboard

class EnterPinFragment : BaseFragment() {

  override val layoutRes = R.layout.enter_pin_fragment

  override fun updateView() {
    connectKeyboard(code, keyboard)

  }

  private fun connectKeyboard(
    verificationCodeView: VerificationCodeView,
    keyboard: VerificationPinKeyboard
  ) {
    keyboard.setOnKeyPressListener { key: Int ->
      if (key >= 0) {
        verificationCodeView.append(key)
      } else {
        verificationCodeView.delete()
      }
    }
  }
}
