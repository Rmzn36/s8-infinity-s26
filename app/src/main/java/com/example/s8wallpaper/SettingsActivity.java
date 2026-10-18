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
            "--- S8 CANLI INFINITY TEMALARI (120Hz & Jiroskop) ---",
            "1. Orchid Gray / Arctic Silver (Mor & Gümüş - Videodaki)",
            "2. Maple Gold (Altın & Kehribar)",
            "3. Midnight Black (Derin AMOLED Siyahı)",
            "4. Coral Blue (Okyanus Mavi)",
            "5. Rose Pink (Nar Çiçeği Pembe)",
            "--- S8 ORIJINAL STOK DUVAR KAĞITLARI (Statik) ---",
            "📷 S8 Orijinal Midnight Black Gradient",
            "📷 S8 Orijinal Coral Blue Gradient",
            "📷 S8 Orijinal Maple Gold Gradient",
            "📷 S8 Orijinal Orchid Purple Gradient"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0 || position == 6) return;

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            if (position >= 1 && position <= 5) {
                int themeIndex = position - 1;
                prefs.edit().putInt(KEY_THEME, themeIndex).apply();
                Toast.makeText(this, "Canlı Infinity Teması Değiştirildi!", Toast.LENGTH_SHORT).show();
                finish();
            } else if (position >= 7) {
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
        Shader shader;

        switch (type) {
            case 0: // Midnight Black
                shader = new LinearGradient(0, 0, 0, height, Color.parseColor("#050508"), Color.parseColor("#181920"), Shader.TileMode.CLAMP);
                break;
            case 1: // Coral Blue
                shader = new LinearGradient(0, 0, 0, height, Color.parseColor("#0A1B2A"), Color.parseColor("#1A3B5C"), Shader.TileMode.CLAMP);
                break;
            case 2: // Maple Gold
                shader = new LinearGradient(0, 0, 0, height, Color.parseColor("#1D180C"), Color.parseColor("#4A3B18"), Shader.TileMode.CLAMP);
                break;
            default: // Orchid Purple
                shader = new LinearGradient(0, 0, 0, height, Color.parseColor("#1B1A2C"), Color.parseColor("#483D59"), Shader.TileMode.CLAMP);
                break;
        }

        paint.setShader(shader);
        canvas.drawRect(0, 0, width, height, paint);

        try {
            wallpaperManager.setBitmap(bitmap);
            Toast.makeText(this, "S8 Stok Duvar Kağıdı Başarıyla Uygulandı!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Hata Oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
