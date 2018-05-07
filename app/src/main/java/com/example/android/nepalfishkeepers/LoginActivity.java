package com.example.android.nepalfishkeepers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.UserParams;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    private EditText loginEmail;
    private EditText loginPass;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.loginEmail);
        loginPass = findViewById(R.id.loginPass);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void loginButtonClicked(View view) {
        String email = loginEmail.getText().toString().trim();
        String pass = loginPass.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
//             loginUser(email, pass);
            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Login", "onFailure: ", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkUserExists();
                        }
                    }
                });
        }
    }

//    public void loginUser(final String email, final String pass) {
//        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Map<String, Object> users = (Map<String, Object>) dataSnapshot.getValue();
//                for (final Map.Entry<String, Object> entry : users.entrySet()){
//                    //Get user map
//                    final Map singleUser = (Map) entry.getValue();
//                    String currentEmail = singleUser.get("email").toString();
//                    boolean blocked = (boolean) singleUser.get("blocked");
//                    boolean isBlocked = blocked && email.equals(currentEmail);
//
//                    if(!isBlocked) {
//                        mAuth.signInWithEmailAndPassword(email, pass)
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.e("Login", "onFailure: ", e);
//                                }
//                            })
//                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    final String user_name = (String) singleUser.get("Name");
//                                    final String user_id = entry.getKey();
//                                    UserParams userParams = new UserParams()
//                                            .setName(user_name)
//                                            .setLookupId(user_id);
//
//                                    HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
//                                        @Override
//                                        public void onSuccess(@NonNull SuccessResponse successResponse) {
//                                            // Handle success on getOrCreate user
//                                            Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
//                                            startActivity(loginIntent);
//                                            finish();
//                                        }
//
//                                        @Override
//                                        public void onError(@NonNull ErrorResponse errorResponse) {
//                                            // Handle error on getOrCreate user
//                                            Toast.makeText(LoginActivity.this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                }
//                                }
//                            });
//                    } else {
//                        Log.d("Login", "Login failed");
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }


    public void checkUserExists() {
        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {
                    final String user_name = (String) dataSnapshot.child(user_id).child("Name").getValue();
                    UserParams userParams = new UserParams()
                            .setName(user_name)
                            .setLookupId(user_id);

                    HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
                        @Override
                        public void onSuccess(@NonNull SuccessResponse successResponse) {
                            // Handle success on getOrCreate user
                            Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(loginIntent);
                            finish();
                        }

                        @Override
                        public void onError(@NonNull ErrorResponse errorResponse) {
                            // Handle error on getOrCreate user
                            Toast.makeText(LoginActivity.this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void registerRedirectView(View view) {
        TextView ltext = findViewById(R.id.registerRedirect);

        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);

        ltext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.finish();
            }
        });

    }


}

