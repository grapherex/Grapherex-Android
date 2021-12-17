package org.thoughtcrime.securesms.new_registration

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.dialog_restore_account.view.*
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
    btnRestoreAccount.setOnClickListener {
      showRestoreDialog()
    }
  }

  @SuppressLint("AlertDialogBuilderUsage")
  private fun showRestoreDialog() {
    val builder = AlertDialog.Builder(requireContext())
    val view = layoutInflater.inflate(R.layout.dialog_restore_account, null)
    builder.setView(view)
    builder.setCancelable(true)
    val dialog = builder.create()
    view.tvBasicRestore.setOnClickListener(clickEvent(dialog))
    view.tvBackupRestore.setOnClickListener(clickEvent(dialog))
    dialog.show()
  }

  private fun clickEvent(dialog: AlertDialog): View.OnClickListener = View.OnClickListener {
    findNavController().navigate(SplashFragmentDirections.actionEnterVariantFragment().apply {
      actionType = RegistrationActionType.RESTORE_FROM_BACKUP.type
    })
    dialog.dismiss()
  }
}
