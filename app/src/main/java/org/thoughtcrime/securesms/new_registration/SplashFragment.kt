package org.thoughtcrime.securesms.new_registration

import android.content.Intent
import android.net.Uri
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.splash_fragment.*
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.R


class SplashFragment : BaseFragment() {

  override val layoutRes = R.layout.splash_fragment

  override fun updateView() {
    btnSignUp.setOnClickListener {
      findNavController().navigate(SplashFragmentDirections.actionEnterVariantFragment())
    }
    ivPolicy.setOnClickListener {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")))
    }
  }
}
