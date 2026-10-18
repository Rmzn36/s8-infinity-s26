package com.example.s8wallpaper;

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

public class LiveWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new S8InfinityEngine();
    }

    private class S8InfinityEngine extends Engine {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private boolean visible = false;
        private final ArrayList<Particle> particles = new ArrayList<>();
        private final Random random = new Random();
        private float touchX = -1, touchY = -1;

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
            for (int i = 0; i < 130; i++) {
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
                handler.post(drawRunnable);
            } else {
                handler.removeCallbacks(drawRunnable);
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
                    canvas.drawColor(Color.parseColor("#050814"));

                    int width = canvas.getWidth();
                    int height = canvas.getHeight();

                    Paint paint = new Paint();
                    paint.setAntiAlias(true);

                    for (Particle p : particles) {
                        p.update(width, height, touchX, touchY);

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

            void reset(int width, int height) {
                x = random.nextFloat() * width;
                y = random.nextFloat() * height;
                speedX = (random.nextFloat() - 0.5f) * 1.5f;
                speedY = (random.nextFloat() - 0.8f) * 2.0f;
                radius = random.nextFloat() * 4 + 2;
                alpha = random.nextInt(180) + 75;
                
                int[] s8Colors = {Color.parseColor("#00E5FF"), Color.parseColor("#7C4DFF"), Color.parseColor("#18FFFF")};
                color = s8Colors[random.nextInt(s8Colors.length)];
            }

            void update(int width, int height, float tx, float ty) {
                x += speedX;
                y += speedY;

                if (tx != -1 && ty != -1) {
                    float dx = tx - x;
                    float dy = ty - y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < 300) {
                        x += (dx / dist) * 2;
                        y += (dy / dist) * 2;
                    }
                }

                if (x < 0 || x > width || y < 0 || y > height) {
                    reset(width, height);
                }
            }
        }
    }
}
