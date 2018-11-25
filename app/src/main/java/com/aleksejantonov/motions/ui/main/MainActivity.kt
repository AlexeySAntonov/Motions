package com.aleksejantonov.motions.ui.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.aleksejantonov.motions.R
import com.aleksejantonov.motions.sl.SL

class MainActivity : AppCompatActivity() {
  private val router by lazy { SL.componentManager().appComponent().appRouter }

  override fun onCreate(savedInstanceState: Bundle?) {
    router.createNavigator(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (savedInstanceState == null) router.openMain()
  }
}
