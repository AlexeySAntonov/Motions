package com.aleksejantonov.motions

import android.app.Application
import com.aleksejantonov.motions.sl.SL

class App : Application() {

  override fun onCreate() {
    super.onCreate()
    SL.init(this)
  }
}