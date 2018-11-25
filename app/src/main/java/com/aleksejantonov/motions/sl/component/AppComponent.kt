package com.aleksejantonov.motions.sl.component

import android.content.Context
import com.aleksejantonov.motions.util.navigation.AppRouter

class AppComponent(private val context: Context) {
  val appRouter by lazy { AppRouter() }
}