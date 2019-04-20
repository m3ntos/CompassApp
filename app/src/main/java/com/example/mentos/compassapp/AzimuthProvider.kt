package com.example.mentos.compassapp

import android.content.Context
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorManager
import com.github.pwittchen.reactivesensors.library.ReactiveSensorFilter
import com.github.pwittchen.reactivesensors.library.ReactiveSensors
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import kotlin.math.roundToInt

class AzimuthProvider(appContext: Context) {

    private val sensors = ReactiveSensors(appContext)

    private val accelerometerEvents = sensors
        .observeSensor(TYPE_ACCELEROMETER)
        .filter(ReactiveSensorFilter.filterSensorChanged())
        .map { it.sensorEvent.values }


    private val magneticFieldEvents = sensors
        .observeSensor(TYPE_MAGNETIC_FIELD)
        .filter(ReactiveSensorFilter.filterSensorChanged())
        .map { it.sensorEvent.values }

    val azimuthInDegrees: Flowable<Int> = Flowables
        .combineLatest(accelerometerEvents, magneticFieldEvents, ::calcOrientation)
        .map { orientationAngles -> orientationAngles[0] }
        .map { Math.toDegrees(it.toDouble()) }
        .map { angle -> if (angle < 0) angle + 360 else angle }
        .map { it.roundToInt() }


    private fun calcOrientation(accelerometer: FloatArray, magnetometer: FloatArray): FloatArray {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, magnetometer)

        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        return orientationAngles
    }
}