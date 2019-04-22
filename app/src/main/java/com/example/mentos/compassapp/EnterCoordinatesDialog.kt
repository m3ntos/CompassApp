package com.example.mentos.compassapp

import android.app.Dialog
import android.content.DialogInterface
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_enter_coordinates.view.*


class EnterCoordinatesDialog : DialogFragment() {

    private lateinit var customView: View
    private var onCoordinatesSetListener: ((location: Location) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        customView = inflater.inflate(R.layout.dialog_enter_coordinates, null)

        return AlertDialog.Builder(context!!)
            .setTitle("Enter target coordinates")
            .setView(customView)
            .setPositiveButton(android.R.string.ok, ::onPositiveButtonClick)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    fun onCoordinatesSet(onCoordinatesSet: (location: Location) -> Unit): EnterCoordinatesDialog {
        this.onCoordinatesSetListener = onCoordinatesSet
        return this
    }

    private fun onPositiveButtonClick(dialog: DialogInterface, which: Int) {
        val latitude = customView.etLatitude.text.toString().toDouble()
        val longitude = customView.etLongitude.text.toString().toDouble()

        val location = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        onCoordinatesSetListener?.invoke(location)
    }
}