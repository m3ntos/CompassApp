package com.example.mentos.compassapp.extensions

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.applyAndroidSchedulers(): Observable<T> {
    return this
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
}