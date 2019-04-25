package com.example.mentos.compassapp

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.runtimepermission.kotlin.askPermission
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class CompassActivity : AppCompatActivity() {

    private val viewModel: CompassViewModel by viewModel()
    private val disposables by lazy { CompositeDisposable() }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askForLocationPermissionWithRationaleDialog()

        btnSetLocation.setOnClickListener {
            askForLocationPermissionWithRationaleDialog {
                EnterCoordinatesDialog()
                    .onCoordinatesSet { viewModel.setTargetLocation(it) }
                    .show(supportFragmentManager, "enterCoordinatesDialog")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        observeData()
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    private fun observeData() {
        disposables += viewModel.azimuth
            .applySchedulers()
            .subscribe(compassView::setAzimuth)

        disposables += viewModel.bearing
            .applySchedulers()
            .subscribe(compassView::setBearing)

        disposables += viewModel.currentLocation
            .applySchedulers()
            .subscribe(::showCurrentLocation)

        disposables += viewModel.targetLocation
            .applySchedulers()
            .subscribe(::showTargetLocation)
    }

    private fun showCurrentLocation(location: Location) {
        tvCurrentLocation.text =
            getString(R.string.location_format, location.latitude, location.longitude)
    }

    private fun showTargetLocation(location: Location) {
        tvTargetLocation.text =
            getString(R.string.location_format, location.latitude, location.longitude)
    }

    private fun <T> Observable<T>.applySchedulers(): Observable<T> {
        return this
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun askForLocationPermissionWithRationaleDialog(onGranted: () -> Unit = {}) {
        askPermission(ACCESS_FINE_LOCATION) {
            viewModel.onLocationPermissionGranted()
            onGranted()
        }.onDeclined {
            AlertDialog.Builder(this)
                .setTitle(R.string.enable_location_dialog_title)
                .setMessage(R.string.enable_location_dialog_message)
                .setPositiveButton(R.string.enable_location_dialog_positive_btn) { _, _ -> it.askAgain() }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }
    }
}

