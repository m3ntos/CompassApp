package com.example.mentos.compassapp.di

import com.example.mentos.compassapp.CompassViewModel
import com.example.mentos.compassapp.providers.AzimuthProvider
import com.example.mentos.compassapp.providers.LocationProvider
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { LocationProvider(androidContext()) }
    single { AzimuthProvider(androidContext()) }

    viewModel { CompassViewModel(get(), get()) }
}