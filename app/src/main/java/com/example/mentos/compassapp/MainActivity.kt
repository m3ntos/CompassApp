package com.example.mentos.compassapp

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.runtimepermission.kotlin.askPermission
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val viewModel: CompassViewModel by viewModel()
    private val disposables by lazy { CompositeDisposable() }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPermission(ACCESS_FINE_LOCATION) { viewModel.onLocationPermissionGranted() }

        btnSetLocation.setOnClickListener {
            askPermission(ACCESS_FINE_LOCATION) {
                EnterCoordinatesDialog()
                    .onCoordinatesSet { viewModel.setTargetLocation(it) }
                    .show(supportFragmentManager, "enterCoordinatesDialog")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        disposables += viewModel.azimuth
            .applySchedulers()
            .subscribe(::showAzimuth)

        disposables += viewModel.bearing
            .applySchedulers()
            .subscribe(::showBearing)

        disposables += viewModel.currentLocation
            .applySchedulers()
            .subscribe(::showCurrentLocation)

        disposables += viewModel.targetLocation
            .applySchedulers()
            .subscribe(::showTargetLocation)
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    private fun showAzimuth(azimuth: Int) {
        Log.d("LOL", "azimuth $azimuth")
        tvAzimuth.text = (azimuth).toString()
    }

    private fun showBearing(bearing: Int) {
        Log.d("LOL", "bearing $bearing")
        tvBearing.text = (bearing).toString()
    }

    private fun showCurrentLocation(location: Location) {
        Log.d("LOL", "current location $location")
        tvCurrentLocation.text = "lat ${location.latitude} lng ${location.longitude}"
    }

    private fun showTargetLocation(location: Location) {
        Log.d("LOL", "ratget location $location")
        tvTargetLocation.text = "lat ${location.latitude} lng ${location.longitude}"
    }

    private fun <T> Observable<T>.applySchedulers(): Observable<T> {
        return this
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

