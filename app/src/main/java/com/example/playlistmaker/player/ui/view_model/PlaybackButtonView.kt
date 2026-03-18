package com.example.playlistmaker.player.ui.view_model

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.playlistmaker.R


class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var playDrawable: Drawable? = null
    private var pauseDrawable: Drawable? = null

    private var isPlaying = false

    private var iconRect = Rect()

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            defStyleAttr,
            0
        ).apply {
            try {
                val playIconId = getResourceId(R.styleable.PlaybackButtonView_playIcon, 0)
                val pauseIconId = getResourceId(R.styleable.PlaybackButtonView_pauseIcon, 0)

                if (playIconId != 0) {
                    playDrawable = ContextCompat.getDrawable(context, playIconId)
                }

                if (pauseIconId != 0) {
                    pauseDrawable = ContextCompat.getDrawable(context, pauseIconId)
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        iconRect.set(0, 0, w, h)

        playDrawable?.setBounds(iconRect)
        pauseDrawable?.setBounds(iconRect)
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = if (isPlaying) pauseDrawable else playDrawable

        drawable?.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (event.x in 0f..width.toFloat() && event.y in 0f..height.toFloat()) {
                    performClick()
                    toggleState()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setPlayingState(playing: Boolean) {
        if (isPlaying != playing) {
            isPlaying = playing
            invalidate()
        }
    }

    fun isPlaying(): Boolean = isPlaying

    private fun toggleState() {
        isPlaying = !isPlaying
        invalidate()
    }
}