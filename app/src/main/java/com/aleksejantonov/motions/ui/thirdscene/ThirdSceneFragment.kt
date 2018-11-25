package com.aleksejantonov.motions.ui.thirdscene

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.motions.R

class ThirdSceneFragment : Fragment() {
  companion object {
    fun newInstance() = ThirdSceneFragment()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_third_scene, container, false)
  }
}