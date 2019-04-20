package com.example.mentos.compassapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val disposables by lazy { CompositeDisposable() }

    private val azimuthProvider: AzimuthProvider by lazy { AzimuthProvider(this) }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        azimuthProvider.azimuthInDegrees
            .subscribe(::showAzimuth)
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
}

