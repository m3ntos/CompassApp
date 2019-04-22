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

    val azimuth: Observable<Int> = azimuthProvider.azimuthInDegrees
        .toObservable()
        .tryCalcTrueNorth(currentLocation)
        .map { it.roundToInt() }

    private var permissionGrantedEvent = PublishSubject.create<Unit>()


    fun onLocationPermissionGranted() {
        permissionGrantedEvent.onNext(Unit)
    }

    fun setTargetLocation(targetLocation: Location) {

    }

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
        return azimuth + declination
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


    //    private fun <T1 : Any, T2 : Any> Observable<T1>.combineIfNotEmpty(
//        source2: Observable<T2>,
//        combineFunction: (T1, T2) -> T1
//    ): Observable<T1> {
//        val optionalSource2 = source2
//            .map { it.toOptional() }
//            .startWith(None)
//
//        return Observables.combineLatest(this, optionalSource2) { source1, source2 ->
//            when (source2) {
//                is Some -> combineFunction(source1, source2.value)
//                is None -> source1
//            }
//        }
//    }
}