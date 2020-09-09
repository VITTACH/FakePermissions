package com.vittach.fakepermission

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout


class ButtonBarLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var mAllowStacking = true
    private var mLastWidthSize = -1

    fun setAllowStacking(allowStacking: Boolean) {
        if (mAllowStacking != allowStacking) {
            mAllowStacking = allowStacking
            if (!mAllowStacking && orientation == VERTICAL) {
                setStacked(false)
            }
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        if (mAllowStacking) {
            if (widthSize > mLastWidthSize && isStacked()) {
                setStacked(false)
            }
            mLastWidthSize = widthSize
        }
        var needsRemeasure = false

        val initialWidthMeasureSpec: Int
        if (!isStacked() && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            initialWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST)
            needsRemeasure = true
        } else {
            initialWidthMeasureSpec = widthMeasureSpec
        }
        super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec)
        if (mAllowStacking && !isStacked()) {
            val measuredWidth = measuredWidthAndState
            val measuredWidthState = measuredWidth and View.MEASURED_STATE_MASK
            if (measuredWidthState == View.MEASURED_STATE_TOO_SMALL) {
                setStacked(true)
                needsRemeasure = true
            }
        }
        if (needsRemeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun setStacked(stacked: Boolean) {
        orientation = if (stacked) VERTICAL else HORIZONTAL
        gravity = if (stacked) Gravity.RIGHT else Gravity.BOTTOM
        val spacer = findViewById<View>(R.id.spacer)
        if (spacer != null) {
            spacer.visibility = if (stacked) View.GONE else View.INVISIBLE
        }

        val childCount = childCount
        for (i in childCount - 2 downTo 0) {
            bringChildToFront(getChildAt(i))
        }
    }

    private fun isStacked() = orientation == VERTICAL
}