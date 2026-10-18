package com.example.s8wallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
            gravityX = -event.values[0] * 0.5f;
            gravityY = event.values[1] * 0.5f;
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
            for (int i = 0; i < 160; i++) {
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
                    int width = canvas.getWidth();
                    int height = canvas.getHeight();

                    // S8 Degrade Arka Planı
                    Paint bgPaint = new Paint();
                    int[] bgColors = getBgGradientColors(currentTheme);
                    Shader gradient = new LinearGradient(0, 0, 0, height, bgColors[0], bgColors[1], Shader.TileMode.CLAMP);
                    bgPaint.setShader(gradient);
                    canvas.drawRect(0, 0, width, height, bgPaint);

                    Paint particlePaint = new Paint();
                    particlePaint.setAntiAlias(true);

                    for (Particle p : particles) {
                        p.update(width, height, touchX, touchY, gravityX, gravityY);

                        particlePaint.setColor(p.color);
                        particlePaint.setAlpha(p.alpha);
                        canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
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

        private int[] getBgGradientColors(int theme) {
            switch (theme) {
                case 0: // Orchid Gray
                    return new int[]{Color.parseColor("#1B1A2C"), Color.parseColor("#483D59")};
                case 1: // Maple Gold
                    return new int[]{Color.parseColor("#1D180C"), Color.parseColor("#4A3B18")};
                case 2: // Midnight Black
                    return new int[]{Color.parseColor("#050508"), Color.parseColor("#181920")};
                case 3: // Coral Blue
                    return new int[]{Color.parseColor("#0A1B2A"), Color.parseColor("#1A3B5C")};
                case 4: // Rose Pink
                    return new int[]{Color.parseColor("#2B151D"), Color.parseColor("#532738")};
                default:
                    return new int[]{Color.parseColor("#050814"), Color.parseColor("#101828")};
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
                    {Color.parseColor("#D8BFD8"), Color.parseColor("#E6E6FA"), Color.parseColor("#9370DB")}, // Orchid
                    {Color.parseColor("#FFE082"), Color.parseColor("#FFD54F"), Color.parseColor("#FFCA28")}, // Gold
                    {Color.parseColor("#FFFFFF"), Color.parseColor("#ECEFF1"), Color.parseColor("#CFD8DC")}, // Midnight
                    {Color.parseColor("#80DEEA"), Color.parseColor("#4DD0E1"), Color.parseColor("#00E5FF")}, // Coral Blue
                    {Color.parseColor("#F8BBD0"), Color.parseColor("#F48FB1"), Color.parseColor("#FF80AB")}  // Rose Pink
                };
                int[] selectedPalette = themeColors[Math.min(theme, 4)];
                color = selectedPalette[random.nextInt(selectedPalette.length)];
            }

            void reset(int width, int height) {
                // Parçacıkların %70'ini ekranın orta bandında kümele (S8 Orijinal Efekti)
                x = random.nextFloat() * width;
                if (random.nextBoolean()) {
                    y = (height * 0.25f) + (random.nextFloat() * (height * 0.5f));
                } else {
                    y = random.nextFloat() * height;
                }
                
                speedX = (random.nextFloat() - 0.5f) * 1.0f;
                speedY = (random.nextFloat() - 0.8f) * 1.5f;
                radius = random.nextFloat() * 4.0f + 1.5f;
                alpha = random.nextInt(160) + 80;
                updateColorTheme(currentTheme);
            }

            void update(int width, int height, float tx, float ty, float gx, float gy) {
                x += speedX + gx;
                y += speedY + gy;

                if (tx != -1 && ty != -1) {
                    float dx = tx - x;
                    float dy = ty - y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < 350) {
                        x += (dx / dist) * 3.0f;
                        y += (dy / dist) * 3.0f;
                    }
                }

                if (x < 0 || x > width || y < 0 || y > height) {
                    reset(width, height);
                }
            }
        }
    }
}
