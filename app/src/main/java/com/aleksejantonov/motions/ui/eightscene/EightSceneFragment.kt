package com.aleksejantonov.motions.ui.eightscene

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.motions.R

class EightSceneFragment : Fragment() {
  companion object {
    fun newInstance() = EightSceneFragment()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_eight_scene, container, false)
  }
}