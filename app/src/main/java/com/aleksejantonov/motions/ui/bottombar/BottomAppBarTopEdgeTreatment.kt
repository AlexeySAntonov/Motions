package com.aleksejantonov.motions.ui.bottombar

import android.support.design.shape.EdgeTreatment
import android.support.design.shape.ShapePath

class BottomAppBarTopEdgeTreatment(
    private var fabCradleMargin: Float,
    private var fabCradleRoundedCornerRadius: Float,
    private var cradleVerticalOffset: Float
) : EdgeTreatment() {
  companion object {
    private val ARC_QUARTER = 90
    private val ARC_HALF = 180
    private val ANGLE_UP = 270
    private val ANGLE_LEFT = 180
  }

  var fabDiameter: Float = 0.toFloat()
  var horizontalOffset: Float = 0.toFloat()

  init {
    if (cradleVerticalOffset < 0.0f) {
      throw IllegalArgumentException("cradleVerticalOffset must be positive.")
    } else {
      this.horizontalOffset = 0.0f
    }
  }

  fun getFabCradleMargin() = fabCradleMargin
  fun getFabCradleRoundedCornerRadius() = fabCradleRoundedCornerRadius
  fun getCradleVerticalOffset() = cradleVerticalOffset

  fun setFabCradleMargin(fabCradleMargin: Float) {
    this.fabCradleMargin = fabCradleMargin
  }

  fun setFabCradleRoundedCornerRadius(radius: Float) {
    this.fabCradleRoundedCornerRadius = radius
  }

  fun setCradleVerticalOffset(offset: Float) {
    this.cradleVerticalOffset = offset
  }

  override fun getEdgePath(length: Float, interpolation: Float, shapePath: ShapePath) {
    if (this.fabDiameter == 0.0f) {
      shapePath.lineTo(length, 0.0f)
    } else {
      val cradleDiameter = this.fabCradleMargin * 2.0f + this.fabDiameter
      val cradleRadius = cradleDiameter / 2.0f
      val roundedCornerOffset = interpolation * this.fabCradleRoundedCornerRadius
      val middle = length / 2.0f + this.horizontalOffset
      val verticalOffset = interpolation * this.cradleVerticalOffset + (1.0f - interpolation) * cradleRadius
      val verticalOffsetRatio = verticalOffset / cradleRadius
      if (verticalOffsetRatio >= 1.0f) {
        shapePath.lineTo(length, 0.0f)
      } else {
        val distanceBetweenCenters = cradleRadius + roundedCornerOffset
        val distanceBetweenCentersSquared = distanceBetweenCenters * distanceBetweenCenters
        val distanceY = verticalOffset + roundedCornerOffset
        val distanceX = Math.sqrt((distanceBetweenCentersSquared - distanceY * distanceY).toDouble()).toFloat()
        val leftRoundedCornerCircleX = middle - distanceX
        val rightRoundedCornerCircleX = middle + distanceX
        val cornerRadiusArcLength = Math.toDegrees(Math.atan((distanceX / distanceY).toDouble())).toFloat()
        val cutoutArcOffset = 90.0f - cornerRadiusArcLength
        shapePath.lineTo(leftRoundedCornerCircleX - roundedCornerOffset, 0.0f)
        shapePath.addArc(leftRoundedCornerCircleX - roundedCornerOffset, 0.0f, leftRoundedCornerCircleX + roundedCornerOffset, roundedCornerOffset * 2.0f, 270.0f, cornerRadiusArcLength)
        shapePath.addArc(middle - cradleRadius, -cradleRadius - verticalOffset, middle + cradleRadius, cradleRadius - verticalOffset, 180.0f - cutoutArcOffset, cutoutArcOffset * 2.0f - 180.0f)
        shapePath.addArc(rightRoundedCornerCircleX - roundedCornerOffset, 0.0f, rightRoundedCornerCircleX + roundedCornerOffset, roundedCornerOffset * 2.0f, 270.0f - cornerRadiusArcLength, cornerRadiusArcLength)
        shapePath.lineTo(length, 0.0f)
      }
    }
  }
}