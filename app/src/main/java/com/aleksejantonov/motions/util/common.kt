package com.aleksejantonov.motions.util

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.support.annotation.StyleableRes
import android.support.v7.content.res.AppCompatResources

fun getColorStateList(context: Context, attributes: TypedArray, @StyleableRes index: Int): ColorStateList? {
  if (attributes.hasValue(index)) {
    val resourceId = attributes.getResourceId(index, 0)
    if (resourceId != 0) {
      val value = AppCompatResources.getColorStateList(context, resourceId)
      if (value != null) {
        return value
      }
    }
  }
  return attributes.getColorStateList(index)
}