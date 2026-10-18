package com.example.s8wallpaper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    public static final String PREFS_NAME = "S8WallpaperPrefs";
    public static final String KEY_THEME = "s8_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(this);
        String[] themes = {
            "1. Orchid Gray / Arctic Silver (Mavi & Mor)",
            "2. Maple Gold (Altın & Kehribar)",
            "3. Midnight Black (Gümüş & Beyaz)",
            "4. Coral Blue (Okyanus & Turkuaz)",
            "5. Rose Pink (Pembe & Rose Gold)"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, themes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putInt(KEY_THEME, position).apply();
            Toast.makeText(this, "Tema Değiştirildi! Ana ekrana dönün.", Toast.LENGTH_SHORT).show();
            finish();
        });

        setContentView(listView);
    }
}
