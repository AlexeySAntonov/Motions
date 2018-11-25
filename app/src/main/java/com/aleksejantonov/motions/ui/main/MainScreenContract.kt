package com.aleksejantonov.motions.ui.main

import com.aleksejantonov.motions.util.navigation.Screens

interface MvpView

interface MvpPresenter {
  fun attachView(view: MvpView)
  fun detachView()
  fun goTo(screen: Screens)
}