package com.example.mentos.compassapp


import android.app.Dialog
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog.Builder
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_enter_coordinates.view.*

class EnterCoordinatesDialog : DialogFragment() {

    private lateinit var customView: View
    private var onCoordinatesSetListener: ((location: Location) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        customView = inflater.inflate(R.layout.dialog_enter_coordinates, null)

        return Builder(context!!)
            .setTitle("Enter target coordinates")
            .setView(customView)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .also { initDialog(it) }

    }

    fun onCoordinatesSet(onCoordinatesSet: (location: Location) -> Unit): EnterCoordinatesDialog {
        this.onCoordinatesSetListener = onCoordinatesSet
        return this
    }

    private fun initDialog(dialog: AlertDialog) {
        dialog.setOnShowListener {
            dialog.getButton(BUTTON_POSITIVE).setOnClickListener { onPositiveButtonClick() }
        }
    }

    private fun onPositiveButtonClick() {
        val latitude = customView.etLatitude.text.toString()
        val longitude = customView.etLongitude.text.toString()


        if (!isValid(latitude, longitude)) {
            setErrorMessages(latitude, longitude)
            return
        }

        val location = Location("").apply {
            this.latitude = latitude.toDouble()
            this.longitude = longitude.toDouble()
        }
        onCoordinatesSetListener?.invoke(location)
        dismiss()
    }

    private fun setErrorMessages(latitude: String, longitude: String) {
        customView.latitudeLayout.error = if (validateLatitude(latitude)) null
        else getString(R.string.coordinates_dialog_error_latitude)

        customView.longitudeLayout.error = if (validateLongitude(longitude)) null
        else getString(R.string.coordinates_dialog_error_longitude)
    }

    private fun isValid(latitude: String, longitude: String): Boolean {
        return validateLatitude(latitude) && validateLongitude(longitude)
    }

    private fun validateLatitude(latStr: String): Boolean {
        val latitude = latStr.toDoubleOrNull() ?: return false
        return latitude >= -90 && latitude <= 90
    }

    private fun validateLongitude(lngStr: String): Boolean {
        val latitude = lngStr.toDoubleOrNull() ?: return false
        return latitude >= -180 && latitude <= 180
    }
}