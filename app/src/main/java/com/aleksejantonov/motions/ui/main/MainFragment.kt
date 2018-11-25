package com.aleksejantonov.motions.ui.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.motions.R
import com.aleksejantonov.motions.util.navigation.Screens.FIRST_SCENE
import com.aleksejantonov.motions.util.navigation.Screens.SECOND_SCENE
import kotlinx.android.synthetic.main.fragment_main.firstScene
import kotlinx.android.synthetic.main.fragment_main.secondScene

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
  }

  override fun onDestroyView() {
    presenter.detachView()
    super.onDestroyView()
  }
}