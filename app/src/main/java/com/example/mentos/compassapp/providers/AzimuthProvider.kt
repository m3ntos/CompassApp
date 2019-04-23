package com.example.mentos.compassapp.providers

import android.content.Context
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorManager
import com.github.pwittchen.reactivesensors.library.ReactiveSensorFilter
import com.github.pwittchen.reactivesensors.library.ReactiveSensors
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.subjects.PublishSubject


class AzimuthProvider(appContext: Context) {

    private val sensors = ReactiveSensors(appContext)

    private val accelerometerEvents = sensors
        .observeSensor(TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_UI)
        .filter(ReactiveSensorFilter.filterSensorChanged())
        .map { it.sensorEvent.values }


    private val magneticFieldEvents = sensors
        .observeSensor(TYPE_MAGNETIC_FIELD, SensorManager.SENSOR_DELAY_UI)
        .filter(ReactiveSensorFilter.filterSensorChanged())
        .map { it.sensorEvent.values }

    val azimuthInDegrees: Flowable<Float> = Flowables
        .combineLatest(accelerometerEvents, magneticFieldEvents, ::calcOrientation)
        .map { orientationAngles -> orientationAngles[0] }
        .map { it.toDouble() }
        .movingAverage(50)
        .map { Math.toDegrees(it) }
        .map { angle -> if (angle < 0) angle + 360 else angle }
        .map { it.toFloat() }

    private fun calcOrientation(accelerometer: FloatArray, magnetometer: FloatArray): FloatArray {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, magnetometer)

        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        return orientationAngles
    }

    private fun Flowable<Double>.movingAverage(n: Int): Flowable<Double> {
        val publishSubject = PublishSubject.create<Double>()

        val movingAverage = this
            .doOnNext { publishSubject.onNext(it) }
            .buffer(n, 1)

        val averagesUntilBufferReachesN = publishSubject
            .toFlowable(BackpressureStrategy.LATEST)
            .take(n.toLong())
            .scan(listOf()) { list: List<Double>, value: Double -> list + value }

        return Flowable.merge(averagesUntilBufferReachesN, movingAverage)
            .map { averageAngles(it) }
    }

    private fun averageAngles(list: List<Double>): Double {
        val sinAvg = list.map { Math.sin(it) }.average()
        val cosAvg = list.map { Math.cos(it) }.average()
        return Math.atan2(sinAvg, cosAvg)
    }
}