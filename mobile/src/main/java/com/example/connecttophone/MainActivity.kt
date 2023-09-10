package com.example.connecttophone

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import com.google.android.gms.wearable.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {
    lateinit var healthConnectClient: HealthConnectClient
    var minutes = 0
    val startTime = System.currentTimeMillis()
    private val PERMISSION_SET: Set<String> =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getWritePermission(HeartRateRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class)
        )

    private lateinit var requestPermissions: ActivityResultLauncher<Set<String>>

    @SuppressLint("SuspiciousIndentation")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        healthConnectClient = HealthConnectClient.getOrCreate(this)
        try {
            val requestPermissionActivityContract =
                PermissionController.createRequestPermissionResultContract()
            requestPermissions =
                registerForActivityResult(requestPermissionActivityContract) { granted ->
                    if (granted.containsAll(PERMISSION_SET)) {
                        GlobalScope.launch {
                            HealthConnectReadWrite.checkPermissionsAndRun(
                                healthConnectClient,
                                requestPermissions,
                                PERMISSION_SET
                            )
                        }
                    } else {
                        //
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        GlobalScope.launch {
            try {
                HealthConnectReadWrite.checkPermissionsAndRun(
                    healthConnectClient,
                    requestPermissions,
                    PERMISSION_SET
                )
                //     HealthConnectReadWrite.insertHeartData(healthConnectClient, 78)
                //     HealthConnectReadWrite.insertSteps(healthConnectClient,60)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        val caloriesBtn: Button = findViewById(R.id.caloriesBtn)
        caloriesBtn.setOnClickListener {
            val heart =
                getSharedPreferences("healthData", Context.MODE_PRIVATE).getFloat("heartData", 1f)
            val weight = findViewById<EditText>(R.id.caloriesInput).text.toString()
            if (heart.toInt() == 0) {
                Toast.makeText(this, "Make sure your heart rate value is shown", Toast.LENGTH_LONG)
                    .show()
            } else if (weight == "") {
                Toast.makeText(this, "Enter your weight value", Toast.LENGTH_LONG).show()
            }else if(minutes < 15){
                Toast.makeText(this, "Train at least for 15 minutes", Toast.LENGTH_LONG).show()

            }
            else {
                val cal = calculateCaloriesBurned(heart.toDouble(), weight.toInt())
                Toast.makeText(
                    this,
                    "You burned $cal calories during $minutes minutes",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @SuppressLint("CommitPrefEdits", "SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onDataChanged(p0: DataEventBuffer) {
        p0.forEach { it ->
            if (it.type == DataEvent.TYPE_CHANGED) {
                it.dataItem.also {
                    if (it.uri.path?.compareTo("/data") == 0) {
                        Toast.makeText(this, "data received", Toast.LENGTH_SHORT).show()
                        DataMapItem.fromDataItem(it).dataMap.apply {

                            //minutes visibility
                            minutes = this.getFloat("minutes", 0f).toInt()
                            Toast.makeText(applicationContext,"$minutes",Toast.LENGTH_SHORT).show()
                            findViewById<TextView>(R.id.caloriesBtn).isEnabled = true

                            //heart Data work
                            val heartData: Float = this.getFloat("heartRate")
                            if (heartData.toInt() != 0) {
                                val sharedPreferences =
                                    getSharedPreferences("healthData", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putFloat("heartData", heartData)
                            }

                            val fakeSteps = this.getInt("step") + 1
                            val steps = this.getFloat("steps")

                            //views shown
                            findViewById<TextView>(R.id.stepsText).text = steps.toString() + " steps"
                            findViewById<TextView>(R.id.heartText).text = heartData.toInt().toString() + "bpm"


                            GlobalScope.launch {
                                HealthConnectReadWrite.insertHeartData(
                                    healthConnectClient,
                                    heartData.toLong() + 1
                                )
                                HealthConnectReadWrite.insertSteps(
                                    healthConnectClient,
                                    steps.toLong() + 1
                                )
                            }
                        }
                    } else {
                        Log.d(TAG, "onDataChanged: ")
                    }
                }
            }
        }
    }

    private fun calculateCaloriesBurned(heartRate: Double, weight: Int): Double {
        val caloriesBurnedPerMinute = (-55.0969 + (0.6309 * heartRate) + (0.1988 * weight)) / 4.184
        return caloriesBurnedPerMinute * minutes
    }

    fun calculateBMI(weight: Double, height: Double): Double {
        val heightMeters = height / 100 // Convert height from centimeters to meters
        return weight / (heightMeters * heightMeters)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onStart() {
        super.onStart()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }
}