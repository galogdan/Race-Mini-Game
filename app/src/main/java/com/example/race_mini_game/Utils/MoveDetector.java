package com.example.race_mini_game.Utils;// MoveDetector.java
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.example.race_mini_game.Interfaces.MoveCallback;

public class MoveDetector implements SensorEventListener {      // MoveDetector for sensor mode

    private static final float MOVE_THRESHOLD = 2.5f;
    private static final float SENSOR_CONSTANT = 0.1f;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private MoveCallback moveCallback;


    public MoveDetector(Context context, MoveCallback moveCallback) {
        this.moveCallback = moveCallback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    public void start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];


        if (x > MOVE_THRESHOLD) {
            moveCallback.onMoveLeft();
        } else if (x < -MOVE_THRESHOLD) {
            moveCallback.onMoveRight();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}
