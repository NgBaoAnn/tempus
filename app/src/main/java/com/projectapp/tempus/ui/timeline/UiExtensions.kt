package com.projectapp.tempus.ui.timeline

import android.annotation.SuppressLint
import android.content.Context
import com.projectapp.tempus.R


@SuppressLint("DiscouragedApi")
fun Context.getIconResId(label: String?): Int {
    
    if (label.isNullOrEmpty()) return R.drawable.ic_launcher_foreground

    
    val resourceName = "${label.trim().lowercase()}"

    
    val resId = this.resources.getIdentifier(
        resourceName,
        "drawable",
        this.packageName
    )

    
    return if (resId != 0) resId else R.drawable.ic_launcher_foreground
}