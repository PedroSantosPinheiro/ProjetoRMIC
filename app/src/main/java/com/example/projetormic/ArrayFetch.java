package com.example.projetormic;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ArrayFetch {

    private static final String TAG = "FirebaseData";
    private static double[][] array = new double[24][32];

    // Interface to notify when data is fetched
    public interface DataFetchedListener {
        void onDataFetched(double[][] array);
    }

    public static void fetchDataFromFirebase(String path, DataFetchedListener listener) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(path);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    for (int i = 0; i < 24; i++) {
                        for (int j = 0; j < 32; j++) {
                            Double value = dataSnapshot.child(String.valueOf(i)).child(String.valueOf(j)).getValue(Double.class);
                            if (value != null) {
                                array[i][j] = value;
                                Log.d(TAG, "Array[" + i + "][" + j + "] = " + array[i][j]);
                            }
                        }
                    }
                    Log.d(TAG, "Data updated successfully.");
                    if (listener != null) {
                        listener.onDataFetched(array); // Notify that data has been fetched
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing data", e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }
}
