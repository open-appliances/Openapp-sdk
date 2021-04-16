package co.openapp.openappsdk.extension

import android.content.Context
import android.widget.Toast

fun Context.toast(msg: String?, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(this, msg ?: "null", duration).apply { show() }
}