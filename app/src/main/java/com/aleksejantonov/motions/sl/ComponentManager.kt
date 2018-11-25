package com.aleksejantonov.motions.sl

import android.content.Context
import com.aleksejantonov.motions.sl.component.AppComponent

class ComponentManager(private val context: Context) {
  private val appComponent by lazy { AppComponent(context) }

  fun appComponent() = appComponent
}