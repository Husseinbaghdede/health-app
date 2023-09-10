package com.example.connecttophone

import androidx.activity.result.ActivityResultLauncher
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class HealthConnectReadWrite {

    companion object {

        private fun buildHeartRateSeries(
            sessionStartTime: ZonedDateTime,
            sessionEndTime: ZonedDateTime,
            data: Long
        ): HeartRateRecord {
            val samples = mutableListOf<HeartRateRecord.Sample>()
            samples.add(
                HeartRateRecord.Sample(
                    time = ZonedDateTime.now().minusSeconds(1).toInstant(),
                    beatsPerMinute = data + 1
                )
            )
            return HeartRateRecord(
                startTime = sessionStartTime.toInstant(),
                startZoneOffset = sessionStartTime.offset,
                endTime = sessionEndTime.toInstant(),
                endZoneOffset = sessionEndTime.offset,
                samples = samples
            )
        }


        suspend fun insertHeartData(healthConnectClient: HealthConnectClient, data: Long) {
            val now = LocalDateTime.now()
            val timeZone = ZoneId.systemDefault()
            val start =
                ZonedDateTime.of(
                    now.year,
                    now.monthValue,
                    now.dayOfMonth,
                    now.hour,
                    now.minute,
                    0,
                    0,
                    timeZone
                )
            val end = start.plusMinutes(1)
            healthConnectClient.insertRecords(
                listOf(
                    buildHeartRateSeries(start, end, data)
                )
            )
        }


        @OptIn(DelicateCoroutinesApi::class)
        suspend fun insertSteps(healthConnectClient: HealthConnectClient, data: Long) {
                val startTime = ZonedDateTime.now().minusSeconds(1).toInstant()
                val endTime = ZonedDateTime.now().toInstant()
                val records = listOf(
                    StepsRecord(
                        count = data,
                        startTime = startTime,
                        endTime = endTime,
                        startZoneOffset = null,
                        endZoneOffset = null,
                    ) )
                GlobalScope.launch {
                    healthConnectClient.insertRecords(records)
                }
        }
        suspend fun checkPermissionsAndRun(
            healthConnectClient: HealthConnectClient,
            requestPermissions: ActivityResultLauncher<Set<String>>,
            permissionsSet: Set<String>
        ) {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(permissionsSet)) {
               //
            } else {
                requestPermissions.launch(permissionsSet)
            }
        }

    }

}