package com.example.projetormic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity{

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                // Request permission if not granted
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1);
            }
        }

        DatabaseReference autoCameraRef = FirebaseDatabase.getInstance().getReference("thermal_camera_auto");

        autoCameraRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean autoCameraEnabled = Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class));

                    if (autoCameraEnabled) {
                        // Start LiveFeed activity directly
                        Intent intent = new Intent(MainActivity.this, LiveView.class);
                        intent.putExtra("openedAutomatically", true);
                        startActivity(intent);
                        //finish(); // Optional: finish MainActivity to prevent returning
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to read thermal_camera_auto", databaseError.toException());
            }
        });

        DatabaseReference faultyCellRef = FirebaseDatabase.getInstance().getReference("solar_panel_data/faulty_cell_count");
        faultyCellRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean faultyCellCount = dataSnapshot.getValue(Boolean.class);

                    if (Boolean.TRUE.equals(faultyCellCount)) {
                        // Trigger notification when faulty_cell_count is true
                        showNotification();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to read faulty_cell_count", databaseError.toException());
            }
        });

        LinearLayout fetchButtonLive = findViewById(R.id.fetchButtonLive);
        fetchButtonLive.setOnClickListener(v -> {
            // Get Firebase database reference
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("enable_thermal_camera_view");

            // Update the value to true
            databaseRef.setValue(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("FirebaseUpdate", "enable_thermal_camera_view set to true");

                    // Start SecondaryActivity after updating Firebase
                    Intent intent = new Intent(MainActivity.this, LiveView.class);
                    startActivity(intent);
                } else {
                    Log.e("FirebaseUpdate", "Failed to update enable_thermal_camera_view", task.getException());
                }
            });
        });

        LinearLayout fetchButtonFaulty = findViewById(R.id.fetchButtonFaulty);
        fetchButtonFaulty.setOnClickListener(v -> {

            // Start SecondaryActivity
            Intent intent = new Intent(MainActivity.this, FaultyView.class);
            startActivity(intent);

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can show notifications
                Log.d("Permission", "POST_NOTIFICATIONS permission granted");
            } else {
                // Permission denied, handle accordingly
                Log.d("Permission", "POST_NOTIFICATIONS permission denied");
            }
        }
    }

    private void showNotification() {
        // Check if the POST_NOTIFICATIONS permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

            // Create a Notification Channel (needed for Android 8.0 and above)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Faulty Cell Notification";
                String description = "Notification for faulty cell count";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel("faulty_cell_channel", name, importance);
                channel.setDescription(description);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            // Create the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "faulty_cell_channel")
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Faulty Cell Alert")
                    .setContentText("Faulty Cells Detected!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            // Show the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());
        } else {
            // Permission is not granted, handle accordingly
            Log.d("Permission", "POST_NOTIFICATIONS permission not granted. Requesting permission.");
        }
    }
}
