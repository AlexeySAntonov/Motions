package com.aleksejantonov.motions.ui.bottombar

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksejantonov.motions.R
import com.aleksejantonov.motions.ui.bottombar.NiceBar.Companion.FAB_ALIGNMENT_MODE_END
import kotlinx.android.synthetic.main.fragment_bottom_bar.bar
import kotlinx.android.synthetic.main.fragment_bottom_bar.fab

class BottomBarFragment : Fragment() {
  companion object {
    @JvmStatic
    fun newInstance() = BottomBarFragment()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_bottom_bar, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

  }
}