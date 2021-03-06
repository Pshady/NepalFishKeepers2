package com.example.android.nepalfishkeepers;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
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
import com.google.gson.JsonObject;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.nepalfishkeepers.Constants.FCM_SERVER_KEY;

public class PostActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 2;
    private Uri uri = null;
    private ImageButton imageButton;
    private EditText editName;
    private EditText editDesc;
    private EditText editPrice;
    private Spinner spinnerCategory;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private FirebaseUser mCurrentUser;
    private ArrayList<String> mCategories;
    private Spinner mSpinner;

    @Override
    protected void onStart() {
        super.onStart();



        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("Categories");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mCategories = new ArrayList<>();
                    Map<String, Object> categories = (Map<String, Object>) dataSnapshot.getValue();
                    for (final Map.Entry<String, Object> entry : categories.entrySet()) {
                        //Get user map
                        final Map category = (Map) entry.getValue();
                        mCategories.add(category.get("name").toString());
                    }
                    Collections.sort(mCategories);
                    ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(PostActivity.this,
                            android.R.layout.simple_list_item_1, mCategories);
                    myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mSpinner.setAdapter(myAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        editName = findViewById(R.id.editName);
        editDesc = findViewById(R.id.editDesc);
        editPrice = findViewById(R.id.editPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("NFK");
        mSpinner = findViewById(R.id.spinnerCategory);
        mCategories = new ArrayList<>();

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
        final float price = TextUtils.isEmpty(editPrice.getText().toString()) ? 0
                : Float.parseFloat(editPrice.getText().toString());

        if (!TextUtils.isEmpty(titleValue) && !TextUtils.isEmpty(descValue) && price != 0) {
            if (uri != null) {
                StorageReference filePath = storageReference.child("PostImage").child(uri.getLastPathSegment());
                filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri downloadurl = taskSnapshot.getDownloadUrl();
                        Toast.makeText(PostActivity.this, "Upload Complete", Toast.LENGTH_LONG).show();
                        createNewPost(titleValue, descValue, categoryValue, downloadurl, price);

                    }
                });
            }
            else {
                createNewPost(titleValue, descValue, categoryValue, null, price);
            }
        }
    }

    private void sendNotifications(final String title, final String body, final String category, final String postKey) {
        String url ="https://fcm.googleapis.com/fcm/send";
        VolleyLog.DEBUG = true;

        JSONObject params = new JSONObject();
        JSONObject notification = new JSONObject();
        try {
            params.put("to", "/topics/" + category);
            notification.put("message", body);
            notification.put("title", title);
            notification.put("type", NotificationType.POST_CREATED);
            notification.put("postKey", postKey);
            params.put("data", notification);
        } catch (JSONException e) {
            Log.d("JsonError", e.getMessage());
        }

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Toast.makeText(getApplicationContext(), "Notification sent", Toast.LENGTH_SHORT).show();;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();;
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" + FCM_SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueueSingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonRequest);
    }

    private void createNewPost(final String titleValue, final String descValue, final String categoryValue, @Nullable final Uri downloadurl, final float priceValue) {
        final DatabaseReference newPost = databaseReference.push();

        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userimage = dataSnapshot.child("userimage").getValue().toString();
                String username = dataSnapshot.child("username").getValue().toString();

                Nfk post = new Nfk(titleValue, descValue,
                        downloadurl != null ? downloadurl.toString() : null, username,
                        categoryValue, userimage, mCurrentUser.getUid(), false, priceValue);
                newPost.setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            // Send notification
                            sendNotifications(titleValue, descValue, categoryValue, newPost.getKey());

                            FancyToast.makeText(getApplicationContext(), "Post created!",
                                    FancyToast.LENGTH_SHORT,
                                    FancyToast.ERROR,
                                    false).show();

                            Intent mainActivityIntent = new Intent(PostActivity.this, MainActivity.class);
                            startActivity(mainActivityIntent);
//                            PostActivity.this.finish();
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