package com.example.s8wallpaper;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.Random;

public class LiveWallpaperService extends WallpaperService implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float gravityX = 0;
    private float gravityY = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new S8InfinityEngine();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Telefonun duruş açısına göre yumuşatılmış ivme değeri
            gravityX = -event.values[0] * 0.4f;
            gravityY = event.values[1] * 0.4f;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private class S8InfinityEngine extends Engine {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private boolean visible = false;
        private final ArrayList<Particle> particles = new ArrayList<>();
        private final Random random = new Random();
        private float touchX = -1, touchY = -1;
        private int currentTheme = 0;

        private final Runnable drawRunnable = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
            for (int i = 0; i < 140; i++) {
                particles.add(new Particle());
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                touchX = event.getX();
                touchY = event.getY();
            } else {
                touchX = -1;
                touchY = -1;
            }
            super.onTouchEvent(event);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                if (sensorManager != null && accelerometer != null) {
                    sensorManager.registerListener(LiveWallpaperService.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
                }
                loadTheme();
                handler.post(drawRunnable);
            } else {
                if (sensorManager != null) {
                    sensorManager.unregisterListener(LiveWallpaperService.this);
                }
                handler.removeCallbacks(drawRunnable);
            }
        }

        private void loadTheme() {
            SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
            currentTheme = prefs.getInt(SettingsActivity.KEY_THEME, 0);
            for (Particle p : particles) {
                p.updateColorTheme(currentTheme);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunnable);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    // Derin S8 AMOLED Arka Planı
                    canvas.drawColor(Color.parseColor("#04060E"));

                    int width = canvas.getWidth();
                    int height = canvas.getHeight();

                    Paint paint = new Paint();
                    paint.setAntiAlias(true);

                    for (Particle p : particles) {
                        p.update(width, height, touchX, touchY, gravityX, gravityY);

                        paint.setColor(p.color);
                        paint.setAlpha(p.alpha);
                        canvas.drawCircle(p.x, p.y, p.radius, paint);
                    }
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            handler.removeCallbacks(drawRunnable);
            if (visible) {
                handler.postDelayed(drawRunnable, 16);
            }
        }

        private class Particle {
            float x, y, speedX, speedY, radius;
            int alpha, color;

            Particle() {
                reset(2000, 3000);
            }

            void updateColorTheme(int theme) {
                int[][] themeColors = {
                    {Color.parseColor("#00E5FF"), Color.parseColor("#7C4DFF"), Color.parseColor("#18FFFF")}, // Arctic / Orchid
                    {Color.parseColor("#FFD700"), Color.parseColor("#FF8C00"), Color.parseColor("#FFE082")}, // Gold
                    {Color.parseColor("#FFFFFF"), Color.parseColor("#B0BEC5"), Color.parseColor("#ECEFF1")}, // Midnight Black
                    {Color.parseColor("#0288D1"), Color.parseColor("#00BCD4"), Color.parseColor("#80DEEA")}, // Coral Blue
                    {Color.parseColor("#FF4081"), Color.parseColor("#F48FB1"), Color.parseColor("#FF80AB")}  // Rose Pink
                };
                int[] selectedPalette = themeColors[Math.min(theme, 4)];
                color = selectedPalette[random.nextInt(selectedPalette.length)];
            }

            void reset(int width, int height) {
                x = random.nextFloat() * width;
                y = random.nextFloat() * height;
                speedX = (random.nextFloat() - 0.5f) * 1.2f;
                speedY = (random.nextFloat() - 0.8f) * 1.8f;
                radius = random.nextFloat() * 4.5f + 2.0f;
                alpha = random.nextInt(180) + 75;
                updateColorTheme(currentTheme);
            }

            void update(int width, int height, float tx, float ty, float gx, float gy) {
                // Jiroskop / Eğim kuvveti + Normal hareket
                x += speedX + gx;
                y += speedY + gy;

                // Dokunmatik çekim efekti
                if (tx != -1 && ty != -1) {
                    float dx = tx - x;
                    float dy = ty - y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < 320) {
                        x += (dx / dist) * 2.5f;
                        y += (dy / dist) * 2.5f;
                    }
                }

                if (x < 0 || x > width || y < 0 || y > height) {
                    reset(width, height);
                }
            }
        }
    }
}
