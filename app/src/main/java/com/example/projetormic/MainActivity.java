package com.example.projetormic;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        double[][] array = ArrayExample.getArray();
        Bitmap heatmapBitmap = generateHeatmap(array, 320, 240);

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(heatmapBitmap);

        generateColorbar();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private Bitmap generateHeatmap(double[][] data, int width, int height) {
        int rows = data.length;
        int cols = data[0].length;
        Bitmap bitmap = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                float value = (float) data[y][x];
                int color = getColorForTemperature(value, 20, 40);
                bitmap.setPixel(x, y, color);
            }
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private int getColorForTemperature(float value, float minTemp, float maxTemp) {
        float normalized = (value - minTemp) / (maxTemp - minTemp);
        normalized = Math.min(1f, Math.max(0f, normalized)); // Clamp between 0 and 1

        int red = (int) (Math.min(1.0f, Math.max(0.0f, normalized * 2 - 0.5f)) * 255);
        int blue = (int) (Math.min(1.0f, Math.max(0.0f, 1.5f - normalized * 2)) * 255);
        int green = (int) (Math.min(1.0f, Math.max(0.0f, 1.5f - Math.abs(normalized - 0.5f) * 2)) * 255);

        return Color.rgb(red, green, blue);
    }

    private void generateColorbar() {
        int width = 320;
        int height = 50;
        Bitmap colorbarBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            float temp = 20 + (40 - 20) * (x / (float) (width - 1));
            float normalized = (temp - 20) / (40 - 20);
            normalized = Math.min(1f, Math.max(0f, normalized));

            int red = (int) (Math.min(1.0f, Math.max(0.0f, normalized * 2 - 0.5f)) * 255);
            int blue = (int) (Math.min(1.0f, Math.max(0.0f, 1.5f - normalized * 2)) * 255);
            int green = (int) (Math.min(1.0f, Math.max(0.0f, 1.5f - Math.abs(normalized - 0.5f) * 2)) * 255);

            int color = Color.rgb(red, green, blue);
            for (int y = 0; y < height; y++) {
                colorbarBitmap.setPixel(x, y, color);
            }
        }

        ImageView colorbarImageView = findViewById(R.id.colorbarImageView);
        colorbarImageView.setImageBitmap(colorbarBitmap);
    }
}
