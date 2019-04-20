package com.example.mentos.compassapp

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import com.patloew.rxlocation.RxLocation
import com.google.android.gms.location.LocationRequest
import io.reactivex.Observable


class MainActivity : AppCompatActivity() {

    private val disposables by lazy { CompositeDisposable() }

    private val azimuthProvider: AzimuthProvider by lazy { AzimuthProvider(this) }
    private val locationProvider: LocationProvider by lazy { LocationProvider(this) }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        azimuthProvider.azimuthInDegrees
            .subscribe(::showAzimuth)
            .apply { disposables.add(this) }


        locationProvider.currentLocation
            .subscribe(::showLocation)
            .apply { disposables.add(this) }
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    private fun showAzimuth(azimuth: Int) {
        Log.d("LOL", "azimuth $azimuth")
        textView.text = (azimuth).toString()
    }

    private fun showLocation(location: Location) {
        Log.d("LOL", "location $location")
        tvLocation.text = "lat ${location.latitude} lng ${location.longitude}"
    }
}

