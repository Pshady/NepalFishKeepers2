package com.example.android.nepalfishkeepers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.hypertrack.lib.models.User;
import com.hypertrack.lib.models.UserParams;
import com.shashank.sony.fancytoastlib.FancyToast;

public class LoginActivity extends AppCompatActivity {


    private EditText loginEmail;
    private EditText loginPass;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.loginEmail);
        loginPass = findViewById(R.id.loginPass);


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        LinearLayout linearLayout = findViewById(R.id.loginLayout);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }

    public void loginButtonClicked(View view) {
        String email = loginEmail.getText().toString().trim();
        String pass = loginPass.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Logging in...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(mProgressDialog != null) {
                                mProgressDialog.cancel();
                            }

                            FancyToast.makeText(getApplicationContext(), "Email or password not correct.!",
                                    FancyToast.LENGTH_SHORT,
                                    FancyToast.ERROR,
                                    false).show();

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

        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(pass)) {
            FancyToast.makeText(getApplicationContext(), "Enter email address and password!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            FancyToast.makeText(getApplicationContext(), "Enter email!",  FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            FancyToast.makeText(getApplicationContext(), "Enter password!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            return;
        }

    }

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

                            // Save Hypertrack user id
                            User user = (User) successResponse.getResponseObject();
                            mDatabase.child(user_id).child("hyperUserId").setValue(user.getId());

                            if(mProgressDialog != null) {
                                mProgressDialog.cancel();
                            }

                            // Handle success on getOrCreate user
                            Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(loginIntent);
                            finish();
                        }

                        @Override
                        public void onError(@NonNull ErrorResponse errorResponse) {
                            // Handle error on getOrCreate user
                            FancyToast.makeText(LoginActivity.this, errorResponse.getErrorMessage(), FancyToast.LENGTH_SHORT, FancyToast.ERROR,true).show();
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
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);

        LoginActivity.this.finish();

    }

    public void forgotPasswordRedirectClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(intent);

        LoginActivity.this.finish();
    }

}



