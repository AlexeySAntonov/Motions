package com.aleksejantonov.motions.ui.tenscene

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.motions.R

class TenSceneFragment : Fragment() {
  companion object {
    fun newInstance() = TenSceneFragment()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_ten_scene, container, false)
  }
}