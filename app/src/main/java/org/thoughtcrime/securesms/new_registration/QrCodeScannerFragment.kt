package org.thoughtcrime.securesms.new_registration

import android.Manifest
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.qr_code_scanner_fragment.*
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.permissions.Permissions

class QrCodeScannerFragment : BaseFragment() {

  private var codeScanner: CodeScanner? = null
  override val layoutRes = R.layout.qr_code_scanner_fragment

  override fun updateView() {
    codeScanner = CodeScanner(requireContext(), csvScanner)

    // Parameters (default values)
    codeScanner?.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
    codeScanner?.formats = listOf(BarcodeFormat.QR_CODE) // list of type BarcodeFormat,

    codeScanner?.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
    codeScanner?.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
    codeScanner?.isAutoFocusEnabled = true // Whether to enable auto focus or not
    codeScanner?.isFlashEnabled = false // Whether to enable flash or not

    // Callbacks
    codeScanner?.decodeCallback = DecodeCallback {
      requireActivity().runOnUiThread {
        Toast.makeText(requireContext(), "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
        findNavController().navigate(QrCodeScannerFragmentDirections.actionEnterPinFragment())
      }
    }
    codeScanner?.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
      requireActivity().runOnUiThread {
        Toast.makeText(
          requireContext(), "Camera initialization error: ${it.message}",
          Toast.LENGTH_LONG
        ).show()
      }
    }

    requestPermissions()
  }

  override fun onResume() {
    super.onResume()
    requestPermissions()
  }

  override fun onPause() {
    codeScanner?.releaseResources()
    super.onPause()
  }

  private fun requestPermissions() {
    Permissions.with(this)
      .request(Manifest.permission.CAMERA)
      .ifNecessary()
      .onAllGranted { codeScanner?.startPreview() }
      .onAnyDenied {
        Toast.makeText(
          requireContext(),
          R.string.AvatarSelectionBottomSheetDialogFragment__taking_a_photo_requires_the_camera_permission,
          Toast.LENGTH_SHORT
        )
          .show()
      }
      .execute()
  }
}
