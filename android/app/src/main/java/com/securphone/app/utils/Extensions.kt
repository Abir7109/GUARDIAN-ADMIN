package com.securphone.app.utils

import android.view.View
import android.view.animation.AnimationUtils
import com.securphone.app.R

fun View.shake() {
    val anim = AnimationUtils.loadAnimation(context, R.anim.shake)
    this.startAnimation(anim)
}

fun View.fadeIn() {
    this.visibility = View.VISIBLE
    val anim = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    this.startAnimation(anim)
}
