/*
Alexander Ching
CSE 562 SPRING 2021 A2

Adapted from
https://github.com/wanganran/CSE562_Android_sample/blob/main/StepCounter/MainActivity.java
https://www.youtube.com/watch?v=pkT7DU1Yo9Q

 */



package com.example.cse562_a2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;


import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;


import java.io.File;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Sensor accel;
    private Sensor gyro;
    private Sensor gravity;

    private BufferedWriter buffAccel;
    private BufferedWriter buffGyro;
    private BufferedWriter buffGravity;
    private File fileAccel;
    private File fileGyro;
    private File fileGravity;
    private String filePath;

    private Button startB;
    private boolean isPushed;

    private Context context = this;

    private double tiltX;
    private double tiltY;
    private TextView tiltXValue;
    private TextView tiltYValue;


    private float[] lastGyro = new float[3];
    private float[] lastAccel = new float[3];
    private float[] lastGravity = new float[3];
    private double localGravity;
    private double lastTimeStamp;
    private double[] accelTilt = new double[2];
    private double[] lastGyroTilt = new double[2];
    private double[] gyroTilt = new double[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isPushed = false;
        tiltX = 0;
        tiltY = 0;
        lastGyroTilt[0] = 0;
        lastGyroTilt[1] = 0;

        // Create Sensors
        Log.d(TAG, "onCreate: Initializing Sensor Services");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);

        // Create Text View

        tiltXValue = (TextView) findViewById(R.id.tiltX);
        tiltYValue = (TextView) findViewById(R.id.tiltY);

        Log.d(TAG, "onCreate: Registered IMU listener");

        // Create Directory

        File directory = new File(context.getExternalFilesDir(null) + "/IMU/");
        boolean isPresent = true;
        if (!directory.exists())
        {
            try{
                isPresent = directory.mkdirs();
            }
            catch(Exception e){
                Log.i("Exception", e.toString());
            }
        }
        if (isPresent)
        {
            Log.i("Directory", directory.getAbsolutePath());
            fileGyro = new File(directory.getAbsolutePath(), "gyro.csv");
            fileAccel = new File(directory.getAbsolutePath(), "accel.csv");
            fileGravity = new File(directory.getAbsolutePath(), "gravity.csv");

            if (fileGyro.exists())
            {
                fileGyro.delete();
            }
            if (fileAccel.exists())
            {
                fileAccel.delete();
            }
            if(fileGravity.exists())
            {
                fileGravity.delete();
            }
        }
        else
        {
            Log.i("Error", "Did not create Directory");
        }


        // Button Setup
        startB = (Button) findViewById(R.id.startB);
        startB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("MyApp", "Button pushed!");

                tiltXValue.setText(Double.toString(tiltX));
                tiltYValue.setText(Double.toString(tiltY));
                lastGyroTilt[0] = 0;
                lastGyroTilt[1] = 0;

                isPushed = !isPushed;

                if (isPushed) {
                    startB.setBackgroundColor(Color.GREEN);
                    startB.setText("Recording!");

                    try {
                        buffGyro = new BufferedWriter(new FileWriter(fileGyro, true));
                        buffAccel = new BufferedWriter(new FileWriter(fileAccel, true));
                        buffGravity = new BufferedWriter(new FileWriter(fileGravity, true));
                    } catch (IOException e) {
                        Log.i("Exception", e.toString());
                    }

                }
                else {
                    startB.setBackgroundColor(Color.BLACK);
                    startB.setText("START!");

                    try {
                        buffGyro.close();
                        buffAccel.close();
                        buffGravity.close();

                    } catch (IOException e) {
                        Log.i("Exception", e.toString());
                    }

                }
            }
        });

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Long timeStamp = event.timestamp;
        String ts = timeStamp.toString();

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Log.d(TAG, "Time: " + ts + " Accel Changed: X: " + event.values[0] + "   Y: " + event.values[1] + "   Z: " + event.values[2]);
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (isPushed){
                try {
                    // buffAccel.append("" + "accel");
                    buffAccel.append(ts);
                    buffAccel.append(", " + x);
                    buffAccel.append(", " + y);
                    buffAccel.append(", " + z);
                    buffAccel.newLine();

                    //Log.d("Accel", x + "\t" + y + "\t" + z);
                    buffAccel.flush();
                } catch (IOException e){
                    Log.i("File", e.toString());
                }
                lastAccel[0] = x;
                lastAccel[1] = y;
                lastAccel[2] = z;
            }
        }


        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //Log.d(TAG, "Time: " + ts + "Gyro Changed: X: " + event.values[0] + "   Y: " + event.values[1] + "   Z: " + event.values[2]);
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double deltaTimeStamp = ((double) timeStamp - lastTimeStamp)/1000000000; // Get change in timestamps between gyro measurements.
            lastTimeStamp = (double) timeStamp;

            // Log.d("deltaT", Double.toString(deltaTimeStamp) + ' ' + Long.toString(timeStamp) + ' ' + Double.toString(lastTimeStamp));
            // Calculate new tilt angle in radians.
            gyroTilt[0] = ((double) x + (double) lastGyro[0])/2 * deltaTimeStamp + lastGyroTilt[0]; // X axis
            gyroTilt[1] = ((double) y + (double) lastGyro[1])/2 * deltaTimeStamp + lastGyroTilt[1]; // Y axis

            // Log.d("gyro", Double.toString(gyroTilt[0]) + ' ' + Double.toString(lastGyroTilt[0]));


            // Update last values.
            lastGyroTilt[0] = gyroTilt[0];
            lastGyroTilt[1] = gyroTilt[1];

            if (isPushed){
                try {
                    // buffGyro.append("" + "gyro");
                    buffGyro.append(ts);
                    buffGyro.append(", " + x);
                    buffGyro.append(", " + y);
                    buffGyro.append(", " + z);
                    buffGyro.newLine();

                    //Log.d("Gyro", x + "\t" + y + "\t" + z);

                    buffGyro.flush();
                } catch (IOException e){
                    Log.i("File", e.toString());
                }
                lastGyro[0] = x;
                lastGyro[1] = y;
                lastGyro[2] = z;
            }
        }


        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            //Log.d(TAG, "Time: " + ts + "Gyro Changed: X: " + event.values[0] + "   Y: " + event.values[1] + "   Z: " + event.values[2]);
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (isPushed){
                try {
                    // buffGyro.append("" + "gyro");
                    buffGravity.append(ts);
                    buffGravity.append(", " + x);
                    buffGravity.append(", " + y);
                    buffGravity.append(", " + z);
                    buffGravity.newLine();

                    //Log.d("Gravity", x + "\t" + y + "\t" + z);

                    buffGravity.flush();
                } catch (IOException e){
                    Log.i("File", e.toString());
                }
                lastGravity[0] = x;
                lastGravity[1] = y;
                lastGravity[2] = z;

                localGravity = Math.sqrt(x*x + y*y + z*z);
            }
        }

        if(isPushed) {
            double b = 0.95;
            Log.d("gravity", Double.toString(localGravity));
            accelTilt[0] = Math.asin(lastAccel[0] / localGravity);
            accelTilt[1] = Math.asin(lastAccel[1] / localGravity);

            tiltX = (b * gyroTilt[0] + (1 - b) * accelTilt[0]) * 180 / Math.PI;
            tiltY = (b * gyroTilt[1] + (1 - b) * accelTilt[1]) * 180 / Math.PI;

            tiltXValue.setText(Double.toString(tiltX));
            tiltYValue.setText(Double.toString(tiltY));

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}