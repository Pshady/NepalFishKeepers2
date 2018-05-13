package com.example.android.nepalfishkeepers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView mLoginView;
    private EditText mEmail;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    private String mFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mLoginView = findViewById(R.id.loginResetRedirect);
        mEmail = findViewById(R.id.forgotPasswordText);
        mAuth = FirebaseAuth.getInstance();

        mFrom = getIntent().hasExtra("from") ?
                getIntent().getExtras().get("from").toString()
                : null;

        if (mFrom != null) {
            mEmail.setText(mAuth.getCurrentUser().getEmail());
            mLoginView.setText("Back");
            mLoginView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ForgotPasswordActivity.this.finish();
                }
            });
        }
    }

    public void resetPassButtonClicked(View v) {
        String email = mEmail.getText().toString();
        if (!TextUtils.isEmpty(email)) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Resetting password...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mEmail.setText("");

                                if (mProgressDialog != null) {
                                    mProgressDialog.cancel();
                                }
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "The reset link has been emailed to you.",
                                        Toast.LENGTH_SHORT).show();

                                if (mFrom != null) {
                                    mAuth.signOut();
                                }

                                redirectToLogin();
                            }
                        }
                    });
        }
    }


    public void loginResetRedirectClicked(View v) {
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        ForgotPasswordActivity.this.finish();
    }
}
