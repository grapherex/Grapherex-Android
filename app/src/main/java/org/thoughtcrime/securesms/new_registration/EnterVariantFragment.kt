package org.thoughtcrime.securesms.new_registration

import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.enter_variant_fragment.*
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.R

class EnterVariantFragment : BaseFragment() {

  override val layoutRes = R.layout.enter_variant_fragment

  private val args: EnterVariantFragmentArgs by navArgs()

  override fun updateView() {
    processArgs()
    btnScanQrCode.setOnClickListener {
      Toast.makeText(requireContext(), "ScanQrCode", Toast.LENGTH_LONG).show()
    }
  }

  private fun processArgs() {
    tvDescriptionTitle.text = "Sign up by QR code"
    tvDescriptionSubTitle.text = "Scan the QR code to connect your account"
    ivEnterManually.text = "Enter Manually"
    ivEnterManually.setOnClickListener {
      findNavController().navigate(EnterVariantFragmentDirections.actionEnterManuallyFragment())
    }
    when (args.actionType) {
      RegistrationActionType.RESTORE_FROM_BACKUP.type -> {
        tvDescriptionTitle.text = "Restore by QR code"
      }
      RegistrationActionType.RESTORE_FROM_BACKUP_TO_DOWNLOAD.type -> {
        tvDescriptionTitle.text = "Restore your backup\nby QR code"
        tvDescriptionSubTitle.text = "Scan the QR code to download\nbackup"
        ivEnterManually.text = "Upload from library"
        ivEnterManually.setOnClickListener {
          Toast.makeText(requireContext(), "Upload from library", Toast.LENGTH_LONG).show()
        }
      }
    }
  }
}
