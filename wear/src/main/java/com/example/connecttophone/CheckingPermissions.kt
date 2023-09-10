package com.example.connecttophone

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class CheckingPermissions {
    companion object {
        private fun hasBodyPermission(context: Context) = ActivityCompat
            .checkSelfPermission(
                context,
                android.Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_GRANTED;
        private fun hasActivityRecognition(context: Context) = ActivityCompat
            .checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED;

        fun requestPermissions(context:Context) {
            try{
                val permissionTR = mutableListOf<String>()
                if (!hasBodyPermission(context)) {
                    permissionTR.add((android.Manifest.permission.BODY_SENSORS))

                }
                if (!hasActivityRecognition(context)) {
                    permissionTR.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
                }
                if (permissionTR.isNotEmpty()) {
                    ActivityCompat.requestPermissions(context as Activity, permissionTR.toTypedArray(), 0)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }

        }


    }
}