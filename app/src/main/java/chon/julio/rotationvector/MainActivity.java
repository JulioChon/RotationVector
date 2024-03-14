package chon.julio.rotationvector;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;


import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private float[] rotationMatrix = new float[9];
    private float[] orientationValues = new float[3];
    private float bubbleX, bubbleY;
    private BubbleView bubbleView;
    private boolean isInSquare = false;
    private int enterCount = 0;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String ENTER_COUNT_KEY = "EnterCount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bubbleView = new BubbleView(this);
        setContentView(bubbleView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        enterCount = sharedPreferences.getInt(ENTER_COUNT_KEY, 0);

        bubbleView.setEnterCount(enterCount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationValues);


            float pitch = orientationValues[1];
            float roll = orientationValues[2];
            bubbleX = (roll + 1) * ((float) bubbleView.getWidth() / 2);
            bubbleY = (pitch + 1) * ((float) bubbleView.getHeight() / 2);


            boolean newIsInSquare = isInSquare(bubbleX, bubbleY, bubbleView.getSquareLeft(), bubbleView.getSquareTop(),
                    bubbleView.getSquareRight(), bubbleView.getSquareBottom());
            if (newIsInSquare && !isInSquare) {

                enterCount++;
                isInSquare = true;
                bubbleView.setEnterCount(enterCount);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(ENTER_COUNT_KEY, enterCount);
                editor.apply();
            } else if (!newIsInSquare && isInSquare) {
                
                isInSquare = false;
            }


            bubbleView.invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean isInSquare(float x, float y, int left, int top, int right, int bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    private class BubbleView extends View {
        private Paint bubblePaint;
        private Paint squarePaint;
        private int squareSize = 200;
        private int squareLeft, squareTop, squareRight, squareBottom;
        private int enterCount;

        public BubbleView(Context context) {
            super(context);
            bubblePaint = new Paint();
            bubblePaint.setColor(Color.BLUE);
            bubblePaint.setAntiAlias(true);

            squarePaint = new Paint();
            squarePaint.setColor(Color.RED);
            squarePaint.setStyle(Paint.Style.STROKE);
            squarePaint.setStrokeWidth(5);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.WHITE);


            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            squareLeft = centerX - squareSize / 2;
            squareTop = centerY - squareSize / 2;
            squareRight = centerX + squareSize / 2;
            squareBottom = centerY + squareSize / 2;


            canvas.drawRect(squareLeft, squareTop, squareRight, squareBottom, squarePaint);


            canvas.drawCircle(bubbleX, bubbleY, 50, bubblePaint);


            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(50);
            canvas.drawText("Enter Count: " + enterCount, 50, 100, textPaint);
        }

        public int getSquareLeft() {
            return squareLeft;
        }

        public int getSquareTop() {
            return squareTop;
        }

        public int getSquareRight() {
            return squareRight;
        }

        public int getSquareBottom() {
            return squareBottom;
        }

        public void setEnterCount(int enterCount) {
            this.enterCount = enterCount;
        }
    }
}

