package com.aleksejantonov.motions.ui.main

import com.aleksejantonov.motions.sl.SL
import com.aleksejantonov.motions.util.navigation.Screens

class MainPresenter : MvpPresenter {
  private val router by lazy { SL.componentManager().appComponent().appRouter }
  private var view: MvpView? = null

  override fun attachView(view: MvpView) {
    this.view = view
  }

  override fun detachView() {
    this.view = null
  }

  override fun goTo(screen: Screens) {
    router.forward(screen)
  }
}