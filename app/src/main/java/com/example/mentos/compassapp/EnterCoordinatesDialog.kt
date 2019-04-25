package com.example.mentos.compassapp


import android.app.Dialog
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog.Builder
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_enter_coordinates.view.*

class EnterCoordinatesDialog : DialogFragment() {

    interface Callback {
        fun onCoordinatesSet(location: Location)
    }

    private lateinit var customView: View
    private lateinit var callback: Callback

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        customView = inflater.inflate(R.layout.dialog_enter_coordinates, null)

        return Builder(requireContext())
            .setTitle("Enter target coordinates")
            .setView(customView)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .also { initDialog(it) }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as Callback
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement EnterCoordinatesDialog.Callback")
        }
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
        callback.onCoordinatesSet(location)
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