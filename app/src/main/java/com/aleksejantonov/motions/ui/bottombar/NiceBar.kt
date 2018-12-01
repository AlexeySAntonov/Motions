package com.aleksejantonov.motions.ui.bottombar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.Paint.Style
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.ClassLoaderCreator
import android.os.Parcelable.Creator
import android.support.annotation.MenuRes
import android.support.annotation.Px
import android.support.constraint.ConstraintLayout
import android.support.design.R.attr
import android.support.design.animation.AnimationUtils
import android.support.design.behavior.HideBottomViewOnScrollBehavior
import android.support.design.shape.MaterialShapeDrawable
import android.support.design.shape.RoundedCornerTreatment
import android.support.design.shape.ShapePathModel
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.CoordinatorLayout.AttachedBehavior
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.AbsSavedState
import android.support.v4.view.ViewCompat
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.aleksejantonov.motions.R
import com.aleksejantonov.motions.util.getColorStateList
import java.util.ArrayList
import kotlin.annotation.AnnotationRetention.SOURCE

class NiceBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = attr.bottomAppBarStyle
) : ConstraintLayout(context, attrs, defStyleAttr), AttachedBehavior {
  @kotlin.annotation.Retention(SOURCE)
  annotation class FabAlignmentMode

  companion object {
    private const val ANIMATION_DURATION = 300L
    const val FAB_ALIGNMENT_MODE_CENTER = 0
    const val FAB_ALIGNMENT_MODE_END = 1
    const val FAB_ALIGNMENT_MODE_START = 2
  }

  private val fabOffsetEndMode: Int
  private val fabOffsetStartMode: Int
  private val materialShapeDrawable: MaterialShapeDrawable
  private val topEdgeTreatment: BottomAppBarTopEdgeTreatment
  private var attachAnimator: Animator? = null
  private var modeAnimator: Animator? = null
  private var menuAnimator: Animator? = null
  private var fabAlignmentMode: Int = FAB_ALIGNMENT_MODE_CENTER
  var hideOnScroll: Boolean = false
  private var fabAttached: Boolean = false
  private var fabAnimationListener: AnimatorListenerAdapter

  private var backgroundTint: ColorStateList?
    get() = materialShapeDrawable.tintList
    set(backgroundTint) = DrawableCompat.setTintList(materialShapeDrawable, backgroundTint)

  private var fabCradleMargin: Float
    get() = topEdgeTreatment.getFabCradleMargin()
    set(margin) {
      if (margin != fabCradleMargin) {
        topEdgeTreatment.setFabCradleMargin(margin)
        materialShapeDrawable.invalidateSelf()
      }
    }

  private var fabCradleRoundedCornerRadius: Float
    get() = topEdgeTreatment.getFabCradleRoundedCornerRadius()
    set(radius) {
      if (radius != fabCradleRoundedCornerRadius) {
        topEdgeTreatment.setFabCradleRoundedCornerRadius(radius)
        materialShapeDrawable.invalidateSelf()
      }
    }


  private var cradleVerticalOffset: Float
    get() = topEdgeTreatment.getCradleVerticalOffset()
    set(offset) {
      if (offset != cradleVerticalOffset) {
        topEdgeTreatment.setCradleVerticalOffset(offset)
        materialShapeDrawable.invalidateSelf()
      }
    }


  private val isVisibleFab: Boolean
    get() {
      val fab = findDependentFab()
      return fab != null && fab.isOrWillBeShown
    }

  private val fabTranslationY: Float
    get() = getFabTranslationY(fabAttached)

  private val fabTranslationX: Float
    get() = getFabTranslationX(fabAlignmentMode).toFloat()

  private val actionMenuView: ActionMenuView?
    get() {
      for (i in 0 until childCount) {
        val view = getChildAt(i)
        if (view is ActionMenuView) {
          return view
        }
      }
      return null
    }

  private val isAnimationRunning: Boolean
    get() =
      attachAnimator != null && attachAnimator!!.isRunning || menuAnimator != null && menuAnimator!!.isRunning || modeAnimator != null && modeAnimator!!.isRunning

  init {
    fabAttached = true
    fabAnimationListener = object : AnimatorListenerAdapter() {
      override fun onAnimationStart(animation: Animator) {
        maybeAnimateAttachChange(fabAttached)
        maybeAnimateMenuView(fabAlignmentMode, fabAttached)
      }
    }

    val a = context.theme.obtainStyledAttributes(attrs, R.styleable.NiceBar, 0, 0)
    val backgroundTint = getColorStateList(context, a, R.styleable.NiceBar_backgroundTint)
    val fabCradleMargin = a.getDimensionPixelOffset(R.styleable.NiceBar_fabCradleMargin, 0).toFloat()
    val fabCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_fabCradleRoundedCornerRadius, 0).toFloat()
    val fabVerticalOffset = a.getDimensionPixelOffset(R.styleable.NiceBar_fabCradleVerticalOffset, 0).toFloat()
    val barCornersRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barCornersRadius, 0).toFloat()
    val barTopLeftCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barTopLeftRadius, 0).toFloat()
    val barTopRightCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barTopRightRadius, 0).toFloat()
    val barBottomLeftCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barBottomLeftRadius, 0).toFloat()
    val barBottomRightCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barBottomRightRadius, 0).toFloat()
    fabAlignmentMode = a.getInt(R.styleable.NiceBar_fabAlignmentMode, 0)
    hideOnScroll = a.getBoolean(R.styleable.NiceBar_hideOnScroll, false)
    a.recycle()
    fabOffsetEndMode = resources.getDimensionPixelOffset(R.dimen.niceBar_fabOffsetEndMode)
    fabOffsetStartMode = resources.getDimensionPixelOffset(R.dimen.niceBar_fabOffsetStartMode)
    topEdgeTreatment = BottomAppBarTopEdgeTreatment(fabCradleMargin, fabCornerRadius, fabVerticalOffset)
    val appBarModel = ShapePathModel()
    appBarModel.topEdge = topEdgeTreatment
    if (barCornersRadius != 0f) appBarModel.setAllCorners(RoundedCornerTreatment(barCornersRadius))
    else {
      appBarModel.setCornerTreatments(
          RoundedCornerTreatment(barTopLeftCornerRadius),
          RoundedCornerTreatment(barTopRightCornerRadius),
          RoundedCornerTreatment(barBottomRightCornerRadius),
          RoundedCornerTreatment(barBottomLeftCornerRadius)
      )
    }
    materialShapeDrawable = MaterialShapeDrawable(appBarModel)
    materialShapeDrawable.isShadowEnabled = true
    materialShapeDrawable.paintStyle = Style.FILL
    DrawableCompat.setTintList(materialShapeDrawable, backgroundTint)
    ViewCompat.setBackground(this, materialShapeDrawable)
  }

  fun getFabAlignmentMode(): Int {
    return fabAlignmentMode
  }

  fun setFabAlignmentMode(@FabAlignmentMode fabAlignmentMode: Int) {
    maybeAnimateModeChange(fabAlignmentMode)
    maybeAnimateMenuView(fabAlignmentMode, fabAttached)
    this.fabAlignmentMode = fabAlignmentMode
  }

//  fun replaceMenu(@MenuRes newMenu: Int) {
//    menu.clear()
//    inflateMenu(newMenu)
//  }

  fun setFabDiameter(@Px diameter: Int) {
    if (diameter.toFloat() != topEdgeTreatment.fabDiameter) {
      topEdgeTreatment.fabDiameter = diameter.toFloat()
      materialShapeDrawable.invalidateSelf()
    }
  }

  private fun maybeAnimateModeChange(@FabAlignmentMode targetMode: Int) {
    if (fabAlignmentMode != targetMode && ViewCompat.isLaidOut(this)) {
      if (modeAnimator != null) {
        modeAnimator!!.cancel()
      }

      val animators = ArrayList<Animator>()
      createCradleTranslationAnimation(targetMode, animators)
      createFabTranslationXAnimation(targetMode, animators)
      val set = AnimatorSet()
      set.playTogether(animators)
      modeAnimator = set
      modeAnimator!!.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          this@NiceBar.modeAnimator = null
        }
      })
      modeAnimator!!.start()
    }
  }

  private fun createCradleTranslationAnimation(targetMode: Int, animators: MutableList<Animator>) {
    if (fabAttached) {
      val animator = ValueAnimator.ofFloat(topEdgeTreatment.horizontalOffset, getFabTranslationX(targetMode).toFloat())
      animator.addUpdateListener { animation ->
        this@NiceBar.topEdgeTreatment.horizontalOffset = animation.animatedValue as Float
        this@NiceBar.materialShapeDrawable.invalidateSelf()
      }
      animator.duration = ANIMATION_DURATION
      animators.add(animator)
    }
  }

  private fun findDependentFab(): FloatingActionButton? {
    if (parent !is CoordinatorLayout) {
      return null
    } else {
      val dependents = (parent as CoordinatorLayout).getDependents(this)
      val var2 = dependents.iterator()

      var v: View
      do {
        if (!var2.hasNext()) {
          return null
        }

        v = var2.next() as View
      } while (v !is FloatingActionButton)

      return v
    }
  }

  private fun createFabTranslationXAnimation(targetMode: Int, animators: MutableList<Animator>) {
    val animator = ObjectAnimator.ofFloat(findDependentFab(), "translationX", getFabTranslationX(targetMode).toFloat())
    animator.duration = ANIMATION_DURATION
    animators.add(animator)
  }

  private fun maybeAnimateMenuView(@FabAlignmentMode mode: Int, attached: Boolean) {
    var targetMode = mode
    var newFabAttached = attached
    if (ViewCompat.isLaidOut(this)) {
      if (menuAnimator != null) {
        menuAnimator!!.cancel()
      }

      val animators = ArrayList<Animator>()
      if (!isVisibleFab) {
        targetMode = 0
        newFabAttached = false
      }

      createMenuViewTranslationAnimation(targetMode, newFabAttached, animators)
      val set = AnimatorSet()
      set.playTogether(animators)
      menuAnimator = set
      menuAnimator!!.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          this@NiceBar.menuAnimator = null
        }
      })
      menuAnimator!!.start()
    }
  }

  private fun createMenuViewTranslationAnimation(targetMode: Int, targetAttached: Boolean, animators: MutableList<Animator>) {
    val actionMenuView = actionMenuView
    if (actionMenuView != null) {
      val fadeIn = ObjectAnimator.ofFloat(actionMenuView, "alpha", 1.0f)
      if (!fabAttached && (!targetAttached || !isVisibleFab) || fabAlignmentMode != 1 && targetMode != 1) {
        if (actionMenuView.alpha < 1.0f) {
          animators.add(fadeIn)
        }
      } else {
        val fadeOut = ObjectAnimator.ofFloat(actionMenuView, "alpha", 0.0f)
        fadeOut.addListener(object : AnimatorListenerAdapter() {
          var cancelled: Boolean = false

          override fun onAnimationCancel(animation: Animator) {
            cancelled = true
          }

          override fun onAnimationEnd(animation: Animator) {
            if (!cancelled) {
//              this@NiceBar.translateActionMenuView(actionMenuView, targetMode, targetAttached)
            }

          }
        })
        val set = AnimatorSet()
        set.duration = 150L
        set.playSequentially(fadeOut, fadeIn)
        animators.add(set)
      }

    }
  }

  private fun maybeAnimateAttachChange(targetAttached: Boolean) {
    if (ViewCompat.isLaidOut(this)) {
      if (attachAnimator != null) {
        attachAnimator!!.cancel()
      }

      val animators = ArrayList<Animator>()
      createCradleShapeAnimation(targetAttached && isVisibleFab, animators)
      createFabTranslationYAnimation(targetAttached, animators)
      val set = AnimatorSet()
      set.playTogether(animators)
      attachAnimator = set
      attachAnimator!!.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          this@NiceBar.attachAnimator = null
        }
      })
      attachAnimator!!.start()
    }
  }

  private fun createCradleShapeAnimation(showCradle: Boolean, animators: MutableList<Animator>) {
    if (showCradle) {
      topEdgeTreatment.horizontalOffset = fabTranslationX
    }

    val animator = ValueAnimator.ofFloat(materialShapeDrawable.interpolation, if (showCradle) 1.0f else 0.0f)
    animator.addUpdateListener { animation -> this@NiceBar.materialShapeDrawable.interpolation = animation.animatedValue as Float }
    animator.duration = ANIMATION_DURATION
    animators.add(animator)
  }

  private fun createFabTranslationYAnimation(targetAttached: Boolean, animators: MutableList<Animator>) {
    val fab = findDependentFab()
    if (fab != null) {
      val animator = ObjectAnimator.ofFloat(fab, "translationY", getFabTranslationY(targetAttached))
      animator.duration = ANIMATION_DURATION
      animators.add(animator)
    }
  }

  private fun getFabTranslationY(targetAttached: Boolean): Float {
    val fab = findDependentFab()
    if (fab == null) {
      return 0.0f
    } else {
      val fabContentRect = Rect()
      fab.getContentRect(fabContentRect)
      var fabHeight = fabContentRect.height().toFloat()
      if (fabHeight == 0.0f) {
        fabHeight = fab.measuredHeight.toFloat()
      }

      val fabBottomShadow = (fab.height - fabContentRect.bottom).toFloat()
      val fabVerticalShadowPadding = (fab.height - fabContentRect.height()).toFloat()
      val attached = -cradleVerticalOffset + fabHeight / 2.0f + fabBottomShadow
      val detached = fabVerticalShadowPadding - fab.paddingBottom.toFloat()
      return (-measuredHeight).toFloat() + if (targetAttached) attached else detached
    }
  }

  private fun getFabTranslationX(fabAlignmentMode: Int): Int {
    val isRtl = ViewCompat.getLayoutDirection(this) == 1
    val translation = (measuredWidth / 2 - fabOffsetEndMode) * (if (isRtl) -1 else 1)
    return when (fabAlignmentMode) {
      FAB_ALIGNMENT_MODE_START -> -translation
      FAB_ALIGNMENT_MODE_END   -> translation
      else                     -> 0
    }
  }

//  private fun translateActionMenuView(actionMenuView: ActionMenuView?, fabAlignmentMode: Int, fabAttached: Boolean) {
//    var toolbarLeftContentEnd = 0
//    val isRtl = ViewCompat.getLayoutDirection(this) == 1
//
//    var end: Int
//    end = 0
//    while (end < childCount) {
//      val view = getChildAt(end)
//      val isAlignedToStart = view.layoutParams is LayoutParams && (view.layoutParams as LayoutParams).gravity and 8388615 == 8388611
//      if (isAlignedToStart) {
//        toolbarLeftContentEnd = Math.max(toolbarLeftContentEnd, if (isRtl) view.left else view.right)
//      }
//      ++end
//    }
//
//    end = if (isRtl) actionMenuView!!.right else actionMenuView!!.left
//    val offset = toolbarLeftContentEnd - end
//    actionMenuView.translationX = if (fabAlignmentMode == 1 && fabAttached) offset.toFloat() else 0.0f
//  }

  private fun cancelAnimations() {
    if (attachAnimator != null) {
      attachAnimator!!.cancel()
    }

    if (menuAnimator != null) {
      menuAnimator!!.cancel()
    }

    if (modeAnimator != null) {
      modeAnimator!!.cancel()
    }

  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    super.onLayout(changed, l, t, r, b)
    cancelAnimations()
    setCutoutState()
  }

  private fun setCutoutState() {
    topEdgeTreatment.horizontalOffset = fabTranslationX
    val fab = findDependentFab()
    materialShapeDrawable.interpolation = if (fabAttached && isVisibleFab) 1.0f else 0.0f
    if (fab != null) {
      fab.translationY = fabTranslationY
      fab.translationX = fabTranslationX
    }

//    val actionMenuView = actionMenuView
//    if (actionMenuView != null) {
//      actionMenuView.alpha = 1.0f
//      if (!isVisibleFab) {
//        translateActionMenuView(actionMenuView, 0, false)
//      } else {
//        translateActionMenuView(actionMenuView, fabAlignmentMode, fabAttached)
//      }
//    }

  }

  private fun addFabAnimationListeners(fab: FloatingActionButton) {
    removeFabAnimationListeners(fab)
    fab.addOnHideAnimationListener(fabAnimationListener)
    fab.addOnShowAnimationListener(fabAnimationListener)
  }

  private fun removeFabAnimationListeners(fab: FloatingActionButton) {
    fab.removeOnHideAnimationListener(fabAnimationListener)
    fab.removeOnShowAnimationListener(fabAnimationListener)
  }

//  override fun setTitle(title: CharSequence) {}
//
//  override fun setSubtitle(subtitle: CharSequence) {}

  override fun getBehavior(): android.support.design.widget.CoordinatorLayout.Behavior<NiceBar> {
    return NiceBar.Behavior()
  }

  override fun onSaveInstanceState(): Parcelable? {
    val superState = super.onSaveInstanceState()
    val savedState = NiceBar.SavedState(superState)
    savedState.fabAlignmentMode = fabAlignmentMode
    savedState.fabAttached = fabAttached
    return savedState
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    if (state !is NiceBar.SavedState) {
      super.onRestoreInstanceState(state)
    } else {
      super.onRestoreInstanceState(state.superState)
      fabAlignmentMode = state.fabAlignmentMode
      fabAttached = state.fabAttached
    }
  }

  internal class SavedState : AbsSavedState {
    var fabAlignmentMode: Int = 0
    var fabAttached: Boolean = false

    constructor(superState: Parcelable) : super(superState)

    constructor(`in`: Parcel, loader: ClassLoader) : super(`in`, loader) {
      fabAlignmentMode = `in`.readInt()
      fabAttached = `in`.readInt() != 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
      super.writeToParcel(out, flags)
      out.writeInt(fabAlignmentMode)
      out.writeInt(if (fabAttached) 1 else 0)
    }

    companion object {
      @JvmField
      val CREATOR: Creator<NiceBar.SavedState> = object : ClassLoaderCreator<NiceBar.SavedState> {
        override fun createFromParcel(`in`: Parcel, loader: ClassLoader): NiceBar.SavedState {
          return NiceBar.SavedState(`in`, loader)
        }

        override fun createFromParcel(`in`: Parcel): NiceBar.SavedState {
          return NiceBar.SavedState(`in`, null as ClassLoader)
        }

        override fun newArray(size: Int): Array<NiceBar.SavedState?> {
          return arrayOfNulls(size)
        }
      }
    }
  }

  class Behavior : HideBottomViewOnScrollBehavior<NiceBar>() {
    private val fabContentRect = Rect()

    private fun updateFabPositionAndVisibility(fab: FloatingActionButton, child: NiceBar): Boolean {
      val fabLayoutParams = fab.layoutParams as android.support.design.widget.CoordinatorLayout.LayoutParams
      fabLayoutParams.anchorGravity = 17
      child.addFabAnimationListeners(fab)
      return true
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: NiceBar, layoutDirection: Int): Boolean {
      val fab = child.findDependentFab()
      if (fab != null) {
        updateFabPositionAndVisibility(fab, child)
        fab.getMeasuredContentRect(fabContentRect)
        child.setFabDiameter(fabContentRect.height())
      }

      if (!child.isAnimationRunning) {
        child.setCutoutState()
      }

      parent.onLayoutChild(child, layoutDirection)
      return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: NiceBar, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
      return child.hideOnScroll && super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun slideUp(child: NiceBar) {
      super.slideUp(child)
      val fab = child.findDependentFab()
      if (fab != null) {
        fab.clearAnimation()
        fab.animate().translationY(child.fabTranslationY).setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).duration = 225L
      }

    }

    override fun slideDown(child: NiceBar) {
      super.slideDown(child)
      val fab = child.findDependentFab()
      if (fab != null) {
        fab.getContentRect(fabContentRect)
        val fabShadowPadding = (fab.measuredHeight - fabContentRect.height()).toFloat()
        fab.clearAnimation()
        fab.animate().translationY((-fab.paddingBottom).toFloat() + fabShadowPadding).setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR).duration = 175L
      }

    }
  }
}