package com.aleksejantonov.motions.ui.fifthscene

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.motions.R

class FifthSceneFragment : Fragment() {
  companion object {
    fun newInstance() = FifthSceneFragment()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_fifth_scene, container, false)
  }
}