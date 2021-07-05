package org.thoughtcrime.securesms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment


abstract class BaseFragment : Fragment() {

  abstract val layoutRes: Int
  open val toolbarTitleRes: Int = R.string.empty
  var fragmentToolbar: Toolbar? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    retainInstance = true
    applyArguments()
  }

  open fun applyArguments() {}

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(layoutRes, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initToolbar()
    setupToolbar()
    updateView()
  }

  open fun initToolbar() {}

  open fun updateView() {}

  override fun onPause() {
    super.onPause()
    hideKeyboard()
  }

  override fun onResume() {
    super.onResume()
    if (getString(toolbarTitleRes).isNotEmpty()) fragmentToolbar?.title = getString(toolbarTitleRes)
  }

  private fun setupToolbar() {
    fragmentToolbar?.let {
      val currentActivity = (requireActivity() as AppCompatActivity)
      currentActivity.setSupportActionBar(it)
      currentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
      it.setNavigationOnClickListener {
        requireActivity().onBackPressed()
      }
      it.title = ""
    }
  }
}