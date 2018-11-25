package com.aleksejantonov.motions.ui.firstscene

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.motions.R

class FirstSceneFragment : Fragment() {
  companion object {
    fun newInstance() = FirstSceneFragment()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_first_scene, container, false)
  }
}