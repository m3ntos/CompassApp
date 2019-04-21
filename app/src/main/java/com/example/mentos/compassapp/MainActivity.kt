package com.example.mentos.compassapp

import android.hardware.GeomagneticField
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.gojuno.koptional.None
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val disposables by lazy { CompositeDisposable() }

    private val azimuthProvider: AzimuthProvider by lazy { AzimuthProvider(this) }
    private val locationProvider: LocationProvider by lazy { LocationProvider(this) }

    var permissionGrantedEvent = PublishSubject.create<Unit>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button2.setOnClickListener {

            askPermission { permissionGrantedEvent.onNext(Unit) }
        }
    }


    override fun onStart() {
        super.onStart()

        val azimuth = azimuthProvider.azimuthInDegrees.toObservable()


        val currLocation: Observable<Location> = locationProvider.currentLocation
            .onExceptionResumeNext(Observable.empty())
            .repeatWhen { repeatHandler -> repeatHandler.flatMap { permissionGrantedEvent } }

        azimuth
            .tryCalcTrueNorth(currLocation)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

            .subscribe(::showAzimuth)
            .apply { disposables.add(this) }



        currLocation
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::showLocation)
            .apply { disposables.add(this) }
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    private fun Observable<Int>.tryCalcTrueNorth(location: Observable<Location>): Observable<Int> {
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

    private fun calcTrueNorth(azimuth: Int, location: Location): Int {
        val declination = calcDeclination(location)
        return (azimuth + declination).toInt()
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

    private fun showAzimuth(azimuth: Int) {
        Log.d("LOL", "azimuth $azimuth")
        textView.text = (azimuth).toString()
    }

    private fun showLocation(location: Location) {
        Log.d("LOL", "location $location")
        tvLocation.text = "lat ${location.latitude} lng ${location.longitude}"
    }
}

