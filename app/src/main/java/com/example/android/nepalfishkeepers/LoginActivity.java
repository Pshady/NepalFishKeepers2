package com.example.android.nepalfishkeepers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;
import com.hypertrack.lib.models.UserParams;

public class LoginActivity extends AppCompatActivity {


    private EditText loginEmail;
    private EditText loginPass;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String email, pass;

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
        email = loginEmail.getText().toString().trim();
        pass = loginPass.getText().toString().trim();
        Log.i("kera","email:" +email);
        Log.i("kera","pass:" +pass);
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
            Log.i("kera","hunger" );
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
                            Log.i("kera","hunger1" );
                            if (task.isSuccessful()) {
                                Log.i("kera","hunger2" );
                                checkUserExists();
                            }
                        }
                    });
        }
    }

    public void checkUserExists() {
        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {
                    Toast.makeText(LoginActivity.this, "User exist.", Toast.LENGTH_LONG).show();
                    checkForLocationSettings();
                } else {
                    Toast.makeText(LoginActivity.this, "User Does not exist.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Call this method to check Location Settings before proceeding for UserLogin
     */
    private void checkForLocationSettings() {
        // Check for Location permission
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this);
            return;
        }

        // Check for Location settings
        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
        }

        // Location Permissions and Settings have been enabled
        // Proceed with your app logic here i.e User Login in this case
        attemptUserLogin();
    }


    public void attemptUserLogin(){
        email = loginEmail.getText().toString().trim();
        pass = loginPass.getText().toString().trim();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)){
            Log.i("msg",email);
            Log.i("msg",pass);
            Toast.makeText(this, "login_error_msg_invalid_params",
                    Toast.LENGTH_SHORT).show();
        }else{
            /**
             * Get or Create a User for given lookupId on HyperTrack Server here to
             * login your user & configure HyperTrack SDK with this generated
             * HyperTrack UserId.
             * OR
             * Implement your API call for User Login and get back a HyperTrack
             * UserId from your API Server to be configured in the HyperTrack SDK.
             */
            String UUID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            final String lookupId = HTTextUtils.isEmpty(UUID) ? email : UUID;

            UserParams userParams = new UserParams().setName(email).setLookupId(lookupId);
            HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    User user = (User) response.getResponseObject();
                    // Handle createUser success here, if required
                    // HyperTrack SDK auto-configures UserId on createUser API call,
                    // so no need to call HyperTrack.setUserId() API

                    // On UserLogin success
                    onUserLoginSuccess();
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    Toast.makeText(LoginActivity.this, "login_error_msg"
                                    + " " + errorResponse.getErrorMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Call this method when user has successfully logged in
     */
    private void onUserLoginSuccess() {
        HyperTrack.startTracking(new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse successResponse) {
                Toast.makeText(LoginActivity.this, "login_success_msg",
                        Toast.LENGTH_SHORT).show();

                // Start User Session by starting MainActivity
                Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
                finish();
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {

                Toast.makeText(LoginActivity.this, "login_error_msg"
                                + " " + errorResponse.getErrorMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handle on Grant Location Permissions request accepted/denied result
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);

        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Location Permission denied error
                Toast.makeText(this, "Location Permission denied.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handle on Enable Location Services request accepted/denied result
     *
     * @param requestCode
     * @param resultCode
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Enable Location Services request denied error
                Toast.makeText(this,"enable location settings" ,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }
}
