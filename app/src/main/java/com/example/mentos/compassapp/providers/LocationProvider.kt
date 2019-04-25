package com.example.mentos.compassapp.providers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.patloew.rxlocation.RxLocation
import io.reactivex.Observable

class LocationProvider(appContext: Context) {

    companion object {
        const val LOCATION_UPDATES_INTERVAL_IN_MILLIS = 5000L
    }

    private val rxLocation = RxLocation(appContext)

    private val locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(LOCATION_UPDATES_INTERVAL_IN_MILLIS)

    @SuppressLint("MissingPermission")
    private val locationObservable: Observable<Location> = rxLocation.location()
        .updates(locationRequest)

    val currentLocation: Observable<Location> = rxLocation.settings()
        .checkAndHandleResolution(locationRequest)
        .flatMapObservable { locationObservable }
        .publish()
        .refCount()


}