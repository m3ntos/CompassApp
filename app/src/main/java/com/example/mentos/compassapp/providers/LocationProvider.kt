package com.example.mentos.compassapp.providers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.patloew.rxlocation.RxLocation
import io.reactivex.Observable

class LocationProvider(appContext: Context) {

    private val rxLocation = RxLocation(appContext)

    private val locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(5000)

    @SuppressLint("MissingPermission")
    val currentLocation: Observable<Location> = rxLocation.location()
        .updates(locationRequest)
        .onExceptionResumeNext(Observable.empty()) // without permissions, just return empty
}