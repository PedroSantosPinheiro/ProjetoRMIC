package com.example.projetormic;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements ArrayExample.DataFetchedListener {

    private TextView faultyValueTextView;
    private TextView maxTempTextView;
    private TextView minTempTextView;
    private TextView timestampTextView;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        faultyValueTextView = findViewById(R.id.faultyValue);
        maxTempTextView = findViewById(R.id.maxValue);
        minTempTextView = findViewById(R.id.minValue);
        timestampTextView = findViewById(R.id.timestampValue);

        Button fetchButton = findViewById(R.id.fetchButton);
        fetchButton.setOnClickListener(v -> {
            fetchDataFromFirebase();
            ArrayExample.fetchDataFromFirebase(this);
            Bitmap heatmapBitmap = generateHeatmap(ArrayExample.getArray(this), 320, 240);

            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(heatmapBitmap);

            generateColorbar();
            updateHeatmap();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("solar_panel_data/faulty_cell_count");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int faultyCount = dataSnapshot.getValue(Integer.class);
                    faultyValueTextView.setText(String.valueOf(faultyCount));
                    Log.d("FirebaseData", "Faulty Cell Count: " + faultyCount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to read faulty_cell_count", databaseError.toException());
            }
        });

        DatabaseReference maxTempRef = FirebaseDatabase.getInstance().getReference("solar_panel_data/max_temp");
        maxTempRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    float maxTemp = dataSnapshot.getValue(Float.class);
                    maxTempTextView.setText(String.format("%.2f ºC", maxTemp));
                    Log.d("FirebaseData", "Max Temp: " + maxTemp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to read max_temp", databaseError.toException());
            }
        });

        DatabaseReference minTempRef = FirebaseDatabase.getInstance().getReference("solar_panel_data/min_temp");
        minTempRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    float minTemp = dataSnapshot.getValue(Float.class);
                    minTempTextView.setText(String.format("%.2f ºC", minTemp));
                    Log.d("FirebaseData", "Min Temp: " + minTemp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to read min_temp", databaseError.toException());
            }
        });

        DatabaseReference timestampRef = FirebaseDatabase.getInstance().getReference("solar_panel_data/timestamp");
        timestampRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String timestamp = dataSnapshot.getValue(String.class);
                    timestampTextView.setText(timestamp);
                    Log.d("FirebaseData", "Timestamp: " + timestamp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to read timestamp", databaseError.toException());
            }
        });
    }

    @Override
    public void onDataFetched() {
        // Data is fetched, now update the heatmap
        updateHeatmap();
    }

    private void updateHeatmap() {
        // Assuming you have a method to generate the heatmap from the array
        Bitmap heatmapBitmap = generateHeatmap(ArrayExample.getArray(this), 320, 240);
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(heatmapBitmap);
    }

    private Bitmap generateHeatmap(double[][] data, int width, int height) {
        int rows = data.length;
        int cols = data[0].length;
        Bitmap bitmap = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                float value = (float) data[y][x];
                int color = getColorForTemperature(value, 20, 60);
                bitmap.setPixel(x, y, color);
            }
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private int getColorForTemperature(float value, float minTemp, float maxTemp) {
        float normalized = (value - minTemp) / (maxTemp - minTemp);
        normalized = Math.min(1f, Math.max(0f, normalized)); // Clamp between 0 and 1

        int red = (int) (Math.min(1.0f, Math.max(0.0f, normalized * 2 - 0.5f)) * 255);
        int green = (int) (Math.min(1.0f, Math.max(0.0f, 1.5f - Math.abs(normalized - 0.5f) * 2)) * 255);
        int blue = (int) (Math.min(1.0f, Math.max(0.0f, 1.5f - normalized * 2)) * 255);

        return Color.rgb(red, green, blue);
    }

    private void generateColorbar() {
        int width = 320;
        int height = 50;
        Bitmap colorbarBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            float temp = 20 + (60 - 20) * (x / (float) (width - 1));
            float normalized = (temp - 20) / (60 - 20);
            normalized = Math.min(1f, Math.max(0f, normalized));

            int red = (int) (Math.min(1.0f, Math.max(0.0f, normalized)) * 255);
            int green = (int) (Math.min(1.0f, Math.max(0.0f, 1.5f - Math.abs(normalized - 0.5f) * 2)) * 255);
            int blue = (int) (Math.min(1.0f, Math.max(0.0f, 1.5f - normalized * 2)) * 255);

            int color = Color.rgb(red, green, blue);
            for (int y = 0; y < height; y++) {
                colorbarBitmap.setPixel(x, y, color);
            }
        }

        ImageView colorbarImageView = findViewById(R.id.colorbarImageView);
        colorbarImageView.setImageBitmap(colorbarBitmap);
    }
}
