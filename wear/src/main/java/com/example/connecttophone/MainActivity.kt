package com.example.connecttophone

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import com.example.connecttophone.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import kotlin.properties.Delegates


private const val key = "steps"

class MainActivity : Activity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dataClient: DataClient
    private var count = 10
    var startTime by Delegates.notNull<Long>()
    var endTime by Delegates.notNull<Long>()
    var minutesTaken = 0
    lateinit var sensorManager: SensorManager
    var sensorHeart: Sensor? = null
    var sensorStep: Sensor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CheckingPermissions.requestPermissions(this)

        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorHeart = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        sensorStep = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        dataClient = Wearable.getDataClient(this)


        binding.btnStart.setOnClickListener {
            sensorManager.registerListener(this, sensorHeart, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(this, sensorStep, SensorManager.SENSOR_DELAY_NORMAL)
            startTime = System.currentTimeMillis()
            binding.btnStop.isEnabled = true
            binding.btnStart.isEnabled = false

        }
        binding.btnStop.setOnClickListener {
            sensorManager.unregisterListener(this)
            binding.btnStop.isEnabled = false
            binding.btnStart.isEnabled = true

            endTime = System.currentTimeMillis()
            val durationMinutes = (endTime - startTime).toDouble() / (1000 * 60)
            Log.d(TAG, "minutes: "+ durationMinutes)
            minutesTaken = durationMinutes.toInt()
            sendDataMap("minutes",minutesTaken.toFloat())
        }
    }



    override fun onStart() {
        super.onStart()
        try {
            //    sensorManager.registerListener(this, sensorHeart, SensorManager.SENSOR_DELAY_NORMAL)
            //    sensorManager.registerListener(this, sensorStep, SensorManager.SENSOR_DELAY_NORMAL)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            //   sensorManager.registerListener(this, sensorHeart, SensorManager.SENSOR_DELAY_NORMAL)
            //   sensorManager.registerListener(this, sensorStep, SensorManager.SENSOR_DELAY_NORMAL)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        //  sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                val heartData = event.values[0]
                binding.heartText.text = heartData.toString()
                sendDataMap("heartRate", heartData)
            } else if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val steps = event.values[0].toInt()
                binding.stepsText.text = steps.toString()
                sendDataMap("steps", steps.toFloat())
            }
        }
    }


    private fun sendDataMap(str: String, value: Float) {
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/data").run {
            dataMap.putFloat(str, value)
            asPutDataRequest()
        }
        val putDataRequest: Task<DataItem> = dataClient.putDataItem(putDataReq)
        putDataRequest.addOnSuccessListener {
            Log.d(str, "sendDataMap: " + value + "is sent successful")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}