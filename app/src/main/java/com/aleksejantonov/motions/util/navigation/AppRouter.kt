package com.aleksejantonov.motions.util.navigation

import com.aleksejantonov.motions.ui.main.MainActivity

class AppRouter {
  private lateinit var navigator: MainNavigator

  fun createNavigator(activity: MainActivity) {
    navigator = MainNavigator(activity)
  }

  fun openMain() = navigator.openMain()
  fun replace(screen: Screens) = navigator.replace(screen)
  fun forward(screen: Screens) = navigator.forward(screen)
  fun back() = navigator.back()
}