package dora.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import dora.widget.expandablelayout.R

class DoraExpandableLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val header: LinearLayout
    private val titleView: TextView
    private val arrow: ImageView
    private val contentContainer: FrameLayout

    private var isExpanded = false
    private var animDuration = 300L
    private var arrowDownRes = R.drawable.dview_chevron_down
    private var arrowUpRes = R.drawable.dview_chevron_up

    var onToggle: ((expanded: Boolean) -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.dview_expandable_layout, this, true)
        header = findViewById(R.id.collapsible_header)
        titleView = findViewById(R.id.expandable_title)
        arrow = findViewById(R.id.expandable_arrow)
        contentContainer = findViewById(R.id.expandable_content)

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.DoraExpandableLayout)
            titleView.text = a.getString(R.styleable.DoraExpandableLayout_dview_el_titleText) ?: ""
            isExpanded = a.getBoolean(R.styleable.DoraExpandableLayout_dview_el_startExpanded, false)
            animDuration = a.getInt(R.styleable.DoraExpandableLayout_dview_el_animationDuration, 300).toLong()
            arrowDownRes = a.getResourceId(
                R.styleable.DoraExpandableLayout_dview_el_arrowDown,
                R.drawable.dview_chevron_down
            )
            arrowUpRes = a.getResourceId(
                R.styleable.DoraExpandableLayout_dview_el_arrowUp,
                R.drawable.dview_chevron_up
            )
            a.recycle()
        }

        arrow.setImageResource(if (isExpanded) arrowUpRes else arrowDownRes)
        contentContainer.isVisible = isExpanded

        header.setOnClickListener { toggle() }
    }

    fun setTitle(text: CharSequence) { titleView.text = text }

    fun setContent(view: View) {
        contentContainer.removeAllViews()
        contentContainer.addView(view)
        contentContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    fun toggle() {
        if (isExpanded) collapse() else expand()
    }

    fun expand() {
        if (isExpanded) return
        contentContainer.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = contentContainer.measuredHeight
        contentContainer.layoutParams.height = 0
        contentContainer.visibility = View.VISIBLE

        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.duration = animDuration
        animator.addUpdateListener { valueAnimator ->
            val h = valueAnimator.animatedValue as Int
            contentContainer.layoutParams.height = h
            contentContainer.requestLayout()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                arrow.setImageResource(arrowUpRes)
            }
        })
        animator.start()
        isExpanded = true
        onToggle?.invoke(true)
    }

    fun collapse() {
        if (!isExpanded) return
        val initialHeight = contentContainer.height
        val animator = ValueAnimator.ofInt(initialHeight, 0)
        animator.duration = animDuration
        animator.addUpdateListener { valueAnimator ->
            val h = valueAnimator.animatedValue as Int
            contentContainer.layoutParams.height = h
            contentContainer.requestLayout()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                contentContainer.visibility = View.GONE
                arrow.setImageResource(arrowDownRes)
            }
        })
        animator.start()
        isExpanded = false
        onToggle?.invoke(false)
    }
}
