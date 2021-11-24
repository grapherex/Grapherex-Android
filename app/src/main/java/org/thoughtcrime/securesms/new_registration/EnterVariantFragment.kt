package org.thoughtcrime.securesms.new_registration

import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.enter_variant_fragment.*
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.R

class EnterVariantFragment : BaseFragment() {

  override val layoutRes = R.layout.enter_variant_fragment

  override fun updateView() {
    btnSignUp.setOnClickListener {
      findNavController().navigate(EnterVariantFragmentDirections.actionQrCodeScannerFragment())
    }
    ivEnterManually.setOnClickListener {
      findNavController().navigate(EnterVariantFragmentDirections.actionEnterManuallyFragment())
    }
  }
}
