package org.thoughtcrime.securesms.new_registration

import android.os.Bundle
import org.thoughtcrime.securesms.BaseActivity
import org.thoughtcrime.securesms.R

class NewRegistrationActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_registration)
  }
}