package com.example.projetormic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

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

public class MainActivity extends AppCompatActivity{

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        Button fetchButtonLive = findViewById(R.id.fetchButtonLive);
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

        Button fetchButtonFaulty = findViewById(R.id.fetchButtonFaulty);
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
}
