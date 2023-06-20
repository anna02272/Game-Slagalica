package com.example.slagalica;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7f;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 500;

    private long shakeTimestamp;
    private OnShakeListener listener;

    public interface OnShakeListener {
        void onShake();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (listener != null) {
            float x = event.values[0] / SensorManager.GRAVITY_EARTH;
            float y = event.values[1] / SensorManager.GRAVITY_EARTH;
            float z = event.values[2] / SensorManager.GRAVITY_EARTH;

            float gForce = (float) Math.sqrt(x * x + y * y + z * z);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                if (shakeTimestamp + SHAKE_TIMEOUT > now) {
                    return;
                }

                if (shakeTimestamp + SHAKE_DURATION < now) {
                    listener.onShake();
                }
                shakeTimestamp = now;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
