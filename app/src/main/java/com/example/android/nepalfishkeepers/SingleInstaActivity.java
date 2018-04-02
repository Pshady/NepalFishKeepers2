package com.example.android.nepalfishkeepers;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.time.temporal.TemporalQueries;

public class SingleInstaActivity extends AppCompatActivity {

    private String post_key = null;
    private DatabaseReference mDatabase;
    private ImageView singlePostImage;
    private TextView singlePostTitle;
    private TextView singlePostDesc;
    private Button deleteButton;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_insta);

        post_key = getIntent().getExtras().getString("PostId");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("NFK");

        singlePostDesc = findViewById(R.id.singleDesc);
        singlePostTitle = findViewById(R.id.singleTitle);
        singlePostImage = findViewById(R.id.singleImageView);

        mAuth = FirebaseAuth.getInstance();
        deleteButton = findViewById(R.id.singleDeleteButton);
        deleteButton.setVisibility(View.INVISIBLE);

        mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_desc = (String) dataSnapshot.child("desc").getValue();
                String post_image = (String) dataSnapshot.child("image").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();

                singlePostTitle.setText(post_title);
                singlePostDesc.setText(post_desc);
                Picasso.with(SingleInstaActivity.this).load(post_image).into(singlePostImage);

                if (mAuth.getCurrentUser().getUid().equals(post_uid)) {
                    deleteButton.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

     public void deleteButtonClicked(View view) {
        mDatabase.child(post_key).removeValue();
        Intent mainIntent = new Intent(SingleInstaActivity.this, MainActivity.class);
        startActivity(mainIntent);

    }


}
