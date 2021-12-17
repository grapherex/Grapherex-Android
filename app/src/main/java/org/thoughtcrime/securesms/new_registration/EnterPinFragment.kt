package org.thoughtcrime.securesms.new_registration

import android.os.CountDownTimer
import android.view.View
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.enter_pin_fragment.*
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.registration.VerificationCodeView
import org.thoughtcrime.securesms.components.registration.VerificationPinKeyboard

class EnterPinFragment : BaseFragment() {

  override val layoutRes = R.layout.enter_pin_fragment

  private var timer: CountDownTimer? = null

  override fun updateView() {
    btnResendCode.setOnClickListener {
      launchTimer()
    }
    launchTimer()
    connectKeyboard(code, keyboard)
    code.setOnCompleteListener {
      findNavController().navigate(EnterPinFragmentDirections.actionEnterVariantToDownloadFragment())
    }
  }

  private fun launchTimer() {
    btnResendCode.isEnabled = false
    tvTimer.visibility = View.VISIBLE
    timer = object : CountDownTimer(60000, 1000) {

      override fun onTick(millisUntilFinished: Long) {
        tvTimer.text =
          getString(R.string.EnterCodeFragment_send_again_after, millisUntilFinished / 1000)
      }

      override fun onFinish() {
        tvTimer.visibility = View.GONE
        btnResendCode.isEnabled = true
      }

    }
    timer?.start()
  }

  override fun onDestroyView() {
    timer?.cancel()
    super.onDestroyView()
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
