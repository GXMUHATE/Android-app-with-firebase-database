package com.gracane.synecoculture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class UploadImageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;


    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private EditText edtTextName;
    private EditText edtTextSurname;
    private EditText edtTextEmail;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonUpload = findViewById(R.id.button);

        mEditTextFileName = findViewById(R.id.edit_text_file_name);

        //dados do usuario
        edtTextName = findViewById(R.id.txtName);
        edtTextSurname = findViewById(R.id.txtSurname);
        edtTextEmail = findViewById(R.id.txtEmail);

        mImageView = findViewById(R.id.imageView);
        mProgressBar = findViewById(R.id.progress_bar);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mDatabaseRef.keepSynced(true); // TODO: Remove

        // Button Listeners
        mButtonChooseImage.setOnClickListener(v -> openFileChooser());

        mButtonUpload.setOnClickListener(v -> {

            uploadFile();

        });


    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadFile() {
        if (mImageUri != null) {
            mDatabaseRef.keepSynced(true);
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> mProgressBar.setProgress(0), 500);
                        Toast.makeText(UploadImageActivity.this, "Upload Successfull", Toast.LENGTH_LONG).show();
                        /*
                        Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                        String uploadId = mDatabaseRef.push().getKey();
                        mDatabaseRef.child(uploadId).setValue(upload);

                         */
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()) ;
                        Uri downloadUrl = urlTask.getResult();

                        //Log.d(TAG, "onSuccess: firebase download url: " + downloadUrl.toString()); //use if testing...don't need this line.
                        Upload upload = new Upload(mEditTextFileName.getText().toString().trim(), edtTextName.getText().toString().trim(), edtTextSurname.getText().toString().trim(), edtTextEmail.getText().toString().trim(), downloadUrl.toString());

                        // TODO: Create custom key here
                        String key = "";

                        String uploadId = mDatabaseRef.push().getKey();
                        mDatabaseRef.child(uploadId).setValue(upload);
                    })
                    .addOnFailureListener(e -> Toast.makeText(UploadImageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        mProgressBar.setProgress((int) progress);
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}