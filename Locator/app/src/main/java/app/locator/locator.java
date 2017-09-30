package app.locator;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class locator extends AppCompatActivity {

    private TextView print;
    private ScrollView scrollView;

    private Sensor mStepSensor;
    private SensorManager mSensorManager;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private boolean flag = false;
    private double[] tuple = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator);
        Button locator = (Button) this.findViewById(R.id.locator);
        print = (TextView) this.findViewById(R.id.print);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Sensor magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(listener, magneticSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(listener, accelerometerSensor,
                SensorManager.SENSOR_DELAY_FASTEST);

        locator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getlocation();
            }
        });
    }

    private SensorEventListener listener = new SensorEventListener() {
        private int mStep;
        double lastangle = 0, currentangle = -181;
        int lastStepCount = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            final float alpha = 0.97f;
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                if (event.values[0] == 1.0f) {
                    mStep++;
                }
                if (Math.abs(lastangle - currentangle) > 180) {
                    int steps = mStep - lastStepCount;
                    //存steps和lastAngle 到 route
                    double[] tuple = new double[2];
                    tuple[0] = steps;
                    tuple[1] = lastangle;
                    flag = true;

                    print.append(System.currentTimeMillis() + ": a: " + tuple[0] + " " + tuple[1] + '\n');
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);

                    if (Math.abs(lastangle + currentangle) >= 10) {
                        lastangle = currentangle;
                    }
                    lastStepCount = mStep;
                    //}
                } else {
                    //if (Math.abs(lastangle - currentangle) >= 10) {
                    int steps = mStep - lastStepCount;
                    //存steps和lastAngle 到 route
                    tuple = new double[2];
                    tuple[0] = steps;
                    tuple[1] = lastangle;
                    flag = true;

                    print.append(System.currentTimeMillis() + ": b: " + tuple[0] + " " + tuple[1] + '\n');
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);

                    if (Math.abs(lastangle - currentangle) >= 10) {
                        lastangle = currentangle;
                    }
                    lastStepCount = mStep;
                }

            } else {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                            * event.values[0];
                    mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                            * event.values[1];
                    mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                            * event.values[2];
                }
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
                            * event.values[0];
                    mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
                            * event.values[1];
                    mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
                            * event.values[2];
                }
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                        mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                }
                double degree = azimuth;
                if (currentangle == -181) {
                    currentangle = degree;
                } else if (Math.abs(currentangle - degree) <= 45) {
                    currentangle = degree;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void getlocation() {
    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(listener, mStepSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(mSensorEventListener);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(listener);
        }
    }

    public void startwork() {

        if (flag) {
            flag = false;
            print.append(System.currentTimeMillis() + ": c: " + tuple[0] + " " + tuple[1] + '\n');
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

}
