package com.example.projetormic;

import android.annotation.SuppressLint;

public class ArrayExample {

    public static double[][] getArray() {
        double[][] array = new double[24][32];

        // Fill the array
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 32; j++) {
                // Convert row and column to double with 2 decimal places
                @SuppressLint("DefaultLocale") String value = String.format("%02d.%02d", i, j);
                array[i][j] = Double.parseDouble(value);
            }
        }
        return array;
    }
}
