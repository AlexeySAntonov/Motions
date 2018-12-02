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
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.ClassLoaderCreator
import android.os.Parcelable.Creator
import android.support.annotation.DrawableRes
import android.support.annotation.Px
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.design.R.attr
import android.support.design.animation.AnimationUtils
import android.support.design.behavior.HideBottomViewOnScrollBehavior
import android.support.design.shape.MaterialShapeDrawable
import android.support.design.shape.RoundedCornerTreatment
import android.support.design.shape.ShapePathModel
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.CoordinatorLayout.AttachedBehavior
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.AbsSavedState
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
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
    const val BAR_ICONS_MODE_FULL = 0
    const val BAR_ICONS_MODE_NONE = 1

    var LEFT_IMAGE_ID = View.generateViewId()
    var RIGHT_IMAGE_ID = View.generateViewId()
    var CENTER_IMAGE_ID = View.generateViewId()
  }

  private val fabOffset: Int
  private var fabAlignmentMode: Int = FAB_ALIGNMENT_MODE_CENTER
  private var hideOnScroll: Boolean = false
  private var fabAttached: Boolean = false

  private val materialShapeDrawable: MaterialShapeDrawable
  private val topEdgeTreatment: BottomAppBarTopEdgeTreatment

  private var attachAnimator: Animator? = null
  private var modeAnimator: Animator? = null
  private var visibilityAnimator: Animator? = null
  private var fabAnimationListener: AnimatorListenerAdapter

  private var leftImage: ImageView? = null
  private var rightImage: ImageView? = null
  private var centerImage: ImageView? = null

  private var leftImageRes: Int? = null
  private var rightImageRes: Int? = null
  private var centerImageRes: Int? = null

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

  private val isAnimationRunning: Boolean
    get() =
      attachAnimator != null && attachAnimator!!.isRunning
          || modeAnimator != null && modeAnimator!!.isRunning
          || visibilityAnimator != null && visibilityAnimator!!.isRunning

  init {
    fabAttached = true
    fabAnimationListener = object : AnimatorListenerAdapter() {
      override fun onAnimationStart(animation: Animator) {
        maybeAnimateAttachChange(fabAttached)
      }
    }

    val a = context.theme.obtainStyledAttributes(attrs, R.styleable.NiceBar, 0, 0)
    val backgroundTint = getColorStateList(context, a, R.styleable.NiceBar_backgroundTint)
    leftImageRes = a.getResourceId(R.styleable.NiceBar_leftImageRes, 0)
    rightImageRes = a.getResourceId(R.styleable.NiceBar_rightImageRes, 0)
    centerImageRes = a.getResourceId(R.styleable.NiceBar_centerImageRes, 0)
    val fabCradleMargin = a.getDimensionPixelOffset(R.styleable.NiceBar_fabCradleMargin, 0).toFloat()
    val fabCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_fabCradleRoundedCornerRadius, 0).toFloat()
    val fabVerticalOffset = a.getDimensionPixelOffset(R.styleable.NiceBar_fabCradleVerticalOffset, 0).toFloat()
    val barCornersRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barCornersRadius, 0).toFloat()
    val barTopLeftCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barTopLeftRadius, 0).toFloat()
    val barTopRightCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barTopRightRadius, 0).toFloat()
    val barBottomLeftCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barBottomLeftRadius, 0).toFloat()
    val barBottomRightCornerRadius = a.getDimensionPixelOffset(R.styleable.NiceBar_barBottomRightRadius, 0).toFloat()
    val barIconsSideMargin = a.getDimensionPixelOffset(R.styleable.NiceBar_barIconsSideMargin, 0)
    val barIconsMode = a.getInt(R.styleable.NiceBar_barIconsMode, 0)
    fabAlignmentMode = a.getInt(R.styleable.NiceBar_fabAlignmentMode, 0)
    hideOnScroll = a.getBoolean(R.styleable.NiceBar_hideOnScroll, false)
    a.recycle()
    fabOffset = barIconsSideMargin
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
    if (barIconsMode == BAR_ICONS_MODE_FULL) setupIcons(barIconsSideMargin, leftImageRes, rightImageRes, centerImageRes)
  }

  private fun setupIcons(sideMargin: Int, @DrawableRes leftImageRes: Int?, @DrawableRes rightImageRes: Int?, @DrawableRes centerImageRes: Int?) {
    val set = ConstraintSet()
    leftImage = ImageView(context).apply {
      id = LEFT_IMAGE_ID
      if (leftImageRes != 0 && leftImageRes != null) setImageResource(leftImageRes)
      setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))
    }
    rightImage = ImageView(context).apply {
      id = RIGHT_IMAGE_ID
      if (rightImageRes != 0 && rightImageRes != null) setImageResource(rightImageRes)
      setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))
    }
    centerImage = ImageView(context).apply {
      id = CENTER_IMAGE_ID
      if (centerImageRes != 0 && centerImageRes != null) setImageResource(centerImageRes)
      setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))
    }
    addView(leftImage)
    addView(rightImage)
    addView(centerImage)
    set.clone(this)
    set.connect(LEFT_IMAGE_ID, ConstraintSet.TOP, id, ConstraintSet.TOP)
    set.connect(LEFT_IMAGE_ID, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
    set.connect(LEFT_IMAGE_ID, ConstraintSet.START, id, ConstraintSet.START, sideMargin)
    set.connect(RIGHT_IMAGE_ID, ConstraintSet.TOP, id, ConstraintSet.TOP)
    set.connect(RIGHT_IMAGE_ID, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
    set.connect(RIGHT_IMAGE_ID, ConstraintSet.END, id, ConstraintSet.END, sideMargin)
    set.connect(CENTER_IMAGE_ID, ConstraintSet.TOP, id, ConstraintSet.TOP)
    set.connect(CENTER_IMAGE_ID, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
    set.connect(CENTER_IMAGE_ID, ConstraintSet.START, id, ConstraintSet.START)
    set.connect(CENTER_IMAGE_ID, ConstraintSet.END, id, ConstraintSet.END)
    set.applyTo(this)

    val typedValue = TypedValue()
    context.theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, typedValue, true)
    leftImage?.apply {
      setOnClickListener {
        maybeAnimateIconVisibilityChange(this)
        setFabAlignmentMode(FAB_ALIGNMENT_MODE_START)
        setFabDrawable(this.drawable)
      }
      setBackgroundResource(typedValue.resourceId)
    }
    rightImage?.apply {
      setOnClickListener {
        maybeAnimateIconVisibilityChange(this)
        setFabAlignmentMode(FAB_ALIGNMENT_MODE_END)
        setFabDrawable(this.drawable)
      }
      setBackgroundResource(typedValue.resourceId)
    }
    centerImage?.apply {
      setOnClickListener {
        maybeAnimateIconVisibilityChange(this)
        setFabAlignmentMode(FAB_ALIGNMENT_MODE_CENTER)
        setFabDrawable(this.drawable)
      }
      setBackgroundResource(typedValue.resourceId)
    }

    hideImageViewAccordingMode(fabAlignmentMode)
  }

  private fun setFabDrawable(drawable: Drawable) {
    val fab = findDependentFab()
    fab?.setImageDrawable(drawable)
    if (drawable is Animatable) drawable.start()
  }

  private fun hideImageViewAccordingMode(fabAlignmentMode: Int) {
    leftImage?.alpha = 1f
    rightImage?.alpha = 1f
    centerImage?.alpha = 1f
    when (fabAlignmentMode) {
      FAB_ALIGNMENT_MODE_START  -> leftImage?.alpha = 0f
      FAB_ALIGNMENT_MODE_END    -> rightImage?.alpha = 0f
      FAB_ALIGNMENT_MODE_CENTER -> centerImage?.alpha = 0f
    }
  }

  fun setImageRes(fabAlignmentMode: Int, @DrawableRes imageRes: Int) {
    when (fabAlignmentMode) {
      FAB_ALIGNMENT_MODE_START  -> leftImage?.setImageResource(imageRes)
      FAB_ALIGNMENT_MODE_END    -> rightImage?.setImageResource(imageRes)
      FAB_ALIGNMENT_MODE_CENTER -> centerImage?.setImageResource(imageRes)
    }
  }

  private fun dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
  }

  fun getFabAlignmentMode(): Int {
    return fabAlignmentMode
  }

  private fun setFabAlignmentMode(@FabAlignmentMode fabAlignmentMode: Int) {
    maybeAnimateModeChange(fabAlignmentMode)
    this.fabAlignmentMode = fabAlignmentMode
  }

  private fun setFabDiameter(@Px diameter: Int) {
    if (diameter.toFloat() != topEdgeTreatment.fabDiameter) {
      topEdgeTreatment.fabDiameter = diameter.toFloat()
      materialShapeDrawable.invalidateSelf()
    }
  }

  private fun maybeAnimateIconVisibilityChange(view: ImageView) {
    if (ViewCompat.isLaidOut(view)) {
      visibilityAnimator?.cancel()

      val animators = ArrayList<Animator>()
      createImageViewFadeAnimation(animators, view)
      val set = AnimatorSet()
      set.playTogether(animators)
      visibilityAnimator = set
      visibilityAnimator?.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          visibilityAnimator = null
        }
      })
      visibilityAnimator?.start()
    }
  }

  private fun maybeAnimateModeChange(@FabAlignmentMode targetMode: Int) {
    if (fabAlignmentMode != targetMode && ViewCompat.isLaidOut(this)) {
      modeAnimator?.cancel()

      val animators = ArrayList<Animator>()
      createCradleTranslationAnimation(targetMode, animators)
      createFabTranslationXAnimation(targetMode, animators)
      val set = AnimatorSet()
      set.playTogether(animators)
      modeAnimator = set
      modeAnimator?.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          modeAnimator = null
        }
      })
      modeAnimator?.start()
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

  private fun createImageViewFadeAnimation(animators: MutableList<Animator>, view: ImageView?) {
    val inView = when (fabAlignmentMode) {
      FAB_ALIGNMENT_MODE_START -> leftImage
      FAB_ALIGNMENT_MODE_END   -> rightImage
      else                     -> centerImage
    }
    val animatorOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
    val animatorIn = ObjectAnimator.ofFloat(inView, "alpha", 0f, 1f)
    animatorOut.duration = ANIMATION_DURATION
    animatorIn.duration = ANIMATION_DURATION
    animators.add(animatorOut)
    animators.add(animatorIn)

    val drawable = inView?.drawable
    if (drawable is Animatable) drawable.start()
  }

  private fun maybeAnimateAttachChange(targetAttached: Boolean) {
    if (ViewCompat.isLaidOut(this)) {
      attachAnimator?.cancel()

      val animators = ArrayList<Animator>()
      createCradleShapeAnimation(targetAttached && isVisibleFab, animators)
      createFabTranslationYAnimation(targetAttached, animators)
      val set = AnimatorSet()
      set.playTogether(animators)
      attachAnimator = set
      attachAnimator?.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          this@NiceBar.attachAnimator = null
        }
      })
      attachAnimator?.start()
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
    val translation = measuredWidth / 2 - fabOffset
    return when (fabAlignmentMode) {
      FAB_ALIGNMENT_MODE_START -> -(translation - (leftImage?.let { it.width / 2 } ?: 0)) * (if (isRtl) -1 else 1)
      FAB_ALIGNMENT_MODE_END   -> (translation - (rightImage?.let { it.width / 2 } ?: 0)) * (if (isRtl) -1 else 1)
      else                     -> 0
    }
  }

  private fun cancelAnimations() {
    attachAnimator?.cancel()
    modeAnimator?.cancel()
    visibilityAnimator?.cancel()
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
      hideImageViewAccordingMode(fabAlignmentMode)
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