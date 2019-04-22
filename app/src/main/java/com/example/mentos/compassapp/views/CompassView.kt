package com.example.mentos.compassapp.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.mentos.compassapp.R
import kotlinx.android.synthetic.main.view_compass.view.*

class CompassView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var azimuth: Int = 0
    private var bearing: Int? = null

    init {
        inflate(context, R.layout.view_compass, this)
    }

    fun setAzimuth(azimuth: Int) {
        this.azimuth = azimuth
        imageCompass.rotation = -azimuth.toFloat()
        imageArrow.rotation = imageCompass.rotation + (bearing ?: 0)
        tvAzimuth.text = context.getString(R.string.compass_view_azimuth, azimuth)
    }

    fun setBearing(bearing: Int) {
        this.bearing = bearing
        imageArrow.isVisible = true
        imageArrow.rotation = imageCompass.rotation + bearing
        tvBearing.text = context.getString(R.string.compass_view_bearing, bearing)
    }
}