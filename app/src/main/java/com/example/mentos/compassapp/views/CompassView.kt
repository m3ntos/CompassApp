package com.example.mentos.compassapp.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.mentos.compassapp.R
import kotlinx.android.synthetic.main.view_compass.view.*

class CompassView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var azimuth: Float = 0f
    private var bearing: Float? = null

    init {
        inflate(context, R.layout.view_compass, this)
    }

    fun setAzimuth(azimuth: Float) {
        this.azimuth = azimuth
        imageCompass.rotation = -azimuth
        imageArrow.rotation = imageCompass.rotation + (bearing ?: 0f)
        tvAzimuth.text = context.getString(R.string.compass_view_azimuth, azimuth)
    }

    fun setBearing(bearing: Float) {
        this.bearing = bearing
        imageArrow.isVisible = true
        imageArrow.rotation = imageCompass.rotation + bearing
        tvBearing.text = context.getString(R.string.compass_view_bearing, bearing)
    }
}