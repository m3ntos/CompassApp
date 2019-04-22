package com.example.mentos.compassapp

import android.hardware.GeomagneticField
import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.mentos.compassapp.providers.AzimuthProvider
import com.example.mentos.compassapp.providers.LocationProvider
import com.gojuno.koptional.None
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import kotlin.math.roundToInt

class CompassViewModel(
    azimuthProvider: AzimuthProvider,
    locationProvider: LocationProvider
) : ViewModel() {

    val currentLocation: Observable<Location> = locationProvider.currentLocation
        .onExceptionResumeNext(Observable.empty())
        .repeatWhen { repeatHandler -> repeatHandler.flatMap { permissionGrantedEvent } }

    var targetLocation: Observable<Location> = PublishSubject.create<Location>()

    /**
     * angle in degrees from 0 to 360 clockwise between north and the direction the phone is facing
     */
    val azimuth: Observable<Int> = azimuthProvider.azimuthInDegrees
        .toObservable()
        .tryCalcTrueNorth(currentLocation)
        .map { it.roundToInt() }

    /**
     * angle in degrees from 0 to 360 clockwise between true north and [targetLocation]
     */
    val bearing: Observable<Int> = Observables
        .combineLatest(currentLocation, targetLocation, ::calcBearing)
        .map { it.roundToInt() }

    private var permissionGrantedEvent = PublishSubject.create<Unit>()


    fun onLocationPermissionGranted() {
        permissionGrantedEvent.onNext(Unit)
    }

    fun setTargetLocation(targetLocation: Location) {
        (this.targetLocation as PublishSubject).onNext(targetLocation)
    }

    /**
     * if location data is available calculate true north, otherwise return magnetic north
     */
    private fun Observable<Float>.tryCalcTrueNorth(location: Observable<Location>): Observable<Float> {
        val optionalLocation = location
            .map { it.toOptional() }
            .startWith(None)

        return Observables.combineLatest(this, optionalLocation) { azimuth, location ->
            when (location) {
                is Some -> calcTrueNorth(azimuth, location.value)
                is None -> azimuth
            }
        }
    }

    private fun calcTrueNorth(azimuth: Float, location: Location): Float {
        val declination = calcDeclination(location)
        val newAzimuth = azimuth + declination
        return if (newAzimuth > 360) newAzimuth - 360 else newAzimuth
    }

    private fun calcDeclination(location: Location): Float {
        val geoField = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )
        return geoField.declination
    }

    private fun calcBearing(currentLocation: Location, targetLocation: Location): Float {
        val bearing = currentLocation.bearingTo(targetLocation)
        return if (bearing < 0) bearing + 360 else bearing
    }
}