package com.example.android.nepalfishkeepers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 2;
    private Uri uri = null;
    private ImageButton imageButton;
    private EditText editName;
    private EditText editDesc;
    private Spinner spinnerCategory;
    private StorageReference storageReference;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        editName = findViewById(R.id.editName);
        editDesc = findViewById(R.id.editDesc);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = database.getInstance().getReference().child("NFK");


        Spinner mySpinner = findViewById(R.id.spinnerCategory);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(PostActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
    }

    public void imageButtonClicked(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture From"), GALLERY_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            uri = data.getData();
            imageButton = findViewById(R.id.imageButton);
            imageButton.setImageURI(uri);
        }
    }

    public void submitButtonClicked(View view) {

        final String titleValue = editName.getText().toString().trim();
        final String descValue = editDesc.getText().toString().trim();
        final String categoryValue = spinnerCategory.getSelectedItem().toString();

        if (!TextUtils.isEmpty(titleValue) && !TextUtils.isEmpty(descValue)) {
            if (uri != null) {
                StorageReference filePath = storageReference.child("PostImage").child(uri.getLastPathSegment());
                filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri downloadurl = taskSnapshot.getDownloadUrl();
                        Toast.makeText(PostActivity.this, "Upload Complete", Toast.LENGTH_LONG).show();
                        createNewPost(titleValue, descValue, categoryValue, downloadurl);

                    }
                });
            }
            else {
                createNewPost(titleValue, descValue, categoryValue, null);
            }
        }
    }

    private void createNewPost(final String titleValue, final String descValue, final String categoryValue, @Nullable final Uri downloadurl) {
        final DatabaseReference newPost = databaseReference.push();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                newPost.child("title").setValue(titleValue);
                newPost.child("desc").setValue(descValue);
                newPost.child("category").setValue(categoryValue);
                if (downloadurl != null) {
                    newPost.child("image").setValue(downloadurl.toString());
                }
                newPost.child("uid").setValue(mCurrentUser.getUid());
                newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Intent mainActivityIntent = new Intent(PostActivity.this, MainActivity.class);
                            startActivity(mainActivityIntent);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
