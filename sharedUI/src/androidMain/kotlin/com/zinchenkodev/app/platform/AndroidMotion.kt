package com.zinchenkodev.app.platform

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidMotionProvider(
    private val sensorManager: SensorManager,
    private val gravitySensor: Sensor?,
    private val accelerometer: Sensor?,
) : MotionProvider {
    override val isAvailable: Boolean = gravitySensor != null || accelerometer != null

    @Composable
    override fun rememberTiltGravityVector(): State<GravityVector> {
        val state: MutableState<GravityVector> = remember { mutableStateOf(GravityVector(0f, 0f, 0f)) }

        DisposableEffect(sensorManager, gravitySensor, accelerometer, isAvailable) {
            if (isAvailable.not()) return@DisposableEffect onDispose {}

            val sensor = gravitySensor ?: accelerometer ?: return@DisposableEffect onDispose {}
            val listener = object : SensorEventListener {
                var last = GravityVector(0f, 0f, 0f)
                var hasLast = false

                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val next = if (gravitySensor != null) {
                        GravityVector(x, y, z)
                    } else {
                        if (hasLast.not()) {
                            hasLast = true
                            last = GravityVector(x, y, z)
                        } else {
                            last = GravityVector(
                                x = last.x + ACCEL_FILTER_ALPHA * (x - last.x),
                                y = last.y + ACCEL_FILTER_ALPHA * (y - last.y),
                                z = last.z + ACCEL_FILTER_ALPHA * (z - last.z),
                            )
                        }
                        last
                    }

                    state.value = next
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            onDispose { sensorManager.unregisterListener(listener) }
        }

        return state
    }

    private companion object {
        private const val ACCEL_FILTER_ALPHA: Float = 0.2f
    }
}

@Composable
actual fun rememberMotionProvider(): MotionProvider {
    val context = LocalContext.current
    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val gravitySensor = remember(sensorManager) { sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) }
    val accelerometer = remember(sensorManager) { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    return remember(sensorManager, gravitySensor, accelerometer) {
        AndroidMotionProvider(sensorManager, gravitySensor, accelerometer)
    }
}
