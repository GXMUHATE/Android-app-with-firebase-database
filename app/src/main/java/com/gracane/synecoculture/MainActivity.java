package com.gracane.synecoculture;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {


    FloatingActionButton fabNewUpload;
    FloatingActionButton fabSeeUploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabNewUpload = findViewById(R.id.fabNewUpload);
        fabSeeUploads = findViewById(R.id.fabSeeUploads);

        fabNewUpload.setOnClickListener(v -> {
            Intent intent = new Intent(this, UploadImageActivity.class);
            startActivity(intent);
        });

        fabSeeUploads.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImagesActivity.class);
            startActivity(intent);
        });
    }
}