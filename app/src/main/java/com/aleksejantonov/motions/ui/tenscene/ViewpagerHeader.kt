package com.aleksejantonov.motions.ui.tenscene

import android.content.Context
import android.support.constraint.motion.MotionLayout
import android.support.v4.view.ViewPager
import android.util.AttributeSet

class ViewpagerHeader @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr), ViewPager.OnPageChangeListener {

  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    val numPages = 3
    progress = (position + positionOffset) / (numPages - 1)
  }

  override fun onPageScrollStateChanged(p0: Int) = Unit
  override fun onPageSelected(p0: Int) = Unit
}