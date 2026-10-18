package com.example.s8wallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;

public class SettingsActivity extends Activity {

    public static final String PREFS_NAME = "S8WallpaperPrefs";
    public static final String KEY_THEME = "s8_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(this);
        String[] menuItems = {
            "--- CANLI TEMA SEÇİMİ (Jiroskoplu) ---",
            "1. Orchid Gray / Arctic Silver (Mavi & Mor)",
            "2. Maple Gold (Altın & Kehribar)",
            "3. Midnight Black (Gümüş & Beyaz)",
            "4. Coral Blue (Okyanus & Turkuaz)",
            "5. Rose Pink (Pembe & Rose Gold)",
            "--- STOK STİK DUVAR KAĞITLARINI UYGULA ---",
            "📷 S8 Orijinal Midnight Siyah (Static AMOLED)",
            "📷 S8 Orijinal Okyanus Mavi Gradient",
            "📷 S8 Orijinal Altın Çöl Tonal"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0 || position == 6) return;

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            if (position >= 1 && position <= 5) {
                // Canlı Temayı Güncelle
                int themeIndex = position - 1;
                prefs.edit().putInt(KEY_THEME, themeIndex).apply();
                Toast.makeText(this, "Canlı Tema Değiştirildi! Ana ekrana dönün.", Toast.LENGTH_SHORT).show();
                finish();
            } else if (position >= 7) {
                // Stok Sabit Görsel Oluştur ve Uygula
                applyStaticWallpaper(position - 7);
            }
        });

        setContentView(listView);
    }

    private void applyStaticWallpaper(int type) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        int width = 1440;
        int height = 2960;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        if (type == 0) {
            // Midnight Black Gradient
            Shader shader = new LinearGradient(0, 0, 0, height,
                    Color.parseColor("#0A0E1A"), Color.parseColor("#000000"), Shader.TileMode.CLAMP);
            paint.setShader(shader);
        } else if (type == 1) {
            // Coral Blue Gradient
            Shader shader = new LinearGradient(0, 0, 0, height,
                    Color.parseColor("#003973"), Color.parseColor("#E5E5BE"), Shader.TileMode.CLAMP);
            paint.setShader(shader);
        } else if (type == 2) {
            // Gold Gradient
            Shader shader = new LinearGradient(0, 0, 0, height,
                    Color.parseColor("#282800"), Color.parseColor("#0A0A00"), Shader.TileMode.CLAMP);
            paint.setShader(shader);
        }

        canvas.drawRect(0, 0, width, height, paint);

        try {
            wallpaperManager.setBitmap(bitmap);
            Toast.makeText(this, "S8 Stok Duvar Kağıdı Başarıyla Uygulandı!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Hata Oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
