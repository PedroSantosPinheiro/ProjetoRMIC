package com.example.projetormic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FaultyView extends AppCompatActivity implements ArrayFetch.DataFetchedListener{

    private TextView faultyValueTextView;
    private TextView maxTempTextView;
    private TextView minTempTextView;
    private TextView avgTempTextView;
    private TextView timestampTextView;
    private TextView illuminanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_faulty_view);

        faultyValueTextView = findViewById(R.id.faultyValue);
        maxTempTextView = findViewById(R.id.maxValue);
        minTempTextView = findViewById(R.id.minValue);
        avgTempTextView = findViewById(R.id.avgValue);
        timestampTextView = findViewById(R.id.timestampValue);
        illuminanceTextView = findViewById(R.id.illuminanceValue);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title

        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.white));
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        generateColorbar();
        fetchDataFromFirebase();
        ArrayFetch.fetchDataFromFirebase("solar_panel_data_faulty/thermal_frame", (data) -> {
            onDataFetched(data);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_secondary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sync) {
            fetchDataFromFirebase();
            ArrayFetch.fetchDataFromFirebase("solar_panel_data_faulty/thermal_frame", (data) -> {
                onDataFetched(data);
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("solar_panel_data_faulty/faulty_cell_count");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean faultyStatus = Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class));
                    int textColor = faultyStatus ? getResources().getColor(R.color.red) : getResources().getColor(R.color.green);
                    faultyValueTextView.setTextColor(textColor);
                    faultyValueTextView.setText(faultyStatus ? "Faulty Cells Detected" : "No Faulty Cells");
                    Log.d("FirebaseData", "Faulty Cell Status: " + faultyStatus);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to read faulty_cell_count", databaseError.toException());
            }
        });

        DatabaseReference maxTempRef = FirebaseDatabase.getInstance().getReference("solar_panel_data_faulty/max_temp");
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

        DatabaseReference minTempRef = FirebaseDatabase.getInstance().getReference("solar_panel_data_faulty/min_temp");
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

        DatabaseReference avgTempRef = FirebaseDatabase.getInstance().getReference("solar_panel_data_faulty/baseline_temp");
        avgTempRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    float avgTemp = dataSnapshot.getValue(Float.class);
                    avgTempTextView.setText(String.format("%.2f ºC", avgTemp));
                    Log.d("FirebaseData", "Avg Temp: " + avgTemp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to read max_temp", databaseError.toException());
            }
        });

        DatabaseReference timestampRef = FirebaseDatabase.getInstance().getReference("solar_panel_data_faulty/timestamp");
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

        DatabaseReference illuminanceRef = FirebaseDatabase.getInstance().getReference("solar_panel_data_faulty/lux");
        illuminanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    float illuminance = dataSnapshot.getValue(Float.class);
                    illuminanceTextView.setText(String.format("%.2f lux", illuminance));
                    Log.d("FirebaseData", "Illuminance: " + illuminance);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to read min_temp", databaseError.toException());
            }
        });
    }

    private void generateColorbar() {
        int width = 320;
        int height = 50;
        Bitmap colorbarBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float[] positions = {0.0f, 0.25f, 0.5f, 0.75f, 1.0f};
        int[][] colors = {
                {0, 0, 255},    // True blue (#0000FF)
                {0, 255, 255},  // Cyan (#00FFFF)
                {0, 255, 0},    // True green (#00FF00)
                {255, 255, 0},  // Yellow (#FFFF00)
                {255, 0, 0},    // True red (#FF0000)
        };

        for (int x = 0; x < width; x++) {
            float temp = 20 + (60 - 20) * (x / (float) (width - 1));
            float normalized = (temp - 20) / (60 - 20);
            normalized = Math.min(1f, Math.max(0f, normalized));

            int red = 0, green = 0, blue = 0;
            for (int i = 0; i < positions.length - 1; i++) {
                if (normalized >= positions[i] && normalized <= positions[i + 1]) {
                    float segment = (normalized - positions[i]) / (positions[i + 1] - positions[i]);
                    red = (int) (colors[i][0] + segment * (colors[i + 1][0] - colors[i][0]));
                    green = (int) (colors[i][1] + segment * (colors[i + 1][1] - colors[i][1]));
                    blue = (int) (colors[i][2] + segment * (colors[i + 1][2] - colors[i][2]));
                    break;
                }
            }

            int color = Color.rgb(red, green, blue);
            for (int y = 0; y < height; y++) {
                colorbarBitmap.setPixel(x, y, color);
            }
        }

        ImageView colorbarImageView = findViewById(R.id.colorbarImageView);
        colorbarImageView.setImageBitmap(colorbarBitmap);
    }

    @Override
    public void onDataFetched(double[][] data) {
        Bitmap heatmapBitmap = generateHeatmap(data, 320, 240);
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(heatmapBitmap);
    }

    private Bitmap generateHeatmap(double[][] data, int width, int height) {
        int rows = data.length;
        int cols = data[0].length;

        // Create a new high-resolution array
        double[][] interpolatedData = new double[height][width];

        // Scale factors
        float rowScale = (float) (rows - 1) / (height - 1);
        float colScale = (float) (cols - 1) / (width - 1);

        // Perform bilinear interpolation
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float srcY = y * rowScale;
                float srcX = x * colScale;
                int y0 = (int) srcY;
                int x0 = (int) srcX;
                int y1 = Math.min(y0 + 1, rows - 1);
                int x1 = Math.min(x0 + 1, cols - 1);

                double q00 = data[y0][x0];
                double q01 = data[y0][x1];
                double q10 = data[y1][x0];
                double q11 = data[y1][x1];

                double r1 = q00 + (srcX - x0) * (q01 - q00);
                double r2 = q10 + (srcX - x0) * (q11 - q10);
                interpolatedData[y][x] = r1 + (srcY - y0) * (r2 - r1);
            }
        }

        // Create a bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Assign colors based on interpolated values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float value = (float) interpolatedData[y][x];
                int color = getColorForTemperature(value, 20, 45);
                bitmap.setPixel(x, y, color);
            }
        }

        return bitmap;
    }

    private int getColorForTemperature(float value, float minTemp, float maxTemp) {
        float normalized = (value - minTemp) / (maxTemp - minTemp);
        normalized = Math.min(1f, Math.max(0f, normalized)); // Clamp between 0 and 1

        float[] positions = {0.0f, 0.25f, 0.5f, 0.75f, 1.0f};
        int[][] colors = {
                {0, 0, 255},    // True blue (#0000FF)
                {0, 255, 255},  // Cyan (#00FFFF)
                {0, 255, 0},    // True green (#00FF00)
                {255, 255, 0},  // Yellow (#FFFF00)
                {255, 0, 0},    // True red (#FF0000)
        };

        for (int i = 0; i < positions.length - 1; i++) {
            if (normalized >= positions[i] && normalized <= positions[i + 1]) {
                float segment = (normalized - positions[i]) / (positions[i + 1] - positions[i]);
                int red = (int) (colors[i][0] + segment * (colors[i + 1][0] - colors[i][0]));
                int green = (int) (colors[i][1] + segment * (colors[i + 1][1] - colors[i][1]));
                int blue = (int) (colors[i][2] + segment * (colors[i + 1][2] - colors[i][2]));
                return Color.rgb(red, green, blue);
            }
        }

        // Fallback (shouldn't happen due to clamping)
        return Color.rgb(colors[colors.length - 1][0], colors[colors.length - 1][1], colors[colors.length - 1][2]);
    }
}