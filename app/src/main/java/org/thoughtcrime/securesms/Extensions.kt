package org.thoughtcrime.securesms


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.text.SimpleDateFormat
import java.util.*

fun String.parseDateISO8601(): Date =
  SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault()).parse(this)

fun Date.formatShortDate(): String {
  val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
  return format.format(this)
}

fun Context.color(colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Context.getTintDrawable(drawableRes: Int, colorRes: Int): Drawable {
  val source = ContextCompat.getDrawable(this, drawableRes)!!.mutate()
  val wrapped = DrawableCompat.wrap(source)
  DrawableCompat.setTint(wrapped, color(colorRes))
  return wrapped
}

@SuppressLint("RestrictedApi")
fun Menu.showIcons() {
  (this as? MenuBuilder)?.setOptionalIconsVisible(true)
}

fun Context.getTintDrawable(
  drawableRes: Int,
  colorResources: IntArray,
  states: Array<IntArray>
): Drawable {
  val source = ContextCompat.getDrawable(this, drawableRes)!!.mutate()
  val wrapped = DrawableCompat.wrap(source)
  DrawableCompat.setTintList(
    wrapped,
    ColorStateList(states, colorResources.map { color(it) }.toIntArray())
  )
  return wrapped
}

fun TextView.setStartDrawable(drawable: Drawable) {
  setCompoundDrawablesRelativeWithIntrinsicBounds(
    drawable,
    null,
    null,
    null
  )
}

fun ImageView.tint(colorRes: Int) = this.setColorFilter(this.context.color(colorRes))

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
  return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun View.visible(visible: Boolean) {
  this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.isVisible(): Boolean = this.visibility == View.VISIBLE

fun TextView.showTextOrHide(str: String?) {
  this.text = str
  this.visible(!str.isNullOrBlank())
}

fun androidx.fragment.app.Fragment.tryOpenLink(
  link: String?,
  basePath: String? = "https://google.com/search?q="
) {
  if (link != null) {
    try {
      startActivity(
        Intent(
          Intent.ACTION_VIEW,
          when {
            URLUtil.isValidUrl(link) -> Uri.parse(link)
            else -> Uri.parse(basePath + link)
          }
        )
      )
    } catch (e: Exception) {
      startActivity(
        Intent(
          Intent.ACTION_VIEW,
          Uri.parse("https://google.com/search?q=$link")
        )
      )
    }
  }
}

fun androidx.fragment.app.Fragment.sendEmail(email: String?) {
  if (email != null) {
    startActivity(
      Intent.createChooser(
        Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null)),
        null
      )
    )
  }
}

fun AppCompatActivity.hideKeyboard() {
  currentFocus?.apply {
    val inputManager =
      getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
  }
}

fun androidx.fragment.app.Fragment.hideKeyboard() {
  requireActivity().currentFocus?.apply {
    val inputManager =
      requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
  }
}

fun View.setBackgroundTintByColor(@ColorInt color: Int) {
  val wrappedDrawable = DrawableCompat.wrap(background)
  DrawableCompat.setTint(wrappedDrawable.mutate(), color)
}

