package com.aleksejantonov.motions.ui.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.motions.R
import com.aleksejantonov.motions.util.navigation.Screens.BOTTOM_BAR
import com.aleksejantonov.motions.util.navigation.Screens.EIGHT_SCREEN
import com.aleksejantonov.motions.util.navigation.Screens.FIFTH_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.FIRST_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.FOURTH_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.NINE_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.SECOND_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.SEVEN_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.SIX_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.TEN_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.THIRD_SCENE
import kotlinx.android.synthetic.main.fragment_main.bottomBar
import kotlinx.android.synthetic.main.fragment_main.eightScene
import kotlinx.android.synthetic.main.fragment_main.fifthScene
import kotlinx.android.synthetic.main.fragment_main.firstScene
import kotlinx.android.synthetic.main.fragment_main.fourthScene
import kotlinx.android.synthetic.main.fragment_main.nineScene
import kotlinx.android.synthetic.main.fragment_main.secondScene
import kotlinx.android.synthetic.main.fragment_main.sevenScene
import kotlinx.android.synthetic.main.fragment_main.sixScene
import kotlinx.android.synthetic.main.fragment_main.tenScene
import kotlinx.android.synthetic.main.fragment_main.thirdScene

class MainFragment : Fragment(), MvpView {
  companion object {
    fun newInstance() = MainFragment()
  }

  private val presenter by lazy { MainPresenter() }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_main, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.attachView(this)
    firstScene.setOnClickListener { presenter.goTo(FIRST_SCENE) }
    secondScene.setOnClickListener { presenter.goTo(SECOND_SCENE) }
    thirdScene.setOnClickListener { presenter.goTo(THIRD_SCENE) }
    fourthScene.setOnClickListener { presenter.goTo(FOURTH_SCENE) }
    fifthScene.setOnClickListener { presenter.goTo(FIFTH_SCENE) }
    sixScene.setOnClickListener { presenter.goTo(SIX_SCENE) }
    sevenScene.setOnClickListener { presenter.goTo(SEVEN_SCENE) }
    eightScene.setOnClickListener { presenter.goTo(EIGHT_SCREEN) }
    nineScene.setOnClickListener { presenter.goTo(NINE_SCENE) }
    tenScene.setOnClickListener { presenter.goTo(TEN_SCENE) }
    bottomBar.setOnClickListener { presenter.goTo(BOTTOM_BAR) }
  }

  override fun onDestroyView() {
    presenter.detachView()
    super.onDestroyView()
  }
}