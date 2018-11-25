package com.aleksejantonov.motions.util.navigation

import android.support.v4.app.Fragment
import com.aleksejantonov.motions.R
import com.aleksejantonov.motions.ui.fifthscene.FifthSceneFragment
import com.aleksejantonov.motions.ui.firstscene.FirstSceneFragment
import com.aleksejantonov.motions.ui.fourthscene.FourthSceneFragment
import com.aleksejantonov.motions.ui.main.MainActivity
import com.aleksejantonov.motions.ui.main.MainFragment
import com.aleksejantonov.motions.ui.secondscene.SecondSceneFragment
import com.aleksejantonov.motions.ui.sevenscene.SevenSceneFragment
import com.aleksejantonov.motions.ui.sixscene.SixSceneFragment
import com.aleksejantonov.motions.ui.thirdscene.ThirdSceneFragment
import com.aleksejantonov.motions.util.navigation.MainNavigator.Commands.FORWARD
import com.aleksejantonov.motions.util.navigation.MainNavigator.Commands.REPLACE
import com.aleksejantonov.motions.util.navigation.Screens.FIFTH_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.FIRST_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.FOURTH_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.MAIN
import com.aleksejantonov.motions.util.navigation.Screens.SECOND_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.SEVEN_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.SIX_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.THIRD_SCENE

class MainNavigator(activity: MainActivity) {
  private val fragmentManager by lazy { activity.supportFragmentManager }

  fun openMain() {
    replace(MAIN)
  }

  fun replace(screen: Screens) {
    applyCommand(screen, REPLACE)
  }

  fun forward(screen: Screens) {
    applyCommand(screen, FORWARD)
  }

  fun back() {
    fragmentManager.popBackStackImmediate()
  }

  private fun applyCommand(screen: Screens, command: Commands, animate: Boolean = true) {
    fragmentManager
        .beginTransaction()
        .apply { if (animate) setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right) }
        .replace(R.id.fragmentContainer, getFragment(screen))
        .apply { if (command == FORWARD) addToBackStack(null) }
        .commitAllowingStateLoss()
  }

  private fun getFragment(screen: Screens): Fragment {
    return when (screen) {
      MAIN         -> MainFragment.newInstance()
      FIRST_SCENE  -> FirstSceneFragment.newInstance()
      SECOND_SCENE -> SecondSceneFragment.newInstance()
      THIRD_SCENE  -> ThirdSceneFragment.newInstance()
      FOURTH_SCENE -> FourthSceneFragment.newInstance()
      FIFTH_SCENE  -> FifthSceneFragment.newInstance()
      SIX_SCENE    -> SixSceneFragment.newInstance()
      SEVEN_SCENE  -> SevenSceneFragment.newInstance()
    }
  }

  enum class Commands {
    FORWARD,
    BACK,
    REPLACE
  }
}