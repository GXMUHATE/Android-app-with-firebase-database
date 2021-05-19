package com.gracane.synecoculture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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

        // Set up back key on actionBar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


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
            saveUpload(mEditTextFileName.getText().toString(), edtTextName.getText().toString(), edtTextSurname.getText().toString(), edtTextEmail.getText().toString());
        });
    }

    private void saveUpload(String fileName, String uploaderName, String uploaderSurname, String uploaderEmail) {

        // This key can be composed of more elements
        // Here I preferred to use the email+uploaderSurname as a key
        String uploaderEmailFormatted = uploaderEmail.replace(".", "_");
        String key = uploaderEmailFormatted.trim() + uploaderSurname.trim();

        // Verifies if there's a child which id = key
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("uploads/" + key);
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Exist! Do whatever.
                    Toast.makeText(UploadImageActivity.this, "Upload with current data exists", Toast.LENGTH_SHORT).show();
                } else {
                    // Don't exist! Do something.
                    uploadFile(fileName, uploaderName, uploaderSurname, uploaderEmail);
                    // TODO (Optional): Implement on back pressed so that when upload is done, the app returns to the previous screen
                    // onBackPressed()
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed, how to handle?
                Toast.makeText(UploadImageActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

        });

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadFile(String fileName, String uploaderName, String uploaderSurname, String uploaderEmail) {
        if (mImageUri != null) {
            mDatabaseRef.keepSynced(true);
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> mProgressBar.setProgress(0), 500);
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
                        Upload upload = new Upload(
                                fileName,
                                uploaderName,
                                uploaderSurname,
                                uploaderEmail,
                                downloadUrl.toString());

                        // Custom key
                        String uploaderEmailFormatted = uploaderEmail.replace(".", "_");
                        String key = uploaderEmailFormatted.trim() + uploaderSurname.trim();

                        upload.setKey(key);

                        // String uploadId = mDatabaseRef.push().getKey();
                        mDatabaseRef.child(key).setValue(upload);
                        Toast.makeText(UploadImageActivity.this, "Upload Successfull", Toast.LENGTH_LONG).show();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            // Picasso.with(this).load(mImageUri).into(mImageView);
            //alteracoes

            Picasso.with(this).load(mImageUri).networkPolicy(NetworkPolicy.OFFLINE).into(mImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(getApplicationContext()).load(mImageUri).into(mImageView);
                }
            });
        }
    }

    // To go back when action bar 'back' icon pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}