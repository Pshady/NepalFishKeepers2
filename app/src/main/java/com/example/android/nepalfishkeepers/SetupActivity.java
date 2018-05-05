package com.example.android.nepalfishkeepers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private EditText editDisplayName;
    private ImageButton displayImage;
    private Uri mImageuri = null;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseusers;
    private StorageReference mStorageref;
    private Spinner spinnerSubs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        editDisplayName = findViewById(R.id.displayName);
        displayImage = findViewById(R.id.setupImageButton);

        mDatabaseusers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mStorageref = FirebaseStorage.getInstance().getReference().child("profile_image");

        spinnerSubs = findViewById(R.id.spinnerSubs);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(SetupActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubs.setAdapter(myAdapter);
    }

    public void profileImageButtonClicked(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture From"), GALLERY_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageuri = result.getUri();
                displayImage.setImageURI(mImageuri);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
        }
    }

    public void doneButtonClicked(View view) {
        final String name = editDisplayName.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();
        final String categoryValue = spinnerSubs.getSelectedItem().toString();
        final String subs = spinnerSubs.getSelectedItem().toString();

        FirebaseMessaging.getInstance().subscribeToTopic(subs);

        if (!TextUtils.isEmpty(name) && mImageuri != null) {
            StorageReference filepath = mStorageref.child(mImageuri.getLastPathSegment());
            filepath.putFile(mImageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                    mDatabaseusers.child(user_id).child("username").setValue(name);
                    mDatabaseusers.child(user_id).child("userimage").setValue(downloadUrl);
                    mDatabaseusers.child(user_id).child("subscription").setValue(categoryValue);

                    Toast.makeText(getApplicationContext(), "Registration successful.", Toast.LENGTH_LONG).show();

                    Intent directIntent = new Intent(SetupActivity.this, LoginActivity.class);
                    startActivity(directIntent);
                    finish();

                }
            });
        }

    }

}